import { formatDateTime, rupiah } from '../lib/format'
import { useEffect, useMemo, useRef, useState } from 'react'
import { apiFetch } from '../lib/api'

const CONTACT_STORAGE_KEY = 'mieayam_receipt_contacts'

function readSavedContacts(username) {
  if (!username) return { email: '', whatsapp: '' }
  try {
    const raw = localStorage.getItem(CONTACT_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : {}
    const saved = parsed?.[username]
    return {
      email: typeof saved?.email === 'string' ? saved.email : '',
      whatsapp: typeof saved?.whatsapp === 'string' ? saved.whatsapp : '',
    }
  } catch {
    return { email: '', whatsapp: '' }
  }
}

function saveContacts(username, email, whatsapp) {
  if (!username) return
  try {
    const raw = localStorage.getItem(CONTACT_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : {}
    parsed[username] = { email, whatsapp }
    localStorage.setItem(CONTACT_STORAGE_KEY, JSON.stringify(parsed))
  } catch {
    // ignore localStorage failures
  }
}

export function ReceiptSheet({ token, currentUsername, lastOrder, paymentMethod, onDone }) {
  if (!lastOrder) return null

  const [shareEmail, setShareEmail] = useState(() => readSavedContacts(currentUsername).email)
  const [shareWhatsApp, setShareWhatsApp] = useState(() => readSavedContacts(currentUsername).whatsapp)
  const [shareStatus, setShareStatus] = useState('')
  const [shareStatusError, setShareStatusError] = useState(false)
  const [shareSending, setShareSending] = useState(false)
  const emailTouchedRef = useRef(false)
  const whatsappTouchedRef = useRef(false)

  useEffect(() => {
    const saved = readSavedContacts(currentUsername)
    if (!emailTouchedRef.current) setShareEmail(saved.email)
    if (!whatsappTouchedRef.current) setShareWhatsApp(saved.whatsapp)
    setShareStatus('')
    setShareStatusError(false)
  }, [currentUsername])

  const receiptText = useMemo(() => {
    const createdAt = lastOrder?.createdAt ? formatDateTime(lastOrder.createdAt) : ''
    const lines = []
    lines.push('Struk Pembayaran - Aplikasi Mie Ayam')
    if (lastOrder?.id != null) lines.push(`Order #${lastOrder.id}`)
    if (createdAt) lines.push(createdAt)
    lines.push('')
    lines.push('Rincian:')
    for (const it of lastOrder?.items ?? []) {
      lines.push(`- ${it.name} (${it.quantity} x ${rupiah.format(it.priceEach)}) = ${rupiah.format(it.subtotal)}`)
    }
    lines.push('')
    lines.push(`Total: ${rupiah.format(lastOrder?.total ?? 0)}`)
    lines.push(`Status: ${lastOrder?.status === 'PAID' ? 'Lunas' : 'Belum dibayar'}`)
    const method = lastOrder?.paymentMethod ?? paymentMethod
    if (method) lines.push(`Metode: ${method}`)
    if (method === 'BANK' && lastOrder?.bank) lines.push(`Rekening: ${lastOrder.bank}`)
    if (lastOrder?.status === 'PAID') {
      lines.push(`Dibayar: ${rupiah.format(lastOrder?.amountPaid ?? 0)}`)
      lines.push(`Kembalian: ${rupiah.format(lastOrder?.changeAmount ?? 0)}`)
    }
    lines.push('')
    lines.push('Terima kasih.')
    return lines.join('\n')
  }, [lastOrder, paymentMethod])

  const handleDone = async () => {
    if (shareSending) return

    const email = (shareEmail ?? '').trim()
    const whatsapp = (shareWhatsApp ?? '').trim()

    setShareSending(true)
    setShareStatus('')
    setShareStatusError(false)

    try {
      saveContacts(currentUsername, email, whatsapp)

      if (email) {
        await apiFetch(`/api/orders/${lastOrder.id}/share-receipt`, { token, method: 'POST', body: { email, whatsapp: '' } })
      }

      if (whatsapp) {
        const digits = String(whatsapp).replace(/\D/g, '')
        let normalized = digits
        if (normalized.startsWith('0')) normalized = `62${normalized.slice(1)}`
        else if (!normalized.startsWith('62') && normalized.length >= 9) normalized = `62${normalized}`
        const text = encodeURIComponent(receiptText)
        const url = normalized ? `https://wa.me/${normalized}?text=${text}` : `https://wa.me/?text=${text}`
        window.open(url, '_blank', 'noopener,noreferrer')
      }

      await onDone()
    } catch (err) {
      setShareStatus(err.message)
      setShareStatusError(true)
      return
    } finally {
      setShareSending(false)
    }
  }

  return (
    <div className="receipt">
      <div className="receiptHeader">
        <div className="receiptTitle">Struk Pembayaran</div>
        <div className="receiptMeta">{formatDateTime(lastOrder.createdAt)}</div>
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
        {lastOrder.status !== 'PAID' ? (
          <div className="receiptTotalRow">
            <div>Metode</div>
            <div className="receiptStrong">{lastOrder.paymentMethod ?? paymentMethod ?? '-'}</div>
          </div>
        ) : null}
        {lastOrder.status === 'PAID' ? (
          <>
            <div className="receiptTotalRow">
              <div>Metode</div>
              <div className="receiptStrong">{lastOrder.paymentMethod ?? '-'}</div>
            </div>
            {lastOrder.paymentMethod === 'BANK' && lastOrder.bank ? (
              <div className="receiptTotalRow">
                <div>Rekening</div>
                <div className="receiptStrong">{lastOrder.bank}</div>
              </div>
            ) : null}
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
      <div className="receiptShare">
        <div className="receiptShareTitle">Kirim Otomatis Saat Selesai</div>
        <input
          className="input"
          placeholder="Email tujuan (opsional)"
          value={shareEmail}
          onChange={(e) => {
            emailTouchedRef.current = true
            setShareEmail(e.target.value)
          }}
          disabled={shareSending}
        />
        <input
          className="input"
          placeholder="No WhatsApp (opsional, contoh: 62812xxxx)"
          value={shareWhatsApp}
          onChange={(e) => {
            whatsappTouchedRef.current = true
            setShareWhatsApp(e.target.value)
          }}
          disabled={shareSending}
        />
        <div className="receiptShareHint">Email dan nomor WhatsApp akan disimpan per akun. Email dikirim sebagai lampiran PDF. Untuk WhatsApp biasa, chat tetap dibuka otomatis karena tidak bisa auto-send file PDF tanpa WhatsApp Business API.</div>
        {shareStatus ? <div className={`status ${shareStatusError ? 'error' : ''}`}>{shareStatus}</div> : null}
      </div>
      <button className="button primary receiptDone" type="button" onClick={handleDone} disabled={shareSending}>
        {shareSending ? 'Memproses...' : 'Selesai'}
      </button>
    </div>
  )
}
