const BASE = '/api'

function failure(message, code, details) {
  const error = new Error(message)
  error.code = code
  error.details = details ?? {}
  return error
}

async function request(path, options = {}) {
  let res
  try {
    res = await fetch(BASE + path, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    })
  } catch {
    throw failure('Không kết nối được tới máy chủ. Kiểm tra backend có đang chạy không.', 'NETWORK_ERROR')
  }

  const text = await res.text()
  let body = null
  if (text) {
    try {
      body = JSON.parse(text)
    } catch {
      body = null
    }
  }

  if (!res.ok) {
    throw failure(
      body?.message || `Máy chủ trả về lỗi ${res.status}.`,
      body?.code || `HTTP_${res.status}`,
      body?.details,
    )
  }
  return body
}

export const getProducts = () => request('/products')

const postOrder = (path, payload) =>
  request(path, { method: 'POST', body: JSON.stringify(payload) })

export const calculateSubtotal = (items) => postOrder('/orders/subtotal', { items })

export const applyPromotion = (items, couponCode) =>
  postOrder('/orders/promotion', { items, couponCode: couponCode || null })

export const confirmOrder = (items, couponCode, region) =>
  postOrder('/orders/confirm', { items, couponCode: couponCode || null, region })

export const getOrderHistory = (page, size) =>
  request(`/admin/orders?page=${page}&size=${size}`)

export const getOrderDetail = (orderId) => request(`/admin/orders/${orderId}`)
