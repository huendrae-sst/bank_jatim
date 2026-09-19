import assert from 'node:assert/strict';
import test from 'node:test';
import ToastEventBus from 'primevue/toasteventbus';

import { toast } from '../src/utils/toast.js';
import { useToastNotification } from '../src/composables/useToastNotification.js';

test('toast utility emits add events to ToastEventBus with correct severity and detail', () => {
  const emitted = [];
  const listener = (msg) => emitted.push(msg);

  ToastEventBus.on('add', listener);

  toast.success('Data barang tersimpan', 'Sukses Simpan');
  assert.equal(emitted.length, 1);
  assert.deepEqual(emitted[0], {
    severity: 'success',
    summary: 'Sukses Simpan',
    detail: 'Data barang tersimpan',
    life: 4000
  });

  toast.error('Gagal mengambil data dari server');
  assert.equal(emitted.length, 2);
  assert.equal(emitted[1].severity, 'error');
  assert.equal(emitted[1].summary, 'Gagal');
  assert.equal(emitted[1].detail, 'Gagal mengambil data dari server');

  toast.warn('Stok mendekati batas minimum');
  assert.equal(emitted.length, 3);
  assert.equal(emitted[2].severity, 'warn');
  assert.equal(emitted[2].summary, 'Peringatan');

  toast.info('Sinkronisasi selesai');
  assert.equal(emitted.length, 4);
  assert.equal(emitted[3].severity, 'info');
  assert.equal(emitted[3].summary, 'Informasi');

  ToastEventBus.off('add', listener);
});

test('toast.clear emits remove-all-groups event', () => {
  let cleared = false;
  const clearListener = () => {
    cleared = true;
  };

  ToastEventBus.on('remove-all-groups', clearListener);
  toast.clear();
  assert.equal(cleared, true);

  ToastEventBus.off('remove-all-groups', clearListener);
});

test('useToastNotification composable returns the toast utility', () => {
  const composableToast = useToastNotification();
  assert.equal(typeof composableToast.success, 'function');
  assert.equal(typeof composableToast.error, 'function');
  assert.equal(typeof composableToast.warn, 'function');
  assert.equal(typeof composableToast.info, 'function');
});
