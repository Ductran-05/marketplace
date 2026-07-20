import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as ordersApi from '../api/orders'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'
import { OrderStatusBadge } from '../components/OrderStatusBadge'
import { Pagination } from '../components/Pagination'
import type { PageResponse } from '../types/api'
import type { OrderResponse } from '../types/order'

const PAGE_SIZE = 10

export function MyOrdersPage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PageResponse<OrderResponse> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    ordersApi
      .listMyOrders(page, PAGE_SIZE)
      .then((result) => {
        if (!cancelled) setData(result)
      })
      .catch((err) => {
        if (!cancelled) setError(extractErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [page])

  return (
    <div>
      <h1>Đơn hàng của tôi</h1>
      <ErrorBanner message={error} />
      {loading && <p>Đang tải...</p>}
      {!loading && data?.items.length === 0 && <p>Bạn chưa có đơn hàng nào.</p>}
      <ul className="order-list">
        {data?.items.map((order) => (
          <li key={order.id}>
            <Link to={`/orders/${order.id}`} className="order-list-item">
              <span>#{order.id.slice(0, 8)}</span>
              <span>{new Date(order.createdAt).toLocaleString('vi-VN')}</span>
              <OrderStatusBadge status={order.status} />
              <span>
                {order.totalAmount} {order.currency}
              </span>
            </Link>
          </li>
        ))}
      </ul>
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </div>
  )
}
