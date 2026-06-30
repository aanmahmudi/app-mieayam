import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import './App.css'
import { AuthCard } from './components/AuthCard.jsx'
import { AdminMenuPanel } from './components/AdminMenuPanel.jsx'
import { HistorySheet } from './components/HistorySheet.jsx'
import { OrderSheet } from './components/OrderSheet.jsx'
import { PaymentSheet } from './components/PaymentSheet.jsx'
import { ReceiptSheet } from './components/ReceiptSheet.jsx'
import { Stepper } from './components/Stepper.jsx'
import { apiFetch } from './lib/api.js'
import { rupiah } from './lib/format.js'
import { getAuthRouteFromLocation, normalizeLegacyHashRoute } from './lib/routing.js'
import { categories } from './menu/categories.js'
import { ExtraMenu } from './menu/ExtraMenu.jsx'
import { MakananMenu } from './menu/MakananMenu.jsx'
import { MinumanMenu } from './menu/MinumanMenu.jsx'

const TOKEN_KEY = 'mieayam_token'
const USERNAME_KEY = 'mieayam_username'

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
  const [_walletTxs, setWalletTxs] = useState([])
  const [sheetMode, setSheetMode] = useState('order')
  const [orderHistory, setOrderHistory] = useState([])
  const [loadingHistory, setLoadingHistory] = useState(false)
  const [errorHistory, setErrorHistory] = useState('')
  const [isAdmin, setIsAdmin] = useState(false)
  const [currentUsername, setCurrentUsername] = useState(() => localStorage.getItem(USERNAME_KEY) ?? '')
  const [bankChoice, setBankChoice] = useState('BRI')
  const [adminMenuItems, setAdminMenuItems] = useState([])
  const [loadingAdminMenu, setLoadingAdminMenu] = useState(false)
  const [errorAdminMenu, setErrorAdminMenu] = useState('')
  const [menuDraft, setMenuDraft] = useState({ id: null, name: '', category: 'MAKANAN', unit: '', price: '', imageUrl: '' })
  const [savingMenu, setSavingMenu] = useState(false)
  const [adminTab, setAdminTab] = useState('menu')
  const [adminWeeklyStats, setAdminWeeklyStats] = useState([])
  const [adminRecentOrders, setAdminRecentOrders] = useState([])
  const [loadingAdminReport, setLoadingAdminReport] = useState(false)
  const [errorAdminReport, setErrorAdminReport] = useState('')
  const [adminReportFromDate, setAdminReportFromDate] = useState('')
  const [adminReportToDate, setAdminReportToDate] = useState('')

  const [authRoute, setAuthRoute] = useState(() => getAuthRouteFromLocation())
  const [registerUsername, setRegisterUsername] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [registerStatus, setRegisterStatus] = useState('')

  const [loginUsername, setLoginUsername] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [loginStatus, setLoginStatus] = useState('')
  const modalRef = useRef(null)

  const scrollModalToTopSoon = useCallback(() => {
    window.scrollTo(0, 0)
    document.body.scrollTop = 0
    document.documentElement.scrollTop = 0
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        const el = modalRef.current
        if (!el) return
        el.scrollTop = 0
      })
    })
  }, [])

  useEffect(() => {
    if (!cartOpen) return
    scrollModalToTopSoon()
  }, [cartOpen, sheetMode, scrollModalToTopSoon])

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
      localStorage.setItem(USERNAME_KEY, loginUsername)
      setToken(newToken)
      setCurrentUsername(loginUsername)
      setActiveCategory('MAKANAN')
      setLoginUsername('')
      setLoginPassword('')
      window.history.pushState(null, '', '/')
      setSheetMode('order')
      setCartOpen(false)
      setCart({})
      setLastOrder(null)
      setOrderHistory([])
      setErrorHistory('')
    } catch (err) {
      setLoginStatus(err.message)
    }
  }

  const handleLogout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USERNAME_KEY)
    setToken('')
    setActiveCategory('MAKANAN')
    setItems([])
    setErrorMenu('')
    setCart({})
    setOrderStatus('')
    setLastOrder(null)
    setPaymentMethod('CASH')
    setCashPaid('')
    setPaying(false)
    setCartOpen(false)
    setOrderHistory([])
    setLoadingHistory(false)
    setErrorHistory('')
    setWalletBalance(0)
    setWalletTxs([])
    setIsAdmin(false)
    setCurrentUsername('')
    setSheetMode('order')
    window.history.pushState(null, '', '/login')
  }, [])

  const handleAuthError = useCallback((err) => {
    const message = err?.message ?? ''
    if (!message) return false
    if (!message.includes('Sesi habis')) return false
    return true
  }, [])

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
  const sheetTitle =
    sheetMode === 'admin'
      ? 'Admin'
      : sheetMode === 'payment'
        ? 'Pembayaran'
        : sheetMode === 'receipt'
          ? 'Struk'
          : sheetMode === 'history'
            ? 'Riwayat'
            : 'Pesanan'
  const stepIndex = sheetMode === 'order' ? 0 : sheetMode === 'payment' ? 1 : sheetMode === 'receipt' ? 2 : 0

  const loadHistory = useCallback(async () => {
    if (!token) return
    setLoadingHistory(true)
    setErrorHistory('')
    try {
      const data = await apiFetch('/api/orders/my', { token })
      setOrderHistory(Array.isArray(data) ? data : [])
    } catch (err) {
      if (handleAuthError(err)) return
      setErrorHistory(err.message)
    } finally {
      setLoadingHistory(false)
    }
  }, [token, handleAuthError])

  const loadAdminMenu = useCallback(async () => {
    if (!token) return
    setLoadingAdminMenu(true)
    setErrorAdminMenu('')
    try {
      const data = await apiFetch('/api/menu', { token })
      setAdminMenuItems(Array.isArray(data) ? data : [])
    } catch (err) {
      if (handleAuthError(err)) return
      setErrorAdminMenu(err.message)
      setAdminMenuItems([])
    } finally {
      setLoadingAdminMenu(false)
    }
  }, [token, handleAuthError])

  const loadAdminReport = useCallback(async (params) => {
    if (!token) return
    const fromDate = params?.fromDate ?? adminReportFromDate
    const toDate = params?.toDate ?? adminReportToDate
    setLoadingAdminReport(true)
    setErrorAdminReport('')
    try {
      const qsDate =
        fromDate || toDate
          ? `&fromDate=${encodeURIComponent(fromDate || '')}&toDate=${encodeURIComponent(toDate || '')}`
          : ''
      const [weekly, recent] = await Promise.all([
        apiFetch(fromDate || toDate ? `/api/admin/orders/weekly-stats?weeks=12${qsDate}` : '/api/admin/orders/weekly-stats?weeks=12', { token }),
        apiFetch(`/api/admin/orders/recent?limit=60${qsDate}`, { token }),
      ])
      setAdminWeeklyStats(Array.isArray(weekly) ? weekly : [])
      setAdminRecentOrders(Array.isArray(recent) ? recent : [])
    } catch (err) {
      if (handleAuthError(err)) return
      setErrorAdminReport(err.message)
      setAdminWeeklyStats([])
      setAdminRecentOrders([])
    } finally {
      setLoadingAdminReport(false)
    }
  }, [token, handleAuthError, adminReportFromDate, adminReportToDate])

  useEffect(() => {
    if (!token) return
    apiFetch('/api/wallet/me', { token })
      .then((d) => setWalletBalance(d?.balance ?? 0))
      .catch((err) => {
        handleAuthError(err)
      })
    apiFetch('/api/wallet/transactions?limit=10', { token })
      .then((d) => setWalletTxs(Array.isArray(d) ? d : []))
      .catch((err) => {
        handleAuthError(err)
      })
    apiFetch('/api/auth/me', { token })
      .then((d) => {
        setIsAdmin(Array.isArray(d?.roles) && d.roles.includes('ROLE_ADMIN'))
        const username = d?.username ?? ''
        setCurrentUsername(username)
        if (username) localStorage.setItem(USERNAME_KEY, username)
      })
      .catch((err) => {
        if (handleAuthError(err)) return
        setIsAdmin(false)
        setCurrentUsername('')
        localStorage.removeItem(USERNAME_KEY)
      })
    loadHistory()
  }, [token, loadHistory, handleAuthError])

  useEffect(() => {
    if (!token) {
      setOrderHistory([])
      setWalletTxs([])
      setWalletBalance(0)
      setIsAdmin(false)
      setCurrentUsername('')
      localStorage.removeItem(USERNAME_KEY)
      if (sheetMode === 'admin') setSheetMode('order')
    }
  }, [token, sheetMode])

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
      scrollModalToTopSoon()
      setOrderStatus('Pesanan dibuat. Silakan lakukan pembayaran.')
    } catch (err) {
      if (handleAuthError(err)) return
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
            : { method: 'BANK', amountPaid: null, bank: bankChoice }
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
      if (handleAuthError(err)) return
      setOrderStatus(err.message)
    } finally {
      setPaying(false)
    }
  }

  async function cancelOrder(orderId) {
    if (!token || !orderId || paying) return
    setOrderStatus('')
    try {
      await apiFetch(`/api/orders/${orderId}/cancel`, { token, method: 'POST' })
      setOrderHistory((prev) => prev.filter((o) => o?.id !== orderId))
      await loadHistory()
      if (lastOrder?.id === orderId) {
        setLastOrder(null)
        setSheetMode('order')
      }
      setOrderStatus('Pesanan dibatalkan.')
    } catch (err) {
      if (handleAuthError(err)) return
      setOrderStatus(err.message)
    }
  }

  async function saveAdminMenuItem() {
    if (!token || savingMenu) return
    if (!menuDraft.name.trim()) {
      setOrderStatus('Nama menu wajib diisi.')
      return
    }
    const price = menuDraft.price === '' ? 0 : Number.parseInt(menuDraft.price, 10)
    if (Number.isNaN(price) || price < 0) {
      setOrderStatus('Harga tidak valid.')
      return
    }
    setSavingMenu(true)
    setOrderStatus('')
    try {
      const payload = {
        name: menuDraft.name.trim(),
        category: menuDraft.category,
        unit: menuDraft.unit?.trim() || null,
        price,
        imageUrl: menuDraft.imageUrl?.trim() || null,
      }
      if (menuDraft.id) {
        await apiFetch(`/api/admin/menu/${menuDraft.id}`, { token, method: 'PUT', body: payload })
        setOrderStatus('Menu berhasil di-update.')
      } else {
        await apiFetch('/api/admin/menu', { token, method: 'POST', body: payload })
        setOrderStatus('Menu berhasil ditambahkan.')
      }
      setMenuDraft({ id: null, name: '', category: 'MAKANAN', unit: '', price: '', imageUrl: '' })
      loadAdminMenu()
    } catch (err) {
      if (handleAuthError(err)) return
      setOrderStatus(err.message)
    } finally {
      setSavingMenu(false)
    }
  }

  async function deleteAdminMenuItem(id) {
    if (!token || savingMenu) return
    setSavingMenu(true)
    setOrderStatus('')
    try {
      await apiFetch(`/api/admin/menu/${id}`, { token, method: 'DELETE' })
      setOrderStatus('Menu berhasil dihapus.')
      if (menuDraft.id === id) {
        setMenuDraft({ id: null, name: '', category: 'MAKANAN', unit: '', price: '', imageUrl: '' })
      }
      loadAdminMenu()
    } catch (err) {
      if (handleAuthError(err)) return
      setOrderStatus(err.message)
    } finally {
      setSavingMenu(false)
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
        <AuthCard
          authRoute={authRoute}
          registerUsername={registerUsername}
          setRegisterUsername={setRegisterUsername}
          registerPassword={registerPassword}
          setRegisterPassword={setRegisterPassword}
          registerStatus={registerStatus}
          onRegister={handleRegister}
          onGoToLogin={goToLogin}
          loginUsername={loginUsername}
          setLoginUsername={setLoginUsername}
          loginPassword={loginPassword}
          setLoginPassword={setLoginPassword}
          loginStatus={loginStatus}
          onLogin={handleLogin}
          onGoToRegister={goToRegister}
        />
      ) : (
        <>
          <header className="header">
            <div className="titleWrap">
              <h1 className="title">{categories.find((c) => c.key === activeCategory)?.label ?? 'Menu'}</h1>
            </div>
            <div className="headerRight">
              {isAdmin ? (
                <button
                  className="iconButton"
                  type="button"
                  onClick={() => {
                    setSheetMode('admin')
                    setAdminTab('report')
                    setCartOpen(true)
                    loadAdminMenu()
                    loadAdminReport()
                  }}
                  disabled={loadingAdminMenu}
                  aria-label="Admin"
                >
                  <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path
                      d="M12 2l7 4v6c0 5-3 9-7 10-4-1-7-5-7-10V6l7-4z"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinejoin="round"
                    />
                    <path
                      d="M9.5 12.5l1.7 1.7 3.6-3.6"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                </button>
              ) : null}
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

            {activeCategory === 'MAKANAN' ? (
              <MakananMenu items={items} cart={cart} ordering={ordering} onSetQuantity={setCartItemQuantity} />
            ) : activeCategory === 'MINUMAN' ? (
              <MinumanMenu items={items} cart={cart} ordering={ordering} onSetQuantity={setCartItemQuantity} />
            ) : (
              <ExtraMenu items={items} cart={cart} ordering={ordering} onSetQuantity={setCartItemQuantity} />
            )}
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
                    setCartOpen(true)
                    setSheetMode('order')
                    scrollModalToTopSoon()
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
                    Total • {rupiah.format(lastOrder?.total ?? 0)}
                  </div>
                </div>
                <button
                  className="button primary"
                  type="button"
                  onClick={() => {
                    setSheetMode('payment')
                    setCartOpen(true)
                    scrollModalToTopSoon()
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
                  <div className="checkoutMeta">Pembayaran selesai</div>
                </div>
                <button
                  className="button primary"
                  type="button"
                  onClick={() => {
                    setSheetMode('receipt')
                    setCartOpen(true)
                    scrollModalToTopSoon()
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
              <div className="modalWrap top">
                  <div ref={modalRef} className={`modalCard ${sheetMode === 'history' || sheetMode === 'admin' ? 'tall' : ''}`}>
                  {sheetMode !== 'history' ? (
                    <div className="modalHeader">
                      <div>
                        <div className="modalTitle">{sheetTitle}</div>
                        {sheetMode === 'order' ? (
                          <div className="modalMeta">
                            {cartCount} item • {rupiah.format(cartTotal)}
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
                  ) : null}

                  {sheetMode !== 'history' && sheetMode !== 'admin' ? (
                    <Stepper
                      stepIndex={stepIndex}
                      canGoOrder={cartCount || lastOrder}
                      canGoPayment={pendingPayment || lastOrder}
                      canGoReceipt={lastOrder}
                      onGoOrder={() => {
                        setSheetMode('order')
                        scrollModalToTopSoon()
                      }}
                      onGoPayment={() => {
                        setSheetMode('payment')
                        scrollModalToTopSoon()
                      }}
                      onGoReceipt={() => {
                        setSheetMode('receipt')
                        scrollModalToTopSoon()
                      }}
                    />
                  ) : null}

                  <div className="modalBody">
                    {sheetMode === 'admin' ? (
                      <AdminMenuPanel
                        adminTab={adminTab}
                        setAdminTab={setAdminTab}
                        errorAdminMenu={errorAdminMenu}
                        menuDraft={menuDraft}
                        setMenuDraft={setMenuDraft}
                        savingMenu={savingMenu}
                        onSaveMenuItem={saveAdminMenuItem}
                        onRefreshMenu={loadAdminMenu}
                        adminMenuItems={adminMenuItems}
                        loadingAdminMenu={loadingAdminMenu}
                        onDeleteMenuItem={deleteAdminMenuItem}
                        weeklyStats={adminWeeklyStats}
                        recentOrders={adminRecentOrders}
                        loadingReport={loadingAdminReport}
                        errorReport={errorAdminReport}
                        reportFromDate={adminReportFromDate}
                        setReportFromDate={setAdminReportFromDate}
                        reportToDate={adminReportToDate}
                        setReportToDate={setAdminReportToDate}
                        onRefreshReport={loadAdminReport}
                      />
                    ) : null}

                    {sheetMode === 'order' ? (
                      <OrderSheet
                        cartCount={cartCount}
                        cartEntries={cartEntries}
                        ordering={ordering}
                        onSetQuantity={setCartItemQuantity}
                        onSubmitOrder={submitOrder}
                        lastOrder={lastOrder}
                        onGoToPayment={() => {
                          setSheetMode('payment')
                          scrollModalToTopSoon()
                        }}
                      />
                    ) : null}

                    {sheetMode === 'payment' ? (
                      <PaymentSheet
                        pendingPayment={pendingPayment}
                        lastOrder={lastOrder}
                        paymentMethod={paymentMethod}
                        setPaymentMethod={setPaymentMethod}
                        cashPaid={cashPaid}
                        setCashPaid={setCashPaid}
                        paying={paying}
                        walletBalance={walletBalance}
                        bankChoice={bankChoice}
                        setBankChoice={setBankChoice}
                        onPay={payOrder}
                        onCancel={() => cancelOrder(lastOrder?.id)}
                      />
                    ) : null}

                    {sheetMode === 'receipt' ? (
                      <ReceiptSheet
                        token={token}
                        currentUsername={currentUsername}
                        lastOrder={lastOrder}
                        paymentMethod={paymentMethod}
                        onDone={async () => {
                          await loadHistory()
                          setLastOrder(null)
                          setCartOpen(false)
                        }}
                      />
                    ) : null}

                    {sheetMode === 'history' ? (
                      <HistorySheet
                        orderHistory={orderHistory}
                        loadingHistory={loadingHistory}
                        errorHistory={errorHistory}
                        onRefresh={loadHistory}
                        onClose={() => setCartOpen(false)}
                        onSelectOrder={(o) => {
                          setLastOrder(o)
                          setSheetMode(o.status === 'PAID' ? 'receipt' : 'payment')
                        }}
                        onCancelOrder={cancelOrder}
                      />
                    ) : null}

                    {orderStatus ? (
                      <div className={`status ${orderStatus.includes('berhasil') || orderStatus.includes('dibatalkan') ? '' : 'error'}`}>{orderStatus}</div>
                    ) : null}
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
