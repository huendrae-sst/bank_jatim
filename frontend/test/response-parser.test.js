import assert from 'node:assert/strict';
import test from 'node:test';

import { extractList } from '../src/utils/responseParser.js';

const rows = [{ id: 1 }, { id: 2 }];

test('extractList accepts direct and interceptor-unwrapped lists', () => {
  assert.deepEqual(extractList(rows), rows);
  assert.deepEqual(extractList({ data: rows }), rows);
});

test('extractList accepts Spring API and page response envelopes', () => {
  assert.deepEqual(extractList({ data: { data: { content: rows } } }), rows);
  assert.deepEqual(extractList({ data: { content: rows } }), rows);
  assert.deepEqual(extractList({ content: rows }), rows);
});

test('extractList preserves valid empty responses', () => {
  assert.deepEqual(extractList([]), []);
  assert.deepEqual(extractList({ data: [] }), []);
  assert.deepEqual(extractList({ data: { content: [] } }), []);
});

test('extractList rejects invalid list envelopes', () => {
  assert.equal(extractList(null), null);
  assert.equal(extractList({ data: { value: 1 } }), null);
});
