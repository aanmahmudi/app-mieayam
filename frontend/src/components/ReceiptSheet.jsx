import { formatDateTime, rupiah } from '../lib/format'

export function ReceiptSheet({ lastOrder, paymentMethod, onDone }) {
  if (!lastOrder) return null

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
      <button className="button primary receiptDone" type="button" onClick={onDone}>
        Selesai
      </button>
    </div>
  )
}

