export const vnd = (value) =>
  value === null || value === undefined ? '—' : Number(value).toLocaleString('vi-VN') + 'đ'

export const percent = (rate) => `${(Number(rate) * 100).toFixed(0)}%`

export const REGIONS = [
  { code: 'noi_thanh', label: 'Nội thành (8%)' },
  { code: 'ngoai_thanh', label: 'Ngoại thành (10%)' },
  { code: 'tinh_khac', label: 'Tỉnh khác (12%)' },
]

export const regionLabel = (code, withRate = false) => {
  const label = REGIONS.find((r) => r.code === code)?.label
  if (!label) return code
  return withRate ? label : label.replace(/\s*\(.*\)$/, '')
}

export const dateTime = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
