export interface ProductRequest {
  name: string
  description?: string
  price: number
  currency: string
  stockQuantity: number
}

export interface ProductResponse {
  id: string
  sellerId: string
  name: string
  description: string | null
  price: number
  currency: string
  stockQuantity: number
  imageUrl: string | null
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}
