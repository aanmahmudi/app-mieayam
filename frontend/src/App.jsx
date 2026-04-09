import { useCallback, useEffect, useMemo, useState } from 'react'
import './App.css'

const TOKEN_KEY = 'mieayam_token'

const rupiah = new Intl.NumberFormat('id-ID', {
  style: 'currency',
  currency: 'IDR',
  maximumFractionDigits: 0,
})

async function apiFetch(path, { token, method, body } = {}) {
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
    if (contentType.includes('application/json')) {
      try {
        const parsed = JSON.parse(text)
        throw new Error(parsed?.message || parsed?.error || `Request gagal (${res.status})`)
      } catch {
        throw new Error(text || `Request gagal (${res.status})`)
      }
    }
    throw new Error(text || `Request gagal (${res.status})`)
  }

  if (res.status === 204) return null

  const text = await res.text()
  if (!text) return null

  const contentType = res.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) return JSON.parse(text)
  return text
}

function normalizeLegacyHashRoute() {
  const hash = window.location.hash || ''
  if (!hash) return
  if (hash.startsWith('#/')) {
    const path = hash.slice(1)
    window.history.replaceState(null, '', path)
    return
  }
}

function getAuthRouteFromLocation() {
  const path = window.location.pathname || '/'
  if (path === '/register') return 'register'
  return 'login'
}

function formatDateTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleString('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) ?? '')
  const [activeCategory, setActiveCategory] = useState('MAKANAN')
  const [items, setItems] = useState([])
  const [loadingMenu, setLoadingMenu] = useState(false)
  const [errorMenu, setErrorMenu] = useState('')

  const [cart, setCart] = useState({})
  const [ordering, setOrdering] = useState(false)
  const [orderStatus, setOrderStatus] = useState('')
  const [cartOpen, setCartOpen] = useState(false)
  const [lastOrder, setLastOrder] = useState(null)
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [cashPaid, setCashPaid] = useState('')
  const [paying, setPaying] = useState(false)
  const [walletBalance, setWalletBalance] = useState(0)
  const [walletTxs, setWalletTxs] = useState([])
  const [topUpAmount, setTopUpAmount] = useState('')
  const [toppingUp, setToppingUp] = useState(false)
  const [sheetMode, setSheetMode] = useState('order')
  const [orderHistory, setOrderHistory] = useState([])
  const [loadingHistory, setLoadingHistory] = useState(false)
  const [errorHistory, setErrorHistory] = useState('')

  const [authRoute, setAuthRoute] = useState(() => getAuthRouteFromLocation())
  const [registerUsername, setRegisterUsername] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [registerStatus, setRegisterStatus] = useState('')

  const [loginUsername, setLoginUsername] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [loginStatus, setLoginStatus] = useState('')

  const categories = useMemo(
    () => [
      { key: 'MAKANAN', label: 'Makanan' },
      { key: 'MINUMAN', label: 'Minuman' },
      { key: 'EXTRA', label: 'Extra' },
    ],
    []
  )

  useEffect(() => {
    normalizeLegacyHashRoute()

    function onLocationChange() {
      setAuthRoute(getAuthRouteFromLocation())
      setRegisterStatus('')
      setLoginStatus('')
    }
    window.addEventListener('popstate', onLocationChange)
    onLocationChange()
    return () => window.removeEventListener('popstate', onLocationChange)
  }, [])

  useEffect(() => {
    if (!token) return
    let cancelled = false
    Promise.resolve().then(() => {
      if (cancelled) return
      setLoadingMenu(true)
      setErrorMenu('')
    })
    apiFetch(`/api/menu?category=${encodeURIComponent(activeCategory)}`, { token })
      .then((data) => {
        if (cancelled) return
        setItems(data ?? [])
      })
      .catch((e) => {
        if (cancelled) return
        setErrorMenu(e.message)
      })
      .finally(() => {
        if (cancelled) return
        setLoadingMenu(false)
      })
    return () => {
      cancelled = true
    }
  }, [token, activeCategory])

  async function handleRegister(e) {
    e.preventDefault()
    setRegisterStatus('')
    try {
      await apiFetch('/api/auth/register', {
        method: 'POST',
        body: { username: registerUsername, password: registerPassword },
      })
      setRegisterStatus('Register berhasil. Silakan login.')
      setRegisterUsername('')
      setRegisterPassword('')
    } catch (err) {
      setRegisterStatus(err.message)
    }
  }

  async function handleLogin(e) {
    e.preventDefault()
    setLoginStatus('')
    try {
      const data = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: { username: loginUsername, password: loginPassword },
      })
      const newToken = data?.token ?? ''
      localStorage.setItem(TOKEN_KEY, newToken)
      setToken(newToken)
      setLoginUsername('')
      setLoginPassword('')
      window.history.pushState(null, '', '/')
    } catch (err) {
      setLoginStatus(err.message)
    }
  }

  function handleLogout() {
    localStorage.removeItem(TOKEN_KEY)
    setToken('')
    setItems([])
    setErrorMenu('')
    setCart({})
    setOrderStatus('')
    setLastOrder(null)
    setPaymentMethod('CASH')
    setCashPaid('')
    setPaying(false)
    setCartOpen(false)
    window.history.pushState(null, '', '/login')
  }

  function goToLogin() {
    window.history.pushState(null, '', '/login')
    setAuthRoute('login')
    setRegisterStatus('')
    setLoginStatus('')
  }

  function goToRegister() {
    window.history.pushState(null, '', '/register')
    setAuthRoute('register')
    setRegisterStatus('')
    setLoginStatus('')
  }

  const cartEntries = useMemo(() => Object.values(cart), [cart])
  const cartCount = useMemo(() => cartEntries.reduce((acc, e) => acc + e.quantity, 0), [cartEntries])
  const cartTotal = useMemo(() => cartEntries.reduce((acc, e) => acc + e.quantity * e.item.price, 0), [cartEntries])
  const pendingPayment = !!lastOrder && lastOrder.status === 'CREATED'
  const sheetTitle = sheetMode === 'payment' ? 'Pembayaran' : sheetMode === 'receipt' ? 'Struk' : sheetMode === 'history' ? 'Riwayat' : 'Pesanan'
  const stepIndex = sheetMode === 'order' ? 0 : sheetMode === 'payment' ? 1 : sheetMode === 'receipt' ? 2 : 0

  const loadHistory = useCallback(async () => {
    if (!token) return
    setLoadingHistory(true)
    setErrorHistory('')
    try {
      const data = await apiFetch('/api/orders/my', { token })
      setOrderHistory(Array.isArray(data) ? data : [])
    } catch (err) {
      setErrorHistory(err.message)
    } finally {
      setLoadingHistory(false)
    }
  }, [token])

  useEffect(() => {
    if (!token) return
    apiFetch('/api/wallet/me', { token })
      .then((d) => setWalletBalance(d?.balance ?? 0))
      .catch(() => {})
    apiFetch('/api/wallet/transactions?limit=10', { token })
      .then((d) => setWalletTxs(Array.isArray(d) ? d : []))
      .catch(() => {})
    loadHistory()
  }, [token, loadHistory])

  useEffect(() => {
    if (lastOrder?.status === 'PAID') setSheetMode('receipt')
    else if (lastOrder?.status === 'CREATED') setSheetMode('payment')
  }, [lastOrder])

  function setCartItemQuantity(item, nextQuantity) {
    setCart((prev) => {
      const id = item.id
      const quantity = Math.max(0, Math.min(99, nextQuantity))
      if (quantity === 0) {
        const { [id]: _removed, ...rest } = prev
        return rest
      }
      return { ...prev, [id]: { item, quantity } }
    })
  }

  async function submitOrder() {
    if (!cartEntries.length || ordering) return
    setOrdering(true)
    setOrderStatus('')
    try {
      const payload = {
        items: cartEntries.map((e) => ({ menuItemId: e.item.id, quantity: e.quantity })),
      }
      const data = await apiFetch('/api/orders', { token, method: 'POST', body: payload })
      setCart({})
      setLastOrder(data ?? null)
      setPaymentMethod('CASH')
      setCashPaid('')
      setSheetMode('payment')
      setCartOpen(true)
      setOrderStatus(`Pesanan dibuat (#${data?.id}). Silakan lakukan pembayaran.`)
    } catch (err) {
      setOrderStatus(err.message)
    } finally {
      setOrdering(false)
    }
  }

  async function payOrder() {
    if (!lastOrder?.id || paying || !pendingPayment) return
    setPaying(true)
    setOrderStatus('')
    try {
      const payload =
        paymentMethod === 'CASH'
          ? { method: 'CASH', amountPaid: cashPaid ? Number.parseInt(cashPaid, 10) : null }
          : paymentMethod === 'QRIS'
            ? { method: 'QRIS', amountPaid: null }
            : { method: 'BANK', amountPaid: null }
      const data = await apiFetch(`/api/orders/${lastOrder.id}/pay`, { token, method: 'POST', body: payload })
      setLastOrder(data ?? lastOrder)
      apiFetch('/api/wallet/me', { token })
        .then((d) => setWalletBalance(d?.balance ?? 0))
        .catch(() => {})
      apiFetch('/api/wallet/transactions?limit=10', { token })
        .then((d) => setWalletTxs(Array.isArray(d) ? d : []))
        .catch(() => {})
      await loadHistory()
      if (data?.changeAmount) {
        setOrderStatus(`Pembayaran berhasil. Kembalian: ${rupiah.format(data.changeAmount)}`)
      } else {
        setOrderStatus('Pembayaran berhasil.')
      }
      setSheetMode('receipt')
      setCartOpen(true)
    } catch (err) {
      setOrderStatus(err.message)
    } finally {
      setPaying(false)
    }
  }

  async function topUp() {
    if (!token || toppingUp) return
    const amount = topUpAmount ? Number.parseInt(topUpAmount, 10) : 0
    if (!amount) return
    setToppingUp(true)
    setOrderStatus('')
    try {
      const data = await apiFetch('/api/wallet/topup', { token, method: 'POST', body: { amount } })
      setWalletBalance(data?.balance ?? walletBalance)
      setTopUpAmount('')
      const txs = await apiFetch('/api/wallet/transactions?limit=10', { token })
      setWalletTxs(Array.isArray(txs) ? txs : [])
      setOrderStatus('Top up berhasil.')
    } catch (err) {
      setOrderStatus(err.message)
    } finally {
      setToppingUp(false)
    }
  }

  return (
    <div className="container">
      {!token ? (
        <header className="header">
          <div className="titleWrap">
            <h1 className="title">Aplikasi Mie Ayam</h1>
          </div>
        </header>
      ) : null}

      {!token ? (
        <section className="card authCard">
          {authRoute === 'register' ? (
            <>
              <h2>Register</h2>
              <form className="form" onSubmit={handleRegister}>
                <label className="field">
                  <div className="label">Username</div>
                  <input
                    className="input"
                    value={registerUsername}
                    onChange={(e) => setRegisterUsername(e.target.value)}
                    placeholder="contoh: admin"
                  />
                </label>
                <label className="field">
                  <div className="label">Password</div>
                  <input
                    className="input"
                    type="password"
                    value={registerPassword}
                    onChange={(e) => setRegisterPassword(e.target.value)}
                    placeholder="minimal 6 karakter"
                  />
                </label>
                <button className="button primary" type="submit">
                  Register
                </button>
                {registerStatus ? <div className="status">{registerStatus}</div> : null}
              </form>
              <div className="status">
                Sudah punya akun?{' '}
                <button className="linkButton" type="button" onClick={goToLogin}>
                  Login
                </button>
              </div>
            </>
          ) : (
            <>
              <h2>Login</h2>
              <form className="form" onSubmit={handleLogin}>
                <label className="field">
                  <div className="label">Username</div>
                  <input
                    className="input"
                    value={loginUsername}
                    onChange={(e) => setLoginUsername(e.target.value)}
                  />
                </label>
                <label className="field">
                  <div className="label">Password</div>
                  <input
                    className="input"
                    type="password"
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                  />
                </label>
                <button className="button primary" type="submit">
                  Login
                </button>
                {loginStatus ? <div className="status">{loginStatus}</div> : null}
              </form>
              <div className="status">
                Belum punya akun?{' '}
                <button className="linkButton" type="button" onClick={goToRegister}>
                  Register
                </button>
              </div>
            </>
          )}
        </section>
      ) : (
        <>
          <header className="header">
            <div className="titleWrap">
              <h1 className="title">{categories.find((c) => c.key === activeCategory)?.label ?? 'Menu'}</h1>
            </div>
            <div className="headerRight">
              <button
                className="iconButton"
                type="button"
                onClick={() => {
                  setSheetMode('history')
                  setCartOpen(true)
                  loadHistory()
                }}
                disabled={loadingHistory}
                aria-label="Riwayat"
              >
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path
                    d="M12 8v4l2.5 1.5M3.5 12a8.5 8.5 0 1 0 2.49-6.01M3 4v4h4"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </button>
              <button className="button" onClick={handleLogout}>
                Logout
              </button>
            </div>
          </header>

          <section className="card">
            <div className="tabs">
              {categories.map((c) => (
                <button
                  key={c.key}
                  className={`tab ${activeCategory === c.key ? 'active' : ''}`}
                  onClick={() => setActiveCategory(c.key)}
                >
                  {c.label}
                </button>
              ))}
            </div>

            {loadingMenu ? <div className="status">Memuat menu...</div> : null}
            {errorMenu ? <div className="status error">{errorMenu}</div> : null}

            <div className="menuList">
              {items.map((it) => (
                <div className="menuRow" key={it.id}>
                  <div className="menuLeft">
                    {it.imageUrl ? (
                      <img className="menuThumb" alt={it.name} src={encodeURI(it.imageUrl)} loading="lazy" />
                    ) : null}
                    <div>
                    <div className="menuName">{it.name}</div>
                    {it.unit && it.category !== 'MINUMAN' ? <div className="menuUnit">{it.unit}</div> : null}
                    </div>
                  </div>
                  <div className="menuRight">
                    <div className="menuPrice">{rupiah.format(it.price)}</div>
                    <div className="qtyControl">
                      <button
                        className="qtyBtn"
                        type="button"
                        onClick={() => setCartItemQuantity(it, (cart[it.id]?.quantity ?? 0) - 1)}
                        disabled={ordering || (cart[it.id]?.quantity ?? 0) <= 0}
                      >
                        −
                      </button>
                      <div className="qtyNum">{cart[it.id]?.quantity ?? 0}</div>
                      <button
                        className="qtyBtn"
                        type="button"
                        onClick={() => {
                          setCartItemQuantity(it, (cart[it.id]?.quantity ?? 0) + 1)
                        }}
                        disabled={ordering || (cart[it.id]?.quantity ?? 0) >= 99}
                      >
                        +
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {cartCount ? (
            <div className="checkoutBarWrap">
              <div className="checkoutBar">
                <div>
                  <div className="checkoutTitle">Keranjang</div>
                  <div className="checkoutMeta">
                    {cartCount} item • {rupiah.format(cartTotal)}
                  </div>
                </div>
                <button
                  className="button primary"
                  type="button"
                  onClick={() => {
                    setSheetMode('order')
                    setCartOpen(true)
                  }}
                >
                  Checkout
                </button>
              </div>
            </div>
          ) : pendingPayment ? (
            <div className="checkoutBarWrap">
              <div className="checkoutBar">
                <div>
                  <div className="checkoutTitle">Pembayaran</div>
                  <div className="checkoutMeta">
                    Order #{lastOrder?.id} • {rupiah.format(lastOrder?.total ?? 0)}
                  </div>
                </div>
                <button
                  className="button primary"
                  type="button"
                  onClick={() => {
                    setSheetMode('payment')
                    setCartOpen(true)
                  }}
                >
                  Bayar
                </button>
              </div>
            </div>
          ) : lastOrder?.status === 'PAID' ? (
            <div className="checkoutBarWrap">
              <div className="checkoutBar">
                <div>
                  <div className="checkoutTitle">Struk</div>
                  <div className="checkoutMeta">Order #{lastOrder?.id}</div>
                </div>
                <button
                  className="button primary"
                  type="button"
                  onClick={() => {
                    setSheetMode('receipt')
                    setCartOpen(true)
                  }}
                >
                  Lihat
                </button>
              </div>
            </div>
          ) : null}

          {cartOpen ? (
            <>
              <div className="modalBackdrop" onClick={() => setCartOpen(false)} />
              <div className="modalWrap">
                <div className="modalCard">
                  <div className="modalHeader">
                    <div>
                      <div className="modalTitle">{sheetTitle}</div>
                      {sheetMode === 'order' ? (
                        <div className="modalMeta">
                          {cartCount} item • {rupiah.format(cartTotal)}
                        </div>
                      ) : lastOrder?.id ? (
                        <div className="modalMeta">
                          Order #{lastOrder.id}
                          {lastOrder.total ? ` • ${rupiah.format(lastOrder.total)}` : ''}
                        </div>
                      ) : null}
                    </div>
                    <button className="iconButton" type="button" onClick={() => setCartOpen(false)} aria-label="Tutup">
                      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path
                          d="M18 6L6 18M6 6l12 12"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        />
                      </svg>
                    </button>
                  </div>

                  {sheetMode !== 'history' ? (
                    <div className="stepper">
                      <div className={`step ${stepIndex >= 0 ? 'active' : ''}`}>
                        <div className={`stepDot ${stepIndex >= 0 ? 'active' : ''}`}>1</div>
                        <div className="stepLabel">Pesanan</div>
                      </div>
                      <div className={`stepLine ${stepIndex >= 1 ? 'active' : ''}`} />
                      <div className={`step ${stepIndex >= 1 ? 'active' : ''}`}>
                        <div className={`stepDot ${stepIndex >= 1 ? 'active' : ''}`}>2</div>
                        <div className="stepLabel">Pembayaran</div>
                      </div>
                      <div className={`stepLine ${stepIndex >= 2 ? 'active' : ''}`} />
                      <div className={`step ${stepIndex >= 2 ? 'active' : ''}`}>
                        <div className={`stepDot ${stepIndex >= 2 ? 'active' : ''}`}>3</div>
                        <div className="stepLabel">Struk</div>
                      </div>
                    </div>
                  ) : null}

                  <div className="modalBody">
                    {sheetMode === 'order' ? (
                      cartCount ? (
                        <>
                          <div className="cartItems">
                            {cartEntries
                              .slice()
                              .sort((a, b) => a.item.name.localeCompare(b.item.name))
                              .map((e) => (
                                <div className="cartRow" key={e.item.id}>
                                  <div className="cartLeft">
                                    <div className="cartName">{e.item.name}</div>
                                    <div className="cartSub">{rupiah.format(e.item.price)}</div>
                                  </div>
                                  <div className="cartActions">
                                    <button
                                      className="qtyBtn"
                                      type="button"
                                      onClick={() => setCartItemQuantity(e.item, e.quantity - 1)}
                                      disabled={ordering || e.quantity <= 0}
                                    >
                                      −
                                    </button>
                                    <div className="qtyNum">{e.quantity}</div>
                                    <button
                                      className="qtyBtn"
                                      type="button"
                                      onClick={() => setCartItemQuantity(e.item, e.quantity + 1)}
                                      disabled={ordering || e.quantity >= 99}
                                    >
                                      +
                                    </button>
                                  </div>
                                  <div className="cartSubtotal">{rupiah.format(e.quantity * e.item.price)}</div>
                                </div>
                              ))}
                          </div>
                          <button className="button primary cartSubmit" type="button" onClick={submitOrder} disabled={ordering}>
                            {ordering ? 'Memproses...' : 'Buat Pesanan'}
                          </button>
                        </>
                      ) : (
                        <div className="payHint">Pilih menu dulu dengan tombol +.</div>
                      )
                    ) : null}

                    {sheetMode === 'payment' ? (
                      pendingPayment ? (
                        <div className="payWrap">
                          <div className="paySummary">
                            <div className="payLabel">Total</div>
                            <div className="payValue">{rupiah.format(lastOrder?.total ?? 0)}</div>
                          </div>
                          <div className="payMethods">
                            <button
                              type="button"
                              className={`payMethodBtn ${paymentMethod === 'CASH' ? 'active' : ''}`}
                              onClick={() => setPaymentMethod('CASH')}
                              disabled={paying}
                            >
                              Cash
                            </button>
                            <button
                              type="button"
                              className={`payMethodBtn ${paymentMethod === 'QRIS' ? 'active' : ''}`}
                              onClick={() => setPaymentMethod('QRIS')}
                              disabled={paying}
                            >
                              QRIS
                            </button>
                            <button
                              type="button"
                              className={`payMethodBtn ${paymentMethod === 'BANK' ? 'active' : ''}`}
                              onClick={() => setPaymentMethod('BANK')}
                              disabled={paying}
                            >
                              Bank
                            </button>
                          </div>
                          {paymentMethod === 'CASH' ? (
                            <div className="payCash">
                              <div className="label">Uang dibayar</div>
                              <input
                                className="input"
                                inputMode="numeric"
                                placeholder="contoh: 50000"
                                value={cashPaid}
                                onChange={(e) => setCashPaid(e.target.value.replace(/[^\d]/g, ''))}
                                disabled={paying}
                              />
                              {cashPaid && Number.parseInt(cashPaid, 10) < (lastOrder?.total ?? 0) ? (
                                <div className="payHint">
                                  Kurang: {rupiah.format((lastOrder?.total ?? 0) - Number.parseInt(cashPaid, 10))}
                                </div>
                              ) : (
                                <div className="payHint">
                                  Kembalian:{' '}
                                  {rupiah.format(Math.max(0, (cashPaid ? Number.parseInt(cashPaid, 10) : 0) - (lastOrder?.total ?? 0)))}
                                </div>
                              )}
                            </div>
                          ) : paymentMethod === 'QRIS' ? (
                            <div className="payHint">Simulasi QRIS: klik Bayar untuk konfirmasi.</div>
                          ) : (
                            <div className="payBank">
                              <div className="paySummary">
                                <div className="payLabel">Saldo</div>
                                <div className="payValue">{rupiah.format(walletBalance)}</div>
                              </div>
                              <div className="payTopUp">
                                <div className="label">Top up</div>
                                <div className="payTopUpRow">
                                  <input
                                    className="input"
                                    inputMode="numeric"
                                    placeholder="contoh: 50000"
                                    value={topUpAmount}
                                    onChange={(e) => setTopUpAmount(e.target.value.replace(/[^\d]/g, ''))}
                                    disabled={toppingUp || paying}
                                  />
                                  <button className="button" type="button" onClick={topUp} disabled={toppingUp || paying}>
                                    {toppingUp ? '...' : 'Top up'}
                                  </button>
                                </div>
                              </div>
                              <div className="txTitle">Mutasi (terakhir)</div>
                              <div className="txList">
                                {walletTxs.length ? (
                                  walletTxs.map((tx) => (
                                    <div className="txRow" key={tx.id}>
                                      <div className="txLeft">
                                        <div className="txType">{tx.type === 'TOP_UP' ? 'Top up' : 'Pembayaran'}</div>
                                        {tx.orderId ? <div className="txMeta">Order #{tx.orderId}</div> : <div className="txMeta">Saldo: {rupiah.format(tx.balanceAfter)}</div>}
                                      </div>
                                      <div className={`txAmount ${tx.amount < 0 ? 'neg' : 'pos'}`}>{rupiah.format(tx.amount)}</div>
                                    </div>
                                  ))
                                ) : (
                                  <div className="payHint">Belum ada transaksi.</div>
                                )}
                              </div>
                            </div>
                          )}
                          <button
                            className="button primary cartSubmit"
                            type="button"
                            onClick={payOrder}
                            disabled={
                              paying ||
                              (paymentMethod === 'CASH' &&
                                (!cashPaid || Number.parseInt(cashPaid, 10) < (lastOrder?.total ?? 0))) ||
                              (paymentMethod === 'BANK' && walletBalance < (lastOrder?.total ?? 0))
                            }
                          >
                            {paying ? 'Memproses...' : 'Bayar'}
                          </button>
                        </div>
                      ) : (
                        <div className="payHint">Buat pesanan dulu untuk melakukan pembayaran.</div>
                      )
                    ) : null}

                    {sheetMode === 'receipt' && lastOrder ? (
                      <div className="receipt">
                        <div className="receiptHeader">
                          <div className="receiptTitle">Struk Pembayaran</div>
                          <div className="receiptMeta">Order #{lastOrder.id}</div>
                        </div>
                        <div className="receiptItems">
                          {(lastOrder.items ?? []).map((it) => (
                            <div className="receiptItemRow" key={it.menuItemId}>
                              <div className="receiptItemName">{it.name}</div>
                              <div className="receiptItemQty">
                                {it.quantity} x {rupiah.format(it.priceEach)}
                              </div>
                              <div className="receiptItemSub">{rupiah.format(it.subtotal)}</div>
                            </div>
                          ))}
                        </div>
                        <div className="receiptTotals">
                          <div className="receiptTotalRow">
                            <div>Total</div>
                            <div className="receiptStrong">{rupiah.format(lastOrder.total ?? 0)}</div>
                          </div>
                          <div className="receiptTotalRow">
                            <div>Status</div>
                            <div className="receiptStrong">{lastOrder.status === 'PAID' ? 'Lunas' : 'Belum dibayar'}</div>
                          </div>
                          {lastOrder.status === 'PAID' ? (
                            <>
                              <div className="receiptTotalRow">
                                <div>Metode</div>
                                <div className="receiptStrong">{lastOrder.paymentMethod ?? '-'}</div>
                              </div>
                              <div className="receiptTotalRow">
                                <div>Dibayar</div>
                                <div className="receiptStrong">{rupiah.format(lastOrder.amountPaid ?? 0)}</div>
                              </div>
                              <div className="receiptTotalRow">
                                <div>Kembalian</div>
                                <div className="receiptStrong">{rupiah.format(lastOrder.changeAmount ?? 0)}</div>
                              </div>
                            </>
                          ) : null}
                        </div>
                        <button
                          className="button primary receiptDone"
                          type="button"
                          onClick={async () => {
                            await loadHistory()
                            setLastOrder(null)
                            setCartOpen(false)
                          }}
                        >
                          Selesai
                        </button>
                      </div>
                    ) : null}

                    {sheetMode === 'history' ? (
                      <div className="history">
                        <div className="receiptHeader">
                          <div className="receiptTitle">Riwayat Pesanan</div>
                          <button className="button" type="button" onClick={loadHistory} disabled={loadingHistory}>
                            {loadingHistory ? 'Memuat...' : 'Refresh'}
                          </button>
                        </div>
                        {errorHistory ? <div className="status error">{errorHistory}</div> : null}
                        {!loadingHistory && !orderHistory.length ? <div className="payHint">Belum ada riwayat pesanan.</div> : null}
                        <div className="historyList">
                          {orderHistory.map((o) => (
                            <div className="historyCard" key={o.id}>
                              <div className="historyTop">
                                <div className="historyTopLeft">
                                  <div className="historyOrderId">Order #{o.id}</div>
                                  <div className="historyWhen">{formatDateTime(o.createdAt)}</div>
                                </div>
                                <div className={`historyBadge ${o.status === 'PAID' ? 'paid' : 'pending'}`}>
                                  {o.status === 'PAID' ? 'Lunas' : 'Belum dibayar'}
                                </div>
                              </div>
                              <div className="historyMid">
                                <div className="historyAmount">{rupiah.format(o.total)}</div>
                                <div className="historyMetaLine">
                                  {(o.items?.length ?? 0) ? `${o.items.length} item` : 'Tanpa item'} {o.paymentMethod ? `• ${o.paymentMethod}` : null}
                                </div>
                              </div>
                              <div className="historyActions">
                                <button
                                  className="button primary"
                                  type="button"
                                  onClick={() => {
                                    setLastOrder(o)
                                    setSheetMode(o.status === 'PAID' ? 'receipt' : 'payment')
                                  }}
                                >
                                  {o.status === 'PAID' ? 'Lihat Struk' : 'Bayar'}
                                </button>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    ) : null}

                    {orderStatus ? <div className={`status ${orderStatus.includes('berhasil') ? '' : 'error'}`}>{orderStatus}</div> : null}
                  </div>
                </div>
              </div>
            </>
          ) : null}
        </>
      )}
    </div>
  )
}

export default App
