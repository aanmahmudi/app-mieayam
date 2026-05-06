import { formatDateTime, rupiah } from '../lib/format'

export function HistorySheet({ orderHistory, loadingHistory, errorHistory, onRefresh, onClose, onSelectOrder, onCancelOrder }) {
  return (
    <div className="history">
      <div className="receiptHeader">
        <div className="receiptTitle">Riwayat</div>
        <div className="sheetHeaderActions">
          <button className="button" type="button" onClick={onRefresh} disabled={loadingHistory}>
            {loadingHistory ? 'Memuat...' : 'Refresh'}
          </button>
          <button className="iconButton" type="button" onClick={onClose} aria-label="Tutup">
            <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
        </div>
      </div>
      {errorHistory ? <div className="status error">{errorHistory}</div> : null}
      {!loadingHistory && !orderHistory.length ? <div className="payHint">Belum ada riwayat pesanan.</div> : null}
      <div className="historyList">
        {orderHistory.map((o) => (
          <div className="historyCard" key={o.id}>
            <div className="historyTop">
              <div className="historyTopLeft">
                <div className="historyOrderId">Pesanan</div>
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
                <button className="button danger" type="button" onClick={() => onCancelOrder(o.id)}>
                  Batalkan
                </button>
              ) : null}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
