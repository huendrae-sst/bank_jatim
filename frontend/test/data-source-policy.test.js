import assert from 'node:assert/strict';
import { existsSync, readdirSync, readFileSync } from 'node:fs';
import { extname, join, relative, resolve } from 'node:path';
import test from 'node:test';

const frontendRoot = resolve(import.meta.dirname, '..');
const srcRoot = join(frontendRoot, 'src');
const forbiddenSeedFiles = [
  join(srcRoot, 'stores', 'masterDataSeeds.js'),
  join(srcRoot, 'stores', 'transactionSeeds.js')
];

function sourceFiles(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const path = join(directory, entry.name);
    if (entry.isDirectory()) return sourceFiles(path);
    return ['.js', '.vue'].includes(extname(entry.name)) ? [path] : [];
  });
}

test('runtime seed modules are removed', () => {
  const existing = forbiddenSeedFiles
    .filter(existsSync)
    .map((path) => relative(frontendRoot, path));

  assert.deepEqual(existing, [], `Remove runtime seed modules: ${existing.join(', ')}`);
});

test('application code does not import or assign runtime seed records', () => {
  const violations = [];

  for (const path of sourceFiles(srcRoot)) {
    const source = readFileSync(path, 'utf8');
    const relativePath = relative(frontendRoot, path);

    if (/from\s+['"]@\/stores\/(?:masterDataSeeds|transactionSeeds)['"]/.test(source)) {
      violations.push(`${relativePath}: imports a seed module`);
    }
    if (/\bDEFAULT_(?:ORDERS|PURCHASE_REQUESTS|PURCHASE_ORDERS|PICKING_ITEMS|PACKING_ITEMS|SHIPMENTS|RECEIVING_ITEMS|DISCREPANCIES|RETURNS|DESTRUCTIONS|EMBOSS_BATCHES|PRODUCTION_BATCHES|SETTLEMENTS|SWITCHING_DATA|DASHBOARD_METRICS|ITEMS|ORGANIZATIONS|BUDGETS|USERS|VENDORS|COURIERS|EXPEDITION_MAPPINGS|AUDIT_LOGS|NOTIFICATIONS|STOCK_BALANCES)\b/.test(source)) {
      violations.push(`${relativePath}: references DEFAULT_* business records`);
    }
    if (/\bDEFAULT_(?:MENUS|ROLES|EMBOSS_REJECTS)\b/.test(source)) {
      violations.push(`${relativePath}: references remaining DEFAULT_* records`);
    }
    if (/localStorage\.(?:getItem|setItem)\(['"]jims_master_(?:menus|roles)['"]/.test(source)) {
      violations.push(`${relativePath}: uses browser-cached master data as a source`);
    }
  }

  assert.deepEqual(violations, [], violations.join('\n'));
});
