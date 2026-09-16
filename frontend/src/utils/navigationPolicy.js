export function normalizeNavigationPath(value) {
  const path = String(value || '/').split(/[?#]/, 1)[0] || '/';
  if (path === '/') return '/';
  return `/${path.replace(/^\/+|\/+$/g, '')}`;
}

export function routeRuleMatches(rule, targetPath) {
  const rulePath = normalizeNavigationPath(rule?.path);
  const target = normalizeNavigationPath(targetPath);

  if (rule?.matchType === 'EXACT') return target === rulePath;
  if (rule?.matchType === 'PREFIX') {
    return target === rulePath || target.startsWith(`${rulePath}/`);
  }
  return false;
}

export function resolveNavigationItem(items, targetPath) {
  const matches = items.flatMap((item) =>
    (item.routeRules || [])
      .filter((rule) => routeRuleMatches(rule, targetPath))
      .map((rule) => ({
        item,
        rule,
        length: normalizeNavigationPath(rule.path).length
      }))
  );

  matches.sort((left, right) =>
    right.length - left.length
      || Number(right.rule.matchType === 'EXACT') - Number(left.rule.matchType === 'EXACT')
      || (left.rule.order || 0) - (right.rule.order || 0)
  );
  return matches[0]?.item || null;
}

const byOrder = (left, right) =>
  (Number(left.order) || 0) - (Number(right.order) || 0);

export function buildNavigationGroups(items) {
  const visible = items
    .filter((item) => item.allowed === true)
    .map((item) => ({ ...item, children: [] }))
    .sort(byOrder);
  const byCode = new Map(visible.map((item) => [item.code, item]));
  const roots = [];

  for (const item of visible) {
    const parent = item.parentCode ? byCode.get(item.parentCode) : null;
    if (parent) parent.children.push(item);
    else roots.push(item);
  }

  const groups = new Map();
  for (const item of roots) {
    const moduleName = item.module || 'Lainnya';
    if (!groups.has(moduleName)) {
      groups.set(moduleName, {
        key: moduleName,
        module: moduleName,
        icon: item.icon,
        order: Number(item.order) || 0,
        items: []
      });
    }
    groups.get(moduleName).items.push(item);
  }

  return [...groups.values()].sort(byOrder);
}

export function firstAccessiblePath(items) {
  return [...items]
    .filter((item) => item.allowed === true && item.path)
    .sort(byOrder)[0]?.path || null;
}
