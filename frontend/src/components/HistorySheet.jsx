import { formatDateTime, rupiah } from '../lib/format'

export function HistorySheet({ orderHistory, loadingHistory, errorHistory, onRefresh, onSelectOrder, onCancelOrder }) {
  return (
    <div className="history">
      <div className="receiptHeader">
        <div className="receiptTitle">Riwayat Pesanan</div>
        <button className="button" type="button" onClick={onRefresh} disabled={loadingHistory}>
          {loadingHistory ? 'Memuat...' : 'Refresh'}
        </button>
      </div>
      {errorHistory ? <div className="status error">{errorHistory}</div> : null}
      {!loadingHistory && !orderHistory.length ? <div className="payHint">Belum ada riwayat pesanan.</div> : null}
      <div className="historyList">
        {orderHistory.map((o, index) => {
          const seq = orderHistory.length - index
          return (
            <div className="historyCard" key={o.id}>
              <div className="historyTop">
                <div className="historyTopLeft">
                  <div className="historyOrderId">Pesanan #{seq}</div>
                  <div className="historyWhen">{formatDateTime(o.createdAt)}</div>
                </div>
                <div className={`historyBadge ${o.status === 'PAID' ? 'paid' : 'pending'}`}>{o.status === 'PAID' ? 'Lunas' : 'Belum dibayar'}</div>
              </div>
              <div className="historyMid">
                <div className="historyAmount">{rupiah.format(o.total)}</div>
                <div className="historyMetaLine">
                  {(o.items?.length ?? 0) ? `${o.items.length} item` : 'Tanpa item'} {o.paymentMethod ? `• ${o.paymentMethod}` : null}
                </div>
              </div>
              <div className="historyActions">
                <button className="button primary" type="button" onClick={() => onSelectOrder(o)}>
                  {o.status === 'PAID' ? 'Lihat Struk' : 'Bayar'}
                </button>
                {o.status !== 'PAID' ? (
                  <button className="button" type="button" onClick={() => onCancelOrder(o.id)}>
                    Batalkan
                  </button>
                ) : null}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

