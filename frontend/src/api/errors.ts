import { isAxiosError } from 'axios'
import type { ErrorResponse } from '../types/api'

export function extractErrorMessage(error: unknown): string {
  if (isAxiosError<ErrorResponse>(error)) {
    return error.response?.data?.message ?? error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'Đã có lỗi xảy ra'
}
