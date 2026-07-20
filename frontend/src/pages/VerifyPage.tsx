import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import * as authApi from '../api/auth'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'

export function VerifyPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const prefillEmail = (location.state as { email?: string } | null)?.email ?? ''
  const [email, setEmail] = useState(prefillEmail)
  const [otp, setOtp] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleVerify(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setInfo(null)
    setSubmitting(true)
    try {
      await authApi.verify({ email, otp })
      navigate('/login')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleResend() {
    setError(null)
    setInfo(null)
    try {
      const { message } = await authApi.resendOtp({ email })
      setInfo(message)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  return (
    <form onSubmit={handleVerify} className="form">
      <h1>Xác thực email</h1>
      <ErrorBanner message={error} />
      {info && <div className="banner banner-info">{info}</div>}
      <label>
        Email
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      </label>
      <label>
        Mã OTP (6 số, xem tại Mailhog http://localhost:8025)
        <input
          value={otp}
          onChange={(e) => setOtp(e.target.value)}
          required
          pattern="\d{6}"
          maxLength={6}
          inputMode="numeric"
        />
      </label>
      <div className="form-actions">
        <button type="submit" disabled={submitting}>
          Xác thực
        </button>
        <button type="button" onClick={handleResend}>
          Gửi lại mã
        </button>
      </div>
    </form>
  )
}
