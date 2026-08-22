import { useState } from 'react'
import { vnd } from '../format.js'
import { IconArrowRight, IconCart, IconMinus, IconPackage, IconPlus, IconTrash } from '../icons.jsx'

export default function ProductListScreen({
  products,
  items,
  cartCount,
  onAddToCart,
  onChangeQuantity,
  onRemoveItem,
  onGoToCart,
}) {
  const [quantities, setQuantities] = useState({})

  const quantityFor = (productId) => quantities[productId] ?? 1
  const selectedQuantity = (productId) =>
    items.find((item) => item.productId === productId)?.quantity ?? 0

  const addToOrder = (product) => {
    const requested = Math.max(1, Number(quantityFor(product.id)) || 1)
    const remaining = product.stock - selectedQuantity(product.id)
    if (remaining < 1) return

    onAddToCart(product.id, Math.min(requested, remaining))
    setQuantities((current) => ({ ...current, [product.id]: 1 }))
  }

  const changeItemQuantity = (productId, delta) => {
    const current = selectedQuantity(productId)
    const stock = products.find((product) => product.id === productId)?.stock ?? 0
    const next = Math.min(current + delta, stock)
    if (next < 1) return
    onChangeQuantity(productId, next)
  }

  return (
    <>
      <section className="panel">
        <header className="panel-head">
          <h2>
            <IconPackage /> Sản phẩm
          </h2>
        </header>

        <table className="table">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Danh mục</th>
              <th className="num">Giá</th>
              <th className="num">Còn lại</th>
              <th className="num">Số lượng</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => {
              const selected = selectedQuantity(product.id)
              const remaining = product.stock - selected
              const unavailable = remaining <= 0

              return (
                <tr key={product.id} className={product.stock === 0 ? 'row-muted' : undefined}>
                  <td>
                    {product.name} <span className="muted mono">{product.id}</span>
                  </td>
                  <td>
                    <span className="tag">{product.category}</span>
                  </td>
                  <td className="num">{vnd(product.price)}</td>
                  <td className="num">
                    {product.stock === 0 ? (
                      <span className="stock-out">Hết hàng</span>
                    ) : (
                      <span className="stock-ok">{remaining}</span>
                    )}
                  </td>
                  <td className="num">
                    <input
                      aria-label={`Số lượng ${product.name}`}
                      className="quantity-input"
                      type="number"
                      min="1"
                      max={Math.max(1, remaining)}
                      disabled={unavailable}
                      value={quantityFor(product.id)}
                      onChange={(event) =>
                        setQuantities((current) => ({ ...current, [product.id]: event.target.value }))
                      }
                    />
                  </td>
                  <td>
                    <button className="btn-small" disabled={unavailable} onClick={() => addToOrder(product)}>
                      <IconPlus /> Thêm vào đơn
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </section>

      <section className="panel draft-order">
        <header className="panel-head">
          <h2>
            <IconCart /> Giỏ hàng
          </h2>
          <span className="tag tag-preview">Chưa tính tiền</span>
        </header>

        {items.length === 0 ? (
          <p className="empty">Chưa có sản phẩm nào trong giỏ hàng.</p>
        ) : (
          <table className="table">
            <tbody>
              {items.map((item) => {
                const product = products.find((entry) => entry.id === item.productId)
                const stock = product?.stock ?? 0
                return (
                  <tr key={item.productId}>
                    <td>
                      {product?.name ?? item.productId} <span className="muted mono">{item.productId}</span>
                    </td>
                    <td className="qty-cell">
                      <button
                        className="btn-step"
                        aria-label="Giảm số lượng"
                        onClick={() => changeItemQuantity(item.productId, -1)}
                      >
                        <IconMinus />
                      </button>
                      <span className="qty">{item.quantity}</span>
                      <button
                        className="btn-step"
                        aria-label="Tăng số lượng"
                        disabled={item.quantity >= stock}
                        onClick={() => changeItemQuantity(item.productId, 1)}
                      >
                        <IconPlus />
                      </button>
                    </td>
                    <td>
                      <button className="btn-ghost" onClick={() => onRemoveItem(item.productId)}>
                        <IconTrash /> Xoá
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}

        <p className="hint">Giỏ hàng chưa được tính tiền và chưa làm thay đổi tồn kho.</p>
        <button className="btn-primary" onClick={onGoToCart} disabled={cartCount === 0}>
          Tiến hành thanh toán{cartCount > 0 ? ` (${cartCount} sản phẩm)` : ''} <IconArrowRight />
        </button>
      </section>
    </>
  )
}
