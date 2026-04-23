export async function apiFetch(path, { token, method, body } = {}) {
  const res = await fetch(path, {
    method: method ?? 'GET',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body ? { 'Content-Type': 'application/json' } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })

  if (!res.ok) {
    const text = await res.text()
    const contentType = res.headers.get('content-type') ?? ''
    const status = res.status
    if ((status === 401 || status === 403) && path === '/api/auth/login') {
      throw new Error('Username/password salah')
    }

    let parsed = null
    if (contentType.includes('application/json') && text) {
      try {
        parsed = JSON.parse(text)
      } catch {
        parsed = null
      }
    }

    const messageFromServer = (parsed && (parsed.message || parsed.error)) || text
    if (messageFromServer) throw new Error(messageFromServer)

    if (status === 401) throw new Error('Sesi habis. Silakan login lagi.')
    if (status === 403) throw new Error('Akses ditolak.')
    throw new Error(`Request gagal (${status})`)
  }

  if (res.status === 204) return null

  const text = await res.text()
  if (!text) return null

  const contentType = res.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) return JSON.parse(text)
  return text
}

