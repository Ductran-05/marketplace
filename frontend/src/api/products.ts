import { apiClient } from './client'
import type { PageResponse } from '../types/api'
import type { ProductRequest, ProductResponse } from '../types/product'

export async function listProducts(page = 0, size = 10): Promise<PageResponse<ProductResponse>> {
  const { data } = await apiClient.get<PageResponse<ProductResponse>>('/products', {
    params: { page, size },
  })
  return data
}

export async function getProduct(id: string): Promise<ProductResponse> {
  const { data } = await apiClient.get<ProductResponse>(`/products/${id}`)
  return data
}

export async function createProduct(request: ProductRequest): Promise<{ productId: string }> {
  const { data } = await apiClient.post<{ productId: string }>('/products', request)
  return data
}

export async function updateProduct(id: string, request: ProductRequest): Promise<void> {
  await apiClient.put(`/products/${id}`, request)
}

export async function deleteProduct(id: string): Promise<void> {
  await apiClient.delete(`/products/${id}`)
}

export async function uploadProductImage(id: string, file: File): Promise<{ imageKey: string; imageUrl: string }> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await apiClient.post<{ imageKey: string; imageUrl: string }>(`/products/${id}/image`, formData)
  return data
}
