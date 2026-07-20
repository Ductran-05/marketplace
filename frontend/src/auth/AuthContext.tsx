import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { clearTokens, getAccessToken, getRefreshToken } from '../api/tokenStore'
import type { AuthenticatedUser, LoginRequest } from '../types/auth'

interface AuthContextValue {
  user: AuthenticatedUser | null
  loading: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => void
  becomeSellerAndRefresh: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthenticatedUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function hydrate() {
      if (getAccessToken()) {
        try {
          setUser(await authApi.me())
        } catch {
          clearTokens()
          setUser(null)
        }
      }
      setLoading(false)
    }
    void hydrate()
  }, [])

  async function login(request: LoginRequest) {
    await authApi.login(request)
    setUser(await authApi.me())
  }

  function logout() {
    clearTokens()
    setUser(null)
  }

  async function becomeSellerAndRefresh() {
    await authApi.becomeSeller()
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    // Access token still carries the old role until we refresh — see docs/11-frontend.md
    await authApi.refreshTokens(refreshToken)
    setUser(await authApi.me())
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, becomeSellerAndRefresh }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
