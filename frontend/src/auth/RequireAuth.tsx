import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function RequireAuth() {
  const { user, loading } = useAuth()

  if (loading) {
    return <p>Đang tải...</p>
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
