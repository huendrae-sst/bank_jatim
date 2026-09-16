import assert from 'node:assert/strict';
import test from 'node:test';

import { createNavigationApi } from '../src/services/navigationApi.js';

test('navigation API reads the authenticated navigation projection', async () => {
  const calls = [];
  const navigationApi = createNavigationApi({
    get: async (url) => {
      calls.push(url);
      return { success: true, data: [{ code: 'ORD_LIST' }] };
    }
  });

  const response = await navigationApi.getNavigation();

  assert.deepEqual(calls, ['/navigation']);
  assert.equal(response.data[0].code, 'ORD_LIST');
});
