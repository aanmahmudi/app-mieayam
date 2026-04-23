import { rupiah } from '../lib/format'

export function MenuList({ items, cart, ordering, showUnit, onSetQuantity }) {
  return (
    <div className="menuList">
      {items.map((it) => (
        <div className="menuRow" key={it.id}>
          <div className="menuLeft">
            {it.imageUrl ? <img className="menuThumb" alt={it.name} src={encodeURI(it.imageUrl)} loading="lazy" /> : null}
            <div>
              <div className="menuName">{it.name}</div>
              {showUnit && it.unit ? <div className="menuUnit">{it.unit}</div> : null}
            </div>
          </div>
          <div className="menuRight">
            <div className="menuPrice">{rupiah.format(it.price)}</div>
            <div className="qtyControl">
              <button
                className="qtyBtn"
                type="button"
                onClick={() => onSetQuantity(it, (cart[it.id]?.quantity ?? 0) - 1)}
                disabled={ordering || (cart[it.id]?.quantity ?? 0) <= 0}
              >
                −
              </button>
              <div className="qtyNum">{cart[it.id]?.quantity ?? 0}</div>
              <button
                className="qtyBtn"
                type="button"
                onClick={() => onSetQuantity(it, (cart[it.id]?.quantity ?? 0) + 1)}
                disabled={ordering || (cart[it.id]?.quantity ?? 0) >= 99}
              >
                +
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

