export function normalizeRole(item) {
  if (!item || typeof item !== 'object' || Array.isArray(item) || !item.code || !item.name) {
    throw new Error('Format role dari backend tidak valid.');
  }
  return {
    id: item.id,
    code: String(item.code),
    name: String(item.name),
    description: item.description == null ? null : String(item.description),
    systemRole: Boolean(item.systemRole)
  };
}
