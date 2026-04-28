import { formatDateTime, rupiah } from '../lib/format'

export function PaymentSheet({
  pendingPayment,
  lastOrder,
  paymentMethod,
  setPaymentMethod,
  cashPaid,
  setCashPaid,
  paying,
  walletBalance,
  walletTxs,
  bankChoice,
  setBankChoice,
  onPay,
  onCancel,
}) {
  if (!pendingPayment) return null

  return (
    <div className="payWrap">
      <div className="paySummary">
        <div className="payLabel">Total</div>
        <div className="payValue">{rupiah.format(lastOrder?.total ?? 0)}</div>
      </div>
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
          <div className="txTitle">Mutasi (terakhir)</div>
          <div className="txList">
            {walletTxs.length ? (
              walletTxs.map((tx) => (
                <div className="txRow" key={tx.id}>
                  <div className="txLeft">
                    <div className="txType">{tx.type === 'TOP_UP' ? 'Top up' : tx.type === 'REFUND' ? 'Refund' : 'Pembayaran'}</div>
                    {tx.orderId ? (
                      <div className="txMeta">{formatDateTime(tx.createdAt)}</div>
                    ) : (
                      <div className="txMeta">Saldo: {rupiah.format(tx.balanceAfter)}</div>
                    )}
                  </div>
                  <div className={`txAmount ${tx.amount < 0 ? 'neg' : 'pos'}`}>{rupiah.format(tx.amount)}</div>
                </div>
              ))
            ) : (
              null
            )}
          </div>
        </div>
      )}
      <button className="button primary cartSubmit" type="button" onClick={onPay} disabled={paying || (paymentMethod === 'BANK' && walletBalance < (lastOrder?.total ?? 0))}>
        {paying ? 'Memproses...' : 'Bayar'}
      </button>
      <button className="button" type="button" onClick={onCancel} disabled={paying}>
        Batalkan Pesanan
      </button>
    </div>
  )
}
