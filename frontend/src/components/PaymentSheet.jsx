import { rupiah } from '../lib/format'

export function PaymentSheet({
  pendingPayment,
  lastOrder,
  paymentMethod,
  setPaymentMethod,
  cashPaid,
  setCashPaid,
  paying,
  walletBalance,
  bankChoice,
  setBankChoice,
  onPay,
  onCancel,
}) {
  if (!pendingPayment) {
    return <div className="payHint">Memproses...</div>
  }

  return (
    <div className="payWrap">
      <div className="paySummary">
        <div className="payLabel">Total</div>
        <div className="payValue">{rupiah.format(lastOrder?.total ?? 0)}</div>
      </div>
      {(lastOrder?.items?.length ?? 0) ? (
        <div className="payOrderDetails">
          <div className="label">Rincian Pesanan</div>
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
        </div>
      ) : null}
      <div className="payMethods">
        <button
          type="button"
          className={`payMethodBtn ${paymentMethod === 'CASH' ? 'active' : ''}`}
          onClick={() => {
            setPaymentMethod('CASH')
            setCashPaid('')
          }}
          disabled={paying}
        >
          Cash
        </button>
        <button type="button" className={`payMethodBtn ${paymentMethod === 'QRIS' ? 'active' : ''}`} onClick={() => setPaymentMethod('QRIS')} disabled={paying}>
          QRIS
        </button>
        <button type="button" className={`payMethodBtn ${paymentMethod === 'BANK' ? 'active' : ''}`} onClick={() => setPaymentMethod('BANK')} disabled={paying}>
          Bank
        </button>
      </div>
      {paymentMethod === 'CASH' ? (
        <div className="payCash">
          <div className="label">Pembayaran</div>
          <input className="input" placeholder="Pembayaran di kasir / cash" value="Pembayaran di kasir / cash" readOnly disabled />
          <input type="hidden" value={cashPaid} readOnly />
        </div>
      ) : paymentMethod === 'QRIS' ? (
        null
      ) : (
        <div className="payBank">
          <div className="paySummary">
            <div className="payLabel">Saldo</div>
            <div className="payValue">{rupiah.format(walletBalance)}</div>
          </div>
          <div className="label">Rekening tujuan</div>
          <select className="input" value={bankChoice} onChange={(e) => setBankChoice(e.target.value)} disabled={paying}>
            <option value="BRI">BRI</option>
            <option value="MANDIRI">Mandiri</option>
            <option value="BCA">BCA</option>
          </select>
        </div>
      )}
      <button className="button primary cartSubmit" type="button" onClick={onPay} disabled={paying || (paymentMethod === 'BANK' && walletBalance < (lastOrder?.total ?? 0))}>
        {paying ? 'Memproses...' : 'Bayar'}
      </button>
      <button className="button danger payCancel" type="button" onClick={onCancel} disabled={paying}>
        Batalkan Pesanan
      </button>
    </div>
  )
}
