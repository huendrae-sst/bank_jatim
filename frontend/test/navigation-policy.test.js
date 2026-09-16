import assert from 'node:assert/strict';
import test from 'node:test';

import {
  buildNavigationGroups,
  firstAccessiblePath,
  resolveNavigationItem,
  routeRuleMatches
} from '../src/utils/navigationPolicy.js';

function nav(code, module, path, allowed, order, parentCode = null, routeRules = []) {
  return {
    code,
    title: code,
    module,
    parentCode,
    path,
    icon: 'bi-circle',
    order,
    allowed,
    routeRules
  };
}

test('prefix matching respects path segment boundaries', () => {
  assert.equal(routeRuleMatches(
    { path: '/orders', matchType: 'PREFIX' },
    '/orders/42'
  ), true);
  assert.equal(routeRuleMatches(
    { path: '/orders', matchType: 'PREFIX' },
    '/orders-archive'
  ), false);
});

test('exact matching rejects child paths', () => {
  assert.equal(routeRuleMatches(
    { path: '/menus', matchType: 'EXACT' },
    '/menus'
  ), true);
  assert.equal(routeRuleMatches(
    { path: '/menus', matchType: 'EXACT' },
    '/menus/42'
  ), false);
});

test('the most specific rule owns a route even when access is denied', () => {
  const items = [
    nav('ORD_LIST', 'Order', '/orders', true, 10, null, [
      { path: '/orders', matchType: 'PREFIX', order: 1 }
    ]),
    nav('ORD_APPROVAL', 'Order', '/orders/approvals', false, 11, null, [
      { path: '/orders/approvals', matchType: 'PREFIX', order: 1 }
    ])
  ];

  const result = resolveNavigationItem(items, '/orders/approvals');

  assert.equal(result.code, 'ORD_APPROVAL');
  assert.equal(result.allowed, false);
});

test('groups filter denied items, honor order, and attach children', () => {
  const groups = buildNavigationGroups([
    nav('MASTER', 'Administration', '/master', true, 10),
    nav('USERS', 'Administration', '/master/users', true, 11, 'MASTER'),
    nav('ROLES', 'Administration', '/master/roles', false, 12, 'MASTER'),
    nav('ORD', 'Order', '/orders', true, 2)
  ]);

  assert.deepEqual(groups.map((group) => group.module), ['Order', 'Administration']);
  assert.deepEqual(
    groups[1].items[0].children.map((child) => child.code),
    ['USERS']
  );
});

test('allowed orphan is promoted to a module root', () => {
  const groups = buildNavigationGroups([
    nav('CHILD', 'Operations', '/child', true, 5, 'MISSING')
  ]);

  assert.equal(groups[0].items[0].code, 'CHILD');
});

test('first accessible path is data-driven and empty assignments return null', () => {
  assert.equal(firstAccessiblePath([
    nav('DENIED', 'Admin', '/denied', false, 1),
    nav('SECOND', 'Operations', '/second', true, 3),
    nav('FIRST', 'Operations', '/first', true, 2)
  ]), '/first');
  assert.equal(firstAccessiblePath([]), null);
});
