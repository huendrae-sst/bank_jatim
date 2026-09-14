import assert from 'node:assert/strict';
import test from 'node:test';

import { createRoleApi } from '../src/services/roleApi.js';
import { createRoleMenuApi } from '../src/services/roleMenuApi.js';

function recordingClient() {
  const calls = [];
  return {
    calls,
    get: async (url) => { calls.push(['GET', url]); return { data: [] }; },
    post: async (url, body) => { calls.push(['POST', url, body]); return { data: body }; },
    put: async (url, body) => { calls.push(['PUT', url, body]); return { data: body }; },
    delete: async (url) => { calls.push(['DELETE', url]); return { data: null }; }
  };
}

test('role API sends CRUD operations using immutable role codes', async () => {
  const client = recordingClient();
  const roleApi = createRoleApi(client);
  const createPayload = { code: 'CUSTOM_AUDITOR', name: 'Auditor', description: '' };
  const updatePayload = { name: 'Auditor Cabang', description: 'Cabang' };

  await roleApi.list();
  await roleApi.create(createPayload);
  await roleApi.update('CUSTOM AUDITOR', updatePayload);
  await roleApi.remove('CUSTOM AUDITOR');

  assert.deepEqual(client.calls, [
    ['GET', '/master/roles'],
    ['POST', '/master/roles', createPayload],
    ['PUT', '/master/roles/CUSTOM%20AUDITOR', updatePayload],
    ['DELETE', '/master/roles/CUSTOM%20AUDITOR']
  ]);
});

test('role-menu API reads mappings and replaces one role menu list', async () => {
  const client = recordingClient();
  const roleMenuApi = createRoleMenuApi(client);

  await roleMenuApi.listAll();
  await roleMenuApi.getForRole('CUSTOM AUDITOR');
  await roleMenuApi.replaceForRole('CUSTOM AUDITOR', ['MST_USERS']);

  assert.deepEqual(client.calls, [
    ['GET', '/master/role-menus'],
    ['GET', '/master/roles/CUSTOM%20AUDITOR/menus'],
    ['PUT', '/master/roles/CUSTOM%20AUDITOR/menus', ['MST_USERS']]
  ]);
});
