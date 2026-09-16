import assert from 'node:assert/strict';
import test from 'node:test';

import { createNavigationGuard } from '../src/router/navigationGuard.js';

function route(path, meta = {}) {
  return { path, fullPath: path, meta };
}

function navigationStore(owner, home = '/orders') {
  return {
    loadedKeys: [],
    firstAccessiblePath: home,
    async ensureLoaded(userKey) {
      this.loadedKeys.push(userKey);
    },
    resolveRoute() {
      return owner;
    }
  };
}

function dependencies(navigation) {
  return {
    getAuthStore: () => ({
      isAuthenticated: true,
      user: { id: 7, email: 'ayu@bankjatim.co.id' },
      userRole: 'REQUESTER_CABANG'
    }),
    getNavigationStore: () => navigation
  };
}

test('public route bypasses authentication and navigation loading', async () => {
  let navigationRequested = false;
  const guard = createNavigationGuard({
    getAuthStore: () => { throw new Error('auth should not load'); },
    getNavigationStore: () => {
      navigationRequested = true;
      return navigationStore(null);
    }
  });

  assert.equal(await guard(route('/login', { public: true })), true);
  assert.equal(navigationRequested, false);
});

test('unauthenticated user is redirected before navigation loads', async () => {
  let loads = 0;
  const guard = createNavigationGuard({
    getAuthStore: () => ({ isAuthenticated: false }),
    getNavigationStore: () => ({
      ensureLoaded: async () => { loads += 1; }
    })
  });

  assert.equal(await guard(route('/orders')), '/login');
  assert.equal(loads, 0);
});

test('authorized direct URL loads current user navigation and passes', async () => {
  const navigation = navigationStore({ code: 'ORD_LIST', allowed: true });
  const guard = createNavigationGuard(dependencies(navigation));

  assert.equal(await guard(route('/orders/42')), true);
  assert.deepEqual(navigation.loadedKeys, ['7:REQUESTER_CABANG']);
});

test('specific denied owner blocks direct URL access', async () => {
  const navigation = navigationStore({ code: 'ORD_APPROVAL', allowed: false });

  assert.deepEqual(
    await createNavigationGuard(dependencies(navigation))(route('/orders/approvals')),
    {
      name: 'access-denied',
      query: { from: '/orders/approvals', reason: 'forbidden' }
    }
  );
});

test('system page bypasses matching and unmatched protected route fails closed', async () => {
  const navigation = navigationStore(null);
  const guard = createNavigationGuard(dependencies(navigation));

  assert.equal(await guard(route('/access-denied', { system: true })), true);
  assert.deepEqual(await guard(route('/unregistered')), {
    name: 'access-denied',
    query: { from: '/unregistered', reason: 'unmatched' }
  });
});

test('navigation loading failure fails closed', async () => {
  const navigation = navigationStore(null);
  navigation.ensureLoaded = async () => { throw new Error('offline'); };

  assert.deepEqual(
    await createNavigationGuard(dependencies(navigation))(route('/orders')),
    {
      name: 'access-denied',
      query: { from: '/orders', reason: 'unavailable' }
    }
  );
});

test('root selects first accessible data path or empty state', async () => {
  const withHome = navigationStore(null, '/inventory/balances');
  assert.equal(
    await createNavigationGuard(dependencies(withHome))(route('/')),
    '/inventory/balances'
  );

  const empty = navigationStore(null, null);
  assert.deepEqual(
    await createNavigationGuard(dependencies(empty))(route('/')),
    { name: 'access-denied', query: { reason: 'empty' } }
  );
});
