import assert from 'node:assert/strict';
import test from 'node:test';

import {
  clearNavigationState,
  createNavigationState,
  ensureNavigationLoaded
} from '../src/stores/navigationCore.js';

test('navigation loads once per user and reloads after identity changes', async () => {
  const state = createNavigationState();
  let calls = 0;
  const loader = async () => {
    calls += 1;
    return [{ code: `MENU_${calls}` }];
  };

  await ensureNavigationLoaded(state, loader, '7:REQUESTER_CABANG');
  await ensureNavigationLoaded(state, loader, '7:REQUESTER_CABANG');
  await ensureNavigationLoaded(state, loader, '8:AUDITOR');

  assert.equal(calls, 2);
  assert.equal(state.items[0].code, 'MENU_2');
  assert.equal(state.loadedForUser, '8:AUDITOR');
});

test('load failure clears prior permissions and keeps navigation closed', async () => {
  const state = createNavigationState();
  state.items = [{ code: 'OLD', allowed: true }];

  await assert.rejects(
    () => ensureNavigationLoaded(
      state,
      async () => { throw new Error('offline'); },
      '7:USER'
    ),
    /offline/
  );

  assert.deepEqual(state.items, []);
  assert.equal(state.loaded, false);
  assert.equal(state.loadedForUser, null);
  assert.equal(state.error, 'offline');
});

test('invalid response fails closed instead of caching malformed permissions', async () => {
  const state = createNavigationState();

  await assert.rejects(
    () => ensureNavigationLoaded(state, async () => ({ code: 'NOT_A_LIST' }), '7:USER'),
    /Format respons navigasi tidak valid/
  );

  assert.deepEqual(state.items, []);
  assert.equal(state.loaded, false);
});

test('clear removes items, identity, loading, and error state', () => {
  const state = createNavigationState();
  Object.assign(state, {
    items: [{ code: 'ORD_LIST' }],
    loading: true,
    loaded: true,
    loadedForUser: '7:USER',
    error: 'old error'
  });

  clearNavigationState(state);

  assert.deepEqual(state, {
    items: [],
    loading: false,
    loaded: false,
    loadedForUser: null,
    error: null
  });
});
