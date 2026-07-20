import { apiClient } from './client'
import { setTokens } from './tokenStore'
import type {
  AuthResponse,
  AuthenticatedUser,
  LoginRequest,
  RegisterRequest,
  ResendOtpRequest,
  VerifyEmailRequest,
} from '../types/auth'

export async function register(request: RegisterRequest): Promise<{ userId: string; message: string }> {
  const { data } = await apiClient.post('/auth/register', request)
  return data
}

export async function resendOtp(request: ResendOtpRequest): Promise<{ message: string }> {
  const { data } = await apiClient.post('/auth/resend-otp', request)
  return data
}

export async function verify(request: VerifyEmailRequest): Promise<{ message: string }> {
  const { data } = await apiClient.post('/auth/verify', request)
  return data
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', request)
  setTokens(data.accessToken, data.refreshToken)
  return data
}

export async function refreshTokens(refreshToken: string): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/refresh', { refreshToken })
  setTokens(data.accessToken, data.refreshToken)
  return data
}

export async function me(): Promise<AuthenticatedUser> {
  const { data } = await apiClient.get<AuthenticatedUser>('/auth/me')
  return data
}

export async function becomeSeller(): Promise<{ message: string }> {
  const { data } = await apiClient.post('/auth/become-seller')
  return data
}
