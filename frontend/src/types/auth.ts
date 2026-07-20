export interface RegisterRequest {
  email: string
  password: string
  fullName: string
}

export interface ResendOtpRequest {
  email: string
}

export interface VerifyEmailRequest {
  email: string
  otp: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
}

export type Role = 'BUYER' | 'SELLER'

export interface AuthenticatedUser {
  userId: string
  email: string
  role: Role
}
