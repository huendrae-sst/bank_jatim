export function createNavigationGuard({ getAuthStore, getNavigationStore }) {
  return async (to) => {
    if (to.meta.public) return true;

    const auth = getAuthStore();
    if (!auth.isAuthenticated) return '/login';
    if (to.meta.system) return true;

    const navigation = getNavigationStore();
    const identity = auth.user?.id ?? auth.user?.email;
    const userKey = `${String(identity)}:${auth.userRole}`;

    try {
      await navigation.ensureLoaded(userKey);
    } catch {
      return {
        name: 'access-denied',
        query: { from: to.fullPath, reason: 'unavailable' }
      };
    }

    if (to.path === '/') {
      return navigation.firstAccessiblePath || {
        name: 'access-denied',
        query: { reason: 'empty' }
      };
    }

    const owner = navigation.resolveRoute(to.path);
    if (!owner) {
      return {
        name: 'access-denied',
        query: { from: to.fullPath, reason: 'unmatched' }
      };
    }

    return owner.allowed === true
      ? true
      : {
          name: 'access-denied',
          query: { from: to.fullPath, reason: 'forbidden' }
        };
  };
}
