import { useState } from 'react'
import { IconArrowLeft, IconLock } from '../icons.jsx'

const ADMIN_PASSWORD = '123456'

export default function AdminLoginScreen({ onUnlock, onBack }) {
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)

  const submit = (event) => {
    event.preventDefault()
    if (password === ADMIN_PASSWORD) {
      onUnlock()
      return
    }
    setError('Mật khẩu không đúng.')
    setPassword('')
  }

  return (
    <section className="panel panel-narrow">
      <header className="panel-head">
        <h2>
          <IconLock /> Khu vực quản trị
        </h2>
        <button className="btn-ghost" onClick={onBack}>
          <IconArrowLeft /> Về trang bán hàng
        </button>
      </header>

      <form className="lock-form" onSubmit={submit}>
        <div className="lock-badge">
          <IconLock size={28} />
        </div>
        <p className="muted lock-intro">Nhập mật khẩu để xem lịch sử đơn hàng.</p>

        <label className="field" htmlFor="admin-password">
          Mật khẩu
          <input
            id="admin-password"
            type="password"
            autoFocus
            autoComplete="current-password"
            value={password}
            onChange={(event) => {
              setPassword(event.target.value)
              setError(null)
            }}
          />
        </label>

        {error && <div className="alert alert-error">{error}</div>}

        <button className="btn-primary" type="submit" disabled={!password}>
          Mở khoá
        </button>
      </form>
    </section>
  )
}
