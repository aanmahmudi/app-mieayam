import { formatDateTime, rupiah } from '../lib/format'

export function AdminMenuPanel({
  adminTab,
  setAdminTab,
  errorAdminMenu,
  menuDraft,
  setMenuDraft,
  savingMenu,
  onSaveMenuItem,
  onRefreshMenu,
  adminMenuItems,
  loadingAdminMenu,
  onDeleteMenuItem,
  weeklyStats,
  recentOrders,
  loadingReport,
  errorReport,
  onRefreshReport,
}) {
  const formatDateOnly = (value) => {
    if (!value) return ''
    const d = new Date(value)
    if (Number.isNaN(d.getTime())) return ''
    return d.toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' })
  }

  const weekRangeLabel = (weekStart) => {
    const start = new Date(weekStart)
    if (Number.isNaN(start.getTime())) return ''
    const end = new Date(start)
    end.setDate(end.getDate() + 6)
    return `${formatDateOnly(start)} – ${formatDateOnly(end)}`
  }

  return (
    <div className="adminPanel">
      <div className="receiptHeader">
        <div className="receiptTitle">Admin</div>
        <button
          className="button"
          type="button"
          onClick={adminTab === 'menu' ? onRefreshMenu : onRefreshReport}
          disabled={adminTab === 'menu' ? loadingAdminMenu : loadingReport}
        >
          {adminTab === 'menu' ? (loadingAdminMenu ? 'Memuat...' : 'Refresh') : loadingReport ? 'Memuat...' : 'Refresh'}
        </button>
      </div>
      <div className="payMethods">
        <button type="button" className={`payMethodBtn ${adminTab === 'report' ? 'active' : ''}`} onClick={() => setAdminTab('report')}>
          Laporan
        </button>
        <button type="button" className={`payMethodBtn ${adminTab === 'menu' ? 'active' : ''}`} onClick={() => setAdminTab('menu')}>
          Menu
        </button>
      </div>

      {adminTab === 'report' ? (
        <>
          {errorReport ? <div className="status error">{errorReport}</div> : null}

          <div className="label">Ringkasan Mingguan</div>
          {loadingReport ? <div className="payHint">Memuat...</div> : null}
          {!loadingReport && !weeklyStats?.length ? <div className="payHint">Belum ada data.</div> : null}
          <div className="historyList">
            {(weeklyStats ?? []).map((w) => (
              <div className="historyCard" key={w.weekStart}>
                <div className="historyTop">
                  <div className="historyTopLeft">
                    <div className="historyOrderId">{weekRangeLabel(w.weekStart)}</div>
                    <div className="historyWhen">{formatDateTime(w.weekStart)}</div>
                  </div>
                </div>
                <div className="historyMid">
                  <div className="historyAmount">{rupiah.format(w.paidTotal ?? 0)}</div>
                  <div className="historyMetaLine">
                    {w.totalOrders ?? 0} pesanan • {w.paidOrders ?? 0} lunas
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="label">Pesanan Terbaru</div>
          {!loadingReport && !recentOrders?.length ? <div className="payHint">Belum ada pesanan.</div> : null}
          <div className="historyList">
            {(recentOrders ?? []).slice(0, 30).map((o) => (
              <div className="historyCard" key={o.id ?? String(o.createdAt) + (o.username ?? '')}>
                <div className="historyTop">
                  <div className="historyTopLeft">
                    <div className="historyOrderId">{o.username ?? 'User'}</div>
                    <div className="historyWhen">{formatDateTime(o.createdAt)}</div>
                  </div>
                  <div className={`historyBadge ${o.status === 'PAID' ? 'paid' : 'pending'}`}>{o.status === 'PAID' ? 'Lunas' : 'Belum dibayar'}</div>
                </div>
                <div className="historyMid">
                  <div className="historyAmount">{rupiah.format(o.total ?? 0)}</div>
                  <div className="historyMetaLine">
                    {o.paymentMethod ? o.paymentMethod : ''}
                    {o.paymentMethod === 'BANK' && o.bank ? ` • ${o.bank}` : ''}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      ) : (
        <>
          {errorAdminMenu ? <div className="status error">{errorAdminMenu}</div> : null}
          <div className="adminMenuForm">
            <div className="label">{menuDraft.id ? 'Edit Menu' : 'Tambah Menu'}</div>
            <input
              className="input"
              placeholder="Nama menu"
              value={menuDraft.name}
              onChange={(e) => setMenuDraft((p) => ({ ...p, name: e.target.value }))}
              disabled={savingMenu}
            />
            <div className="adminMenuGrid">
              <select
                className="input"
                value={menuDraft.category}
                onChange={(e) => setMenuDraft((p) => ({ ...p, category: e.target.value }))}
                disabled={savingMenu}
              >
                <option value="MAKANAN">MAKANAN</option>
                <option value="MINUMAN">MINUMAN</option>
                <option value="EXTRA">EXTRA</option>
              </select>
              <input
                className="input"
                placeholder="Unit (opsional)"
                value={menuDraft.unit}
                onChange={(e) => setMenuDraft((p) => ({ ...p, unit: e.target.value }))}
                disabled={savingMenu}
              />
            </div>
            <div className="adminMenuGrid">
              <input
                className="input"
                inputMode="numeric"
                placeholder="Harga"
                value={menuDraft.price}
                onChange={(e) => setMenuDraft((p) => ({ ...p, price: e.target.value.replace(/[^\d]/g, '') }))}
                disabled={savingMenu}
              />
              <input
                className="input"
                placeholder="Image URL (opsional)"
                value={menuDraft.imageUrl}
                onChange={(e) => setMenuDraft((p) => ({ ...p, imageUrl: e.target.value }))}
                disabled={savingMenu}
              />
            </div>
            <div className="adminMenuActions">
              <button className="button primary" type="button" onClick={onSaveMenuItem} disabled={savingMenu}>
                {savingMenu ? 'Menyimpan...' : menuDraft.id ? 'Update' : 'Tambah'}
              </button>
              {menuDraft.id ? (
                <button
                  className="button"
                  type="button"
                  onClick={() => setMenuDraft({ id: null, name: '', category: 'MAKANAN', unit: '', price: '', imageUrl: '' })}
                  disabled={savingMenu}
                >
                  Batal
                </button>
              ) : null}
            </div>
          </div>
          <div className="adminMenuList">
            {loadingAdminMenu ? <div className="payHint">Memuat menu...</div> : null}
            {!loadingAdminMenu && !adminMenuItems.length ? <div className="payHint">Menu kosong.</div> : null}
            {adminMenuItems
              .slice()
              .sort((a, b) => (a.category || '').localeCompare(b.category || '') || (a.name || '').localeCompare(b.name || ''))
              .map((it) => (
                <div className="adminMenuRow" key={it.id}>
                  <div className="adminMenuLeft">
                    <div className="adminMenuName">{it.name}</div>
                    <div className="adminMenuMeta">
                      {it.category}
                      {it.unit ? ` • ${it.unit}` : ''}
                    </div>
                  </div>
                  <div className="adminMenuRight">
                    <div className="adminMenuPrice">{rupiah.format(it.price)}</div>
                    <button
                      className="button"
                      type="button"
                      onClick={() =>
                        setMenuDraft({
                          id: it.id,
                          name: it.name ?? '',
                          category: it.category ?? 'MAKANAN',
                          unit: it.unit ?? '',
                          price: String(it.price ?? ''),
                          imageUrl: it.imageUrl ?? '',
                        })
                      }
                      disabled={savingMenu}
                    >
                      Edit
                    </button>
                    <button className="button" type="button" onClick={() => onDeleteMenuItem(it.id)} disabled={savingMenu}>
                      Hapus
                    </button>
                  </div>
                </div>
              ))}
          </div>
        </>
      )}
    </div>
  )
}
