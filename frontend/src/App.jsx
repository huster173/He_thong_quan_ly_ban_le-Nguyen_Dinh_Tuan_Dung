import { Fragment, useCallback, useEffect, useState } from 'react'
import { applyPromotion, calculateSubtotal, confirmOrder, getProducts } from './api.js'
import { IconCart, IconCheck, IconCheckCircle, IconHistory, IconWallet } from './icons.jsx'
import ProductListScreen from './screens/ProductListScreen.jsx'
import CreateOrderScreen, { NO_COUPON } from './screens/CreateOrderScreen.jsx'
import InvoiceScreen from './screens/InvoiceScreen.jsx'
import OrderHistoryScreen from './screens/OrderHistoryScreen.jsx'
import AdminLoginScreen from './screens/AdminLoginScreen.jsx'

const SCREEN = {
  PRODUCTS: 'products',
  CART: 'cart',
  INVOICE: 'invoice',
  HISTORY: 'history',
}

const LET_SERVER_PICK = null

export default function App() {
  const [screen, setScreen] = useState(SCREEN.PRODUCTS)
  const [products, setProducts] = useState([])
  const [loadError, setLoadError] = useState(null)

  const [items, setItems] = useState([])
  const [couponCode, setCouponCode] = useState(NO_COUPON)
  const [region, setRegion] = useState('noi_thanh')
  const [subtotal, setSubtotal] = useState(null)
  const [promotion, setPromotion] = useState(null)

  const [invoice, setInvoice] = useState(null)
  const [orderError, setOrderError] = useState(null)
  const [confirming, setConfirming] = useState(false)

  const [adminUnlocked, setAdminUnlocked] = useState(false)

  const refreshProducts = useCallback(async () => {
    try {
      setProducts(await getProducts())
      setLoadError(null)
    } catch (e) {
      setLoadError(e.message)
    }
  }, [])

  useEffect(() => {
    refreshProducts()
  }, [refreshProducts])

  const addItem = (productId, quantity) => {
    setOrderError(null)
    const stock = products.find((p) => p.id === productId)?.stock ?? 0
    setItems((prev) => {
      const existing = prev.find((i) => i.productId === productId)
      const wanted = (existing?.quantity ?? 0) + quantity
      const capped = Math.min(wanted, stock)
      if (capped < 1) return prev
      if (existing) {
        return prev.map((i) => (i.productId === productId ? { ...i, quantity: capped } : i))
      }
      return [...prev, { productId, quantity: capped }]
    })
  }

  const changeQuantity = (productId, quantity) => {
    setOrderError(null)
    setItems((prev) => prev.map((i) => (i.productId === productId ? { ...i, quantity } : i)))
  }

  const removeItem = (productId) => {
    setOrderError(null)
    setItems((prev) => prev.filter((i) => i.productId !== productId))
  }

  const goToCart = async () => {
    setOrderError(null)
    try {
      const subtotalResult = await calculateSubtotal(items)
      const promotionResult = await applyPromotion(items, LET_SERVER_PICK)
      setSubtotal(subtotalResult)
      setPromotion(promotionResult)
      setCouponCode(promotionResult.couponCode ?? NO_COUPON)
      setScreen(SCREEN.CART)
    } catch (error) {
      setOrderError(error)
      setScreen(SCREEN.CART)
    }
  }

  const changeCoupon = async (code) => {
    const result = await applyPromotion(items, code)
    setCouponCode(code)
    setPromotion(result)
    return result
  }

  const handleConfirm = async () => {
    setConfirming(true)
    setOrderError(null)
    try {
      const result = await confirmOrder(items, couponCode, region)
      setInvoice(result)
      setScreen(SCREEN.INVOICE)
    } catch (e) {
      setOrderError(e)
      if (e.code === 'OUT_OF_STOCK') {
        await shrinkCartToAvailable(e.details)
      }
    } finally {
      setConfirming(false)
      await refreshProducts()
    }
  }

  const shrinkCartToAvailable = async ({ productId, available } = {}) => {
    if (!productId || available === undefined) return

    const adjusted = items
      .map((item) => (item.productId === productId ? { ...item, quantity: available } : item))
      .filter((item) => item.quantity > 0)
    setItems(adjusted)

    if (adjusted.length === 0) {
      setSubtotal(null)
      setPromotion(null)
      return
    }
    try {
      setSubtotal(await calculateSubtotal(adjusted))
      setPromotion(await applyPromotion(adjusted, couponCode))
    } catch {
    }
  }

  const backToProducts = async () => {
    setSubtotal(null)
    setPromotion(null)
    setCouponCode(NO_COUPON)
    setOrderError(null)
    setScreen(SCREEN.PRODUCTS)
    await refreshProducts()
  }

  const startNewOrder = async () => {
    setItems([])
    setCouponCode(NO_COUPON)
    setSubtotal(null)
    setPromotion(null)
    setRegion('noi_thanh')
    setInvoice(null)
    setOrderError(null)
    setScreen(SCREEN.PRODUCTS)
    await refreshProducts()
  }

  const leaveAdmin = () => setScreen(invoice ? SCREEN.INVOICE : SCREEN.PRODUCTS)

  const cartCount = items.reduce((sum, i) => sum + i.quantity, 0)

  return (
    <div className={screen === SCREEN.HISTORY ? 'app app-wide' : 'app'}>
      <header className="app-head">
        <h1>Hệ thống xử lý đơn hàng trực tuyến VNShop</h1>
        {screen === SCREEN.HISTORY ? (
          <span className="crumb crumb-active">
            <span className="crumb-mark">
              <IconHistory />
            </span>
            Quản trị
          </span>
        ) : (
          <>
            <Breadcrumb screen={screen} cartCount={cartCount} />
            <button className="btn-ghost" onClick={() => setScreen(SCREEN.HISTORY)}>
              <IconHistory /> Lịch sử đơn hàng
            </button>
          </>
        )}
      </header>

      {loadError && (
        <div className="alert alert-error">
          Không gọi được backend ({loadError}). Kiểm tra <code>mvn spring-boot:run</code> ở cổng 8080.
        </div>
      )}

      {screen === SCREEN.PRODUCTS && (
        <ProductListScreen
          products={products}
          items={items}
          cartCount={cartCount}
          onAddToCart={addItem}
          onChangeQuantity={changeQuantity}
          onRemoveItem={removeItem}
          onGoToCart={goToCart}
        />
      )}

      {screen === SCREEN.CART && (
        <CreateOrderScreen
          items={items}
          subtotal={subtotal}
          promotion={promotion}
          couponCode={couponCode}
          region={region}
          confirming={confirming}
          orderError={orderError}
          onApplyPromotion={changeCoupon}
          onRegionChange={(v) => {
            setOrderError(null)
            setRegion(v)
          }}
          onConfirm={handleConfirm}
          onBack={backToProducts}
        />
      )}

      {screen === SCREEN.INVOICE && invoice && (
        <InvoiceScreen invoice={invoice} onNewOrder={startNewOrder} />
      )}

      {screen === SCREEN.HISTORY &&
        (adminUnlocked ? (
          <OrderHistoryScreen onBack={leaveAdmin} />
        ) : (
          <AdminLoginScreen onUnlock={() => setAdminUnlocked(true)} onBack={leaveAdmin} />
        ))}
    </div>
  )
}

function Breadcrumb({ screen, cartCount }) {
  const steps = [
    { key: SCREEN.PRODUCTS, label: 'Chọn sản phẩm', icon: IconCart, badge: cartCount || null },
    { key: SCREEN.CART, label: 'Tính tiền', icon: IconWallet },
    { key: SCREEN.INVOICE, label: 'Xác nhận', icon: IconCheckCircle },
  ]
  const current = steps.findIndex((step) => step.key === screen)

  return (
    <nav className="breadcrumb" aria-label="Các bước đặt hàng">
      {steps.map((step, i) => {
        const state = i < current ? 'done' : i === current ? 'active' : 'todo'
        const Icon = step.icon
        return (
          <Fragment key={step.key}>
            {i > 0 && (
              <span className="crumb-sep" aria-hidden="true">
                →
              </span>
            )}
            <span className={`crumb crumb-${state}`} aria-current={state === 'active' ? 'step' : undefined}>
              <span className="crumb-mark">{state === 'done' ? <IconCheck /> : <Icon />}</span>
              {step.label}
            </span>
          </Fragment>
        )
      })}
    </nav>
  )
}
