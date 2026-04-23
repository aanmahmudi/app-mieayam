export function normalizeLegacyHashRoute() {
  const hash = window.location.hash || ''
  if (!hash) return
  if (hash.startsWith('#/')) {
    const path = hash.slice(1)
    window.history.replaceState(null, '', path)
  }
}

export function getAuthRouteFromLocation() {
  const path = window.location.pathname || '/'
  if (path === '/register') return 'register'
  return 'login'
}

