import { useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'

export function AccountPage() {
  const { user, becomeSellerAndRefresh } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleBecomeSeller() {
    setError(null)
    setInfo(null)
    setSubmitting(true)
    try {
      await becomeSellerAndRefresh()
      setInfo('Bạn đã trở thành người bán.')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (!user) {
    return null
  }

  return (
    <div className="form">
      <h1>Tài khoản</h1>
      <ErrorBanner message={error} />
      {info && <div className="banner banner-info">{info}</div>}
      <p>Email: {user.email}</p>
      <p>Vai trò: {user.role}</p>
      {user.role === 'BUYER' && (
        <button type="button" onClick={handleBecomeSeller} disabled={submitting}>
          Trở thành người bán
        </button>
      )}
    </div>
  )
}
