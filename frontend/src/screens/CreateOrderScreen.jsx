import { useState } from 'react'
import { REGIONS, vnd } from '../format.js'
import {
  IconAlert,
  IconArrowLeft,
  IconCheckCircle,
  IconPercent,
  IconReceipt,
  IconTag,
  IconTruck,
  IconWallet,
} from '../icons.jsx'

export const NO_COUPON = 'NONE'

const VOUCHERS = [
  { code: NO_COUPON, label: 'Không dùng voucher' },
  {
    code: 'SALE50K',
    label: 'SALE50K — giảm 50.000đ',
    requirement: 'Cần tổng tiền từ 300.000đ (sau ưu đãi theo số lượng) mới dùng được SALE50K.',
  },
  { code: 'SALE10PT', label: 'SALE10PT — giảm 10%' },
]

const ERROR_TITLES = {
  OUT_OF_STOCK: 'Sản phẩm vừa hết hàng',
  PRODUCT_NOT_FOUND: 'Sản phẩm không còn tồn tại',
  EMPTY_ORDER: 'Giỏ hàng đang trống',
  INVALID_QUANTITY: 'Số lượng không hợp lệ',
  INVALID_COUPON: 'Mã giảm giá không hợp lệ',
  INVALID_REGION: 'Khu vực giao hàng không hợp lệ',
  NETWORK_ERROR: 'Mất kết nối',
  INTERNAL_ERROR: 'Hệ thống đang gặp sự cố',
}

export default function CreateOrderScreen({
  items,
  subtotal,
  promotion,
  couponCode,
  region,
  confirming,
  orderError,
  onApplyPromotion,
  onRegionChange,
  onConfirm,
  onBack,
}) {
  const [selectedVoucher, setSelectedVoucher] = useState(couponCode)
  const [voucherError, setVoucherError] = useState(null)
  const [applyingVoucher, setApplyingVoucher] = useState(false)

  const applyVoucher = async () => {
    setVoucherError(null)
    setApplyingVoucher(true)
    try {
      await onApplyPromotion(selectedVoucher)
    } catch (error) {
      setVoucherError(error.message)
    } finally {
      setApplyingVoucher(false)
    }
  }

  const pricing = promotion ?? subtotal

  const totalQuantity = (subtotal?.lines ?? []).reduce((sum, line) => sum + line.quantity, 0)

  const rejectedVoucher =
    promotion && couponCode && couponCode !== NO_COUPON && promotion.couponCode !== couponCode
      ? VOUCHERS.find((voucher) => voucher.code === couponCode)
      : null

  return (
    <section className="panel">
      <header className="panel-head">
        <h2>
          <IconWallet /> Thanh toán
        </h2>
        <button className="btn-ghost" onClick={onBack}>
          <IconArrowLeft /> Quay lại giỏ hàng
        </button>
      </header>

      <div className="stage">
        <div className="stage-label">
          <IconReceipt /> Đơn hàng của bạn
        </div>
        {subtotal ? (
          <table className="table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th className="num">Đơn giá</th>
                <th className="num">SL</th>
                <th className="num">Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              {subtotal.lines.map((line) => (
                <tr key={line.productId}>
                  <td>
                    {line.name} <span className="muted mono">{line.productId}</span>
                  </td>
                  <td className="num">{vnd(line.unitPrice)}</td>
                  <td className="num">{line.quantity}</td>
                  <td className="num">{vnd(line.lineTotal)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="table-total">
                <td colSpan={3}>Tiền hàng ({totalQuantity} sản phẩm)</td>
                <td className="num">{vnd(subtotal.subtotal)}</td>
              </tr>
            </tfoot>
          </table>
        ) : (
          <p className="empty">Không thể tải thông tin đơn hàng. Hãy quay lại giỏ hàng.</p>
        )}
      </div>

      {pricing && (
        <div className="stage">
          <div className="stage-label">
            <IconPercent /> Ưu đãi theo số lượng
          </div>
          <Row
            label="Giảm tự động"
            value={Number(pricing.categoryDiscount) ? `− ${vnd(pricing.categoryDiscount)}` : 'Chưa đủ điều kiện'}
            tone={Number(pricing.categoryDiscount) ? 'discount' : undefined}
          />
          <div className="sub-row">Mua từ 3 sản phẩm cùng loại được giảm 10% cho loại đó.</div>
        </div>
      )}

      <div className="stage">
        <div className="stage-label">
          <IconTag /> Mã giảm giá
        </div>
        <div className="coupon-row">
          <select
            aria-label="Chọn mã giảm giá"
            value={selectedVoucher}
            onChange={(event) => {
              setSelectedVoucher(event.target.value)
              setVoucherError(null)
            }}
          >
            {VOUCHERS.map((voucher) => (
              <option key={voucher.code} value={voucher.code}>
                {voucher.label}
              </option>
            ))}
          </select>
          <button className="btn-secondary" onClick={applyVoucher} disabled={!subtotal || applyingVoucher}>
            {applyingVoucher ? 'Đang áp dụng…' : 'Áp dụng'}
          </button>
        </div>
        {promotion?.couponCode ? (
          <p className="hint">
            Mã giảm giá đang áp dụng: <strong>{promotion.couponCode}</strong>
          </p>
        ) : (
          <p className="hint">Đơn hàng hiện chưa áp dụng mã giảm giá nào.</p>
        )}
        {selectedVoucher !== couponCode && (
          <p className="hint">Bấm “Áp dụng” để dùng mã giảm giá vừa chọn.</p>
        )}
        {rejectedVoucher && (
          <div className="alert alert-warn">
            Chưa áp dụng được {rejectedVoucher.code}. {rejectedVoucher.requirement}
          </div>
        )}
        {voucherError && <div className="alert alert-error">{voucherError}</div>}
      </div>

      {pricing && (
        <div className="stage summary">
          <div className="stage-label">
            <IconWallet /> Tổng thanh toán
          </div>
          <Row label="Tạm tính" value={vnd(pricing.subtotal)} />
          <Row
            label="Ưu đãi theo số lượng"
            value={Number(pricing.categoryDiscount) ? `− ${vnd(pricing.categoryDiscount)}` : vnd(0)}
            tone={Number(pricing.categoryDiscount) ? 'discount' : undefined}
          />
          <Row
            label={`Voucher${pricing.couponCode ? ` ${pricing.couponCode}` : ''}`}
            value={Number(pricing.couponDiscount) ? `− ${vnd(pricing.couponDiscount)}` : vnd(0)}
            tone={Number(pricing.couponDiscount) ? 'discount' : undefined}
          />
          <Row label="Tổng trước thuế" value={vnd(pricing.total ?? pricing.subtotal)} strong />
        </div>
      )}

      {/* Khu vực đứng sau tổng trước thuế vì nó quyết định thuế suất — bước cuối
          trước khi chốt đơn. */}
      <div className="stage">
        <div className="stage-label">
          <IconTruck /> Giao hàng
        </div>
        <label className="field" htmlFor="region">
          Khu vực giao hàng
          <select id="region" value={region} onChange={(event) => onRegionChange(event.target.value)}>
            {REGIONS.map((item) => (
              <option key={item.code} value={item.code}>
                {item.label}
              </option>
            ))}
          </select>
        </label>
        <p className="hint">Thuế và tổng tiền cuối cùng sẽ hiển thị sau khi xác nhận đơn hàng.</p>
      </div>

      {orderError && (
        <div className="alert alert-error">
          <strong>
            <IconAlert /> {ERROR_TITLES[orderError.code] ?? 'Không xác nhận được đơn hàng'}
          </strong>
          <p>{orderError.message}</p>
          {orderError.code === 'OUT_OF_STOCK' && (
            <p>Giỏ hàng đã được cập nhật theo số lượng còn lại. Kiểm tra lại rồi xác nhận.</p>
          )}
        </div>
      )}

      <button className="btn-primary" onClick={onConfirm} disabled={!subtotal || items.length === 0 || confirming}>
        {confirming ? 'Đang xác nhận…' : <><IconCheckCircle /> Xác nhận đơn hàng</>}
      </button>
      <p className="hint">Kho chỉ được cập nhật khi đơn hàng được xác nhận thành công.</p>
    </section>
  )
}

function Row({ label, value, strong, tone }) {
  const className = ['row', strong && 'row-strong', tone && `row-${tone}`].filter(Boolean).join(' ')
  return (
    <div className={className}>
      <span>{label}</span>
      <span className="num">{value}</span>
    </div>
  )
}
