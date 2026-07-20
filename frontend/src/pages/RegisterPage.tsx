import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import * as authApi from '../api/auth'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'

export function RegisterPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await authApi.register({ email, password, fullName })
      navigate('/verify', { state: { email } })
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="form">
      <h1>Đăng ký</h1>
      <ErrorBanner message={error} />
      <label>
        Họ tên
        <input value={fullName} onChange={(e) => setFullName(e.target.value)} required minLength={2} maxLength={100} />
      </label>
      <label>
        Email
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      </label>
      <label>
        Mật khẩu
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={8}
          maxLength={100}
        />
      </label>
      <button type="submit" disabled={submitting}>
        Đăng ký
      </button>
    </form>
  )
}
