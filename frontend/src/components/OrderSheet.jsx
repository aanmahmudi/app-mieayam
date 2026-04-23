import { rupiah } from '../lib/format'

export function OrderSheet({ cartCount, cartEntries, ordering, onSetQuantity, onSubmitOrder, lastOrder, onGoToPayment }) {
  if (cartCount) {
    return (
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
                  <button className="qtyBtn" type="button" onClick={() => onSetQuantity(e.item, e.quantity - 1)} disabled={ordering || e.quantity <= 0}>
                    −
                  </button>
                  <div className="qtyNum">{e.quantity}</div>
                  <button className="qtyBtn" type="button" onClick={() => onSetQuantity(e.item, e.quantity + 1)} disabled={ordering || e.quantity >= 99}>
                    +
                  </button>
                </div>
                <div className="cartSubtotal">{rupiah.format(e.quantity * e.item.price)}</div>
              </div>
            ))}
        </div>
        <button className="button primary cartSubmit" type="button" onClick={onSubmitOrder} disabled={ordering}>
          {ordering ? 'Memproses...' : 'Buat Pesanan'}
        </button>
      </>
    )
  }

  if (lastOrder?.items?.length) {
    return (
      <div className="orderSummary">
        <div className="receiptItems">
          {lastOrder.items.map((it) => (
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
        </div>
        <button className="button primary cartSubmit" type="button" onClick={onGoToPayment}>
          Lanjut ke Pembayaran
        </button>
      </div>
    )
  }

  return <div className="payHint">Pilih menu dulu dengan tombol +.</div>
}

