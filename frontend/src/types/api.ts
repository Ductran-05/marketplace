export interface ErrorResponse {
  code: string
  message: string
  timestamp: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}
