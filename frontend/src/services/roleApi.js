export function createRoleApi(client) {
  return {
    list: () => client.get('/master/roles'),
    create: (payload) => client.post('/master/roles', payload),
    update: (code, payload) => client.put(`/master/roles/${encodeURIComponent(code)}`, payload),
    remove: (code) => client.delete(`/master/roles/${encodeURIComponent(code)}`)
  };
}
