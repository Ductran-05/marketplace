import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <div className="app-shell">
      <header className="nav">
        <Link to="/" className="brand">
          Marketplace
        </Link>
        <nav className="nav-links">
          {user ? (
            <>
              {user.role === 'SELLER' && <Link to="/products/new">+ Sản phẩm mới</Link>}
              <Link to="/orders">Đơn hàng của tôi</Link>
              <Link to="/account">
                {user.email} ({user.role})
              </Link>
              <button type="button" onClick={handleLogout}>
                Đăng xuất
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Đăng nhập</Link>
              <Link to="/register">Đăng ký</Link>
            </>
          )}
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
