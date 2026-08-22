import { regionLabel, vnd, percent } from '../format.js'
import { IconCart, IconCheck, IconCheckCircle, IconReceipt } from '../icons.jsx'


export default function InvoiceScreen({ invoice, onNewOrder }) {
  return (
    <section className="panel invoice">
      <header className="panel-head">
        <h2>
          <IconReceipt /> Hoá đơn {invoice.orderId && <span className="mono">{invoice.orderId}</span>}
        </h2>
        <span className="tag tag-done">
          <IconCheck /> Đã xác nhận
        </span>
      </header>

      <div className="stage">
        {invoice.lines.map((line) => (
          <div key={line.productId} className="invoice-line">
            <div className="invoice-line-name">
              <span className="mono">{line.productId}</span> {line.name}
            </div>
            <div className="invoice-line-calc">
              <span className="muted">
                {vnd(line.unitPrice)} × {line.quantity}
              </span>
              <span className="num">{vnd(line.lineTotal)}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="stage">
        <Row label="Tạm tính" value={vnd(invoice.subtotal)} />
        <Row
          label="Ưu đãi theo số lượng"
          value={Number(invoice.categoryDiscount) > 0 ? `− ${vnd(invoice.categoryDiscount)}` : vnd(0)}
          tone={Number(invoice.categoryDiscount) > 0 ? 'discount' : undefined}
        />
        <Row
          label={`Mã giảm giá${invoice.couponCode ? ` ${invoice.couponCode}` : ''}`}
          value={Number(invoice.couponDiscount) > 0 ? `− ${vnd(invoice.couponDiscount)}` : vnd(0)}
          tone={Number(invoice.couponDiscount) > 0 ? 'discount' : undefined}
        />
        <Row label="Tổng trước thuế" value={vnd(invoice.total)} strong />
      </div>

      <div className="stage">
        <Row label="Khu vực giao hàng" value={regionLabel(invoice.region)} />
        <Row label="Thuế suất" value={percent(invoice.taxRate)} />
        <Row label="Tiền thuế" value={vnd(invoice.taxAmount)} />
      </div>

      <Row label="TỔNG THANH TOÁN" value={vnd(invoice.finalTotal)} total />

      <div className="alert alert-ok">
        {invoice.stockUpdated ? (
          <>
            <IconCheckCircle /> Đơn hàng đã được xác nhận và tồn kho đã được cập nhật.
          </>
        ) : (
          'Đơn hàng chưa được xác nhận — tồn kho giữ nguyên.'
        )}
      </div>

      <button className="btn-primary" onClick={onNewOrder}>
        <IconCart /> Đơn hàng mới
      </button>
    </section>
  )
}

function Row({ label, value, strong, total, tone }) {
  const className = ['row', strong && 'row-strong', total && 'row-total', tone && `row-${tone}`]
    .filter(Boolean)
    .join(' ')
  return (
    <div className={className}>
      <span>{label}</span>
      <span className="num">{value}</span>
    </div>
  )
}
