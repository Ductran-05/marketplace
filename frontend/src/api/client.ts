import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from './tokenStore'
import type { AuthResponse } from '../types/auth'

// Backend has no custom AuthenticationEntryPoint, so both "not authenticated"
// and "authenticated but wrong role" come back as 403 (never 401). We refresh
// on 403, which means a wrong-role call wastes one refresh+retry before
// failing again — acceptable trade-off, see docs/11-frontend.md.
const PUBLIC_AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/verify', '/auth/resend-otp', '/auth/refresh']

interface RetriableConfig extends InternalAxiosRequestConfig {
  _retried?: boolean
}

export const apiClient = axios.create({
  baseURL: '/api/v1',
})

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }
  // Plain axios, not apiClient — going through apiClient's own interceptors here would recurse.
  const response = await axios.post<AuthResponse>('/api/v1/auth/refresh', { refreshToken })
  setTokens(response.data.accessToken, response.data.refreshToken)
  return response.data.accessToken
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const config = error.config as RetriableConfig | undefined
    const isPublicAuthPath = PUBLIC_AUTH_PATHS.some((path) => config?.url?.includes(path))

    if (error.response?.status !== 403 || !config || config._retried || isPublicAuthPath || !getRefreshToken()) {
      throw error
    }

    config._retried = true
    try {
      refreshPromise ??= refreshAccessToken().finally(() => {
        refreshPromise = null
      })
      const newAccessToken = await refreshPromise
      config.headers.set('Authorization', `Bearer ${newAccessToken}`)
      return apiClient(config)
    } catch (refreshError) {
      clearTokens()
      window.location.href = '/login'
      throw refreshError
    }
  },
)
