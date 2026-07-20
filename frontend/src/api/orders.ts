import { apiClient } from './client'
import type { PageResponse } from '../types/api'
import type { OrderResponse, PlaceOrderItem } from '../types/order'

export async function placeOrder(items: PlaceOrderItem[]): Promise<{ orderId: string }> {
  const { data } = await apiClient.post<{ orderId: string }>('/orders', { items })
  return data
}

export async function getOrder(id: string): Promise<OrderResponse> {
  const { data } = await apiClient.get<OrderResponse>(`/orders/${id}`)
  return data
}

export async function listMyOrders(page = 0, size = 10): Promise<PageResponse<OrderResponse>> {
  const { data } = await apiClient.get<PageResponse<OrderResponse>>('/orders/my', {
    params: { page, size },
  })
  return data
}
