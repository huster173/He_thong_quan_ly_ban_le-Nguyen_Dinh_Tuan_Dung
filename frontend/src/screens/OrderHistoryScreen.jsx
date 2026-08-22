import { useCallback, useEffect, useState } from 'react'
import { getOrderDetail, getOrderHistory } from '../api.js'
import { dateTime, percent, regionLabel, vnd } from '../format.js'
import {
  IconArrowLeft,
  IconArrowRight,
  IconChevronDown,
  IconChevronUp,
  IconHistory,
} from '../icons.jsx'

const PAGE_SIZES = [10, 20, 50]

export default function OrderHistoryScreen({ onBack }) {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(PAGE_SIZES[0])
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [detail, setDetail] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setResult(await getOrderHistory(page, size))
    } catch (e) {
      setError(e.message)
      setResult(null)
    } finally {
      setLoading(false)
    }
  }, [page, size])

  useEffect(() => {
    load()
  }, [load])

  const toggleDetail = async (orderId) => {
    if (detail?.orderId === orderId) {
      setDetail(null)
      return
    }
    setDetail({ orderId, invoice: null, error: null })
    try {
      const invoice = await getOrderDetail(orderId)
      setDetail({ orderId, invoice, error: null })
    } catch (e) {
      setDetail({ orderId, invoice: null, error: e.message })
    }
  }

  const changeSize = (nextSize) => {
    setSize(nextSize)
    setPage(0)
    setDetail(null)
  }

  const goToPage = (nextPage) => {
    setPage(nextPage)
    setDetail(null)
  }

  const totalPages = result?.totalPages ?? 0
  const isEmpty = !loading && !error && (result?.items.length ?? 0) === 0

  return (
    <section className="panel">
      <header className="panel-head">
        <h2>
          <IconHistory /> Lịch sử đơn hàng
        </h2>
        <button className="btn-ghost" onClick={onBack}>
          <IconArrowLeft /> Về trang bán hàng
        </button>
      </header>

      <div className="history-toolbar">
        <span className="muted">
          {result ? `${result.totalItems} đơn` : loading ? 'Đang tải…' : '—'}
        </span>
        <label className="field-inline">
          Số dòng
          <select value={size} onChange={(event) => changeSize(Number(event.target.value))}>
            {PAGE_SIZES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {isEmpty && <p className="empty">Chưa có đơn hàng nào được xác nhận.</p>}

      {result?.items.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Thời gian</th>
              <th className="num">SL</th>
              <th>Khu vực</th>
              <th>Mã giảm giá</th>
              <th className="num">Tổng thanh toán</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {result.items.map((order) => (
              <OrderRow
                key={order.orderId}
                order={order}
                detail={detail?.orderId === order.orderId ? detail : null}
                onToggle={() => toggleDetail(order.orderId)}
              />
            ))}
          </tbody>
        </table>
      )}

      {totalPages > 0 && (
        <div className="pager">
          <button className="btn-secondary" onClick={() => goToPage(page - 1)} disabled={page <= 0 || loading}>
            <IconArrowLeft /> Trước
          </button>
          <span className="muted">
            Trang {page + 1} / {totalPages}
          </span>
          <button
            className="btn-secondary"
            onClick={() => goToPage(page + 1)}
            disabled={page + 1 >= totalPages || loading}
          >
            Sau <IconArrowRight />
          </button>
        </div>
      )}
    </section>
  )
}

function OrderRow({ order, detail, onToggle }) {
  return (
    <>
      <tr>
        <td className="mono">{order.orderId}</td>
        <td>{dateTime(order.createdAt)}</td>
        <td className="num">{order.itemCount}</td>
        <td>{regionLabel(order.region)}</td>
        <td>{order.couponCode ?? '—'}</td>
        <td className="num">{vnd(order.finalTotal)}</td>
        <td className="num">
          <button className="btn-ghost" onClick={onToggle}>
            {detail ? (
              <>
                <IconChevronUp /> Đóng
              </>
            ) : (
              <>
                <IconChevronDown /> Chi tiết
              </>
            )}
          </button>
        </td>
      </tr>
      {detail && (
        <tr>
          <td colSpan={7}>
            {detail.error && <div className="alert alert-error">{detail.error}</div>}
            {!detail.error && !detail.invoice && <p className="muted">Đang tải chi tiết…</p>}
            {detail.invoice && <OrderDetail invoice={detail.invoice} />}
          </td>
        </tr>
      )}
    </>
  )
}

function OrderDetail({ invoice }) {
  return (
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

      <Row label="Tạm tính" value={vnd(invoice.subtotal)} />
      <Row
        label="Ưu đãi theo số lượng"
        value={invoice.categoryDiscount > 0 ? `− ${vnd(invoice.categoryDiscount)}` : vnd(0)}
        tone={invoice.categoryDiscount > 0 ? 'discount' : undefined}
      />
      <Row
        label={`Mã giảm giá${invoice.couponCode ? ` ${invoice.couponCode}` : ''}`}
        value={invoice.couponDiscount > 0 ? `− ${vnd(invoice.couponDiscount)}` : vnd(0)}
        tone={invoice.couponDiscount > 0 ? 'discount' : undefined}
      />
      <Row label="Tổng trước thuế" value={vnd(invoice.total)} />
      <Row label={`Thuế ${percent(invoice.taxRate)}`} value={vnd(invoice.taxAmount)} />
      <Row label="TỔNG THANH TOÁN" value={vnd(invoice.finalTotal)} strong />
    </div>
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
