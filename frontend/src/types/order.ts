export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PAID' | 'SHIPPED' | 'CANCELLED'

export interface PlaceOrderItem {
  productId: string
  quantity: number
}

export interface PlaceOrderRequest {
  items: PlaceOrderItem[]
}

export interface OrderItem {
  productId: string
  productName: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface OrderResponse {
  id: string
  buyerId: string
  status: OrderStatus
  totalAmount: number
  currency: string
  items: OrderItem[]
  createdAt: string
}
