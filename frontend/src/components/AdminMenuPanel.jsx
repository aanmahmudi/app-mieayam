import { rupiah } from '../lib/format'

export function AdminMenuPanel({
  errorAdminMenu,
  menuDraft,
  setMenuDraft,
  savingMenu,
  onSaveMenuItem,
  onRefreshMenu,
  adminMenuItems,
  loadingAdminMenu,
  onDeleteMenuItem,
}) {
  return (
    <div className="adminPanel">
      <div className="receiptHeader">
        <div className="receiptTitle">Admin Menu</div>
        <button className="button" type="button" onClick={onRefreshMenu} disabled={loadingAdminMenu}>
          {loadingAdminMenu ? 'Memuat...' : 'Refresh'}
        </button>
      </div>
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
    </div>
  )
}

