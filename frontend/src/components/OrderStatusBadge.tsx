import type { OrderStatus } from '../types/order'

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Đang xử lý',
  CONFIRMED: 'Đã xác nhận',
  PAID: 'Đã thanh toán',
  SHIPPED: 'Đã giao vận',
  CANCELLED: 'Đã hủy',
}

const STATUS_CLASSES: Record<OrderStatus, string> = {
  PENDING: 'status-pending',
  CONFIRMED: 'status-confirmed',
  PAID: 'status-confirmed',
  SHIPPED: 'status-confirmed',
  CANCELLED: 'status-cancelled',
}

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return <span className={`status-badge ${STATUS_CLASSES[status]}`}>{STATUS_LABELS[status]}</span>
}
