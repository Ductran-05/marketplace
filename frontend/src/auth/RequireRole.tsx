import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'
import type { Role } from '../types/auth'

export function RequireRole({ role }: { role: Role }) {
  const { user, loading } = useAuth()

  if (loading) {
    return <p>Đang tải...</p>
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (user.role !== role) {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}
