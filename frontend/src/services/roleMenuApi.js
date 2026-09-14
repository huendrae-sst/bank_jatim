export function createRoleMenuApi(client) {
  return {
    listAll: () => client.get('/master/role-menus'),
    getForRole: (roleCode) => client.get(`/master/roles/${encodeURIComponent(roleCode)}/menus`),
    replaceForRole: (roleCode, menuCodes) => client.put(
      `/master/roles/${encodeURIComponent(roleCode)}/menus`,
      menuCodes
    )
  };
}
