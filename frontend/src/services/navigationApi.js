export function createNavigationApi(client) {
  return {
    getNavigation: () => client.get('/navigation')
  };
}
