import assert from 'node:assert/strict';
import test from 'node:test';

import { normalizeRole } from '../src/utils/roleMapper.js';

test('normalizeRole returns the persistent backend fields', () => {
  assert.deepEqual(normalizeRole({
    id: 9,
    code: 'CUSTOM_AUDITOR',
    name: 'Auditor Custom',
    description: 'Audit terbatas',
    systemRole: false
  }), {
    id: 9,
    code: 'CUSTOM_AUDITOR',
    name: 'Auditor Custom',
    description: 'Audit terbatas',
    systemRole: false
  });
});

test('normalizeRole rejects legacy strings instead of inventing role records', () => {
  assert.throws(() => normalizeRole('USER_ADMIN'), /Format role dari backend tidak valid/);
  assert.throws(() => normalizeRole({ name: 'Tanpa kode' }), /Format role dari backend tidak valid/);
});
