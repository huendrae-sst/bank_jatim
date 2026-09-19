import assert from 'node:assert/strict';
import test from 'node:test';

import { globalLoading } from '../src/stores/loading.js';
import { useLoading } from '../src/composables/useLoading.js';

test('globalLoading tracks active requests and updates isLoading state', () => {
  globalLoading.forceStop();
  assert.equal(globalLoading.isLoading, false);
  assert.equal(globalLoading.count, 0);

  globalLoading.start('Memuat data test...');
  assert.equal(globalLoading.isLoading, true);
  assert.equal(globalLoading.count, 1);
  assert.equal(globalLoading.message, 'Memuat data test...');

  globalLoading.start();
  assert.equal(globalLoading.count, 2);

  globalLoading.stop();
  assert.equal(globalLoading.count, 1);
  assert.equal(globalLoading.isLoading, true);

  globalLoading.stop();
  assert.equal(globalLoading.count, 0);
  assert.equal(globalLoading.isLoading, false);
});

test('useLoading withLoading wraps async operations and updates loading state', async () => {
  globalLoading.forceStop();
  const { loading, withLoading } = useLoading();

  assert.equal(loading.value, false);

  let executed = false;
  const result = await withLoading(async () => {
    assert.equal(loading.value, true);
    assert.equal(globalLoading.isLoading, true);
    executed = true;
    return 'success-data';
  }, { message: 'Operasi async...' });

  assert.equal(executed, true);
  assert.equal(result, 'success-data');
  assert.equal(loading.value, false);
  assert.equal(globalLoading.isLoading, false);
});

test('useLoading withLoading handles errors and resets loading state', async () => {
  globalLoading.forceStop();
  const { loading, withLoading } = useLoading();

  await assert.rejects(
    async () => {
      await withLoading(async () => {
        throw new Error('Test error');
      });
    },
    { message: 'Test error' }
  );

  assert.equal(loading.value, false);
  assert.equal(globalLoading.isLoading, false);
});
