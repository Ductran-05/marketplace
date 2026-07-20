import { useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import * as ordersApi from '../api/orders'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'
import { OrderStatusBadge } from '../components/OrderStatusBadge'
import type { OrderResponse } from '../types/order'

// Backend has no push channel for saga status changes (see docs/10-saga.md) — poll until
// PENDING resolves. Outbox relay ticks every 2s and saga latency is ~4-8s in dev, so 15
// attempts (~30s) comfortably covers the happy path before giving up.
const POLL_INTERVAL_MS = 2000
const MAX_POLL_ATTEMPTS = 15

export function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [pollTimedOut, setPollTimedOut] = useState(false)
  const attemptsRef = useRef(0)

  useEffect(() => {
    if (!id) {
      return
    }
    let cancelled = false
    let timer: ReturnType<typeof setTimeout> | undefined

    async function fetchOrder() {
      try {
        const result = await ordersApi.getOrder(id!)
        if (cancelled) {
          return
        }
        setOrder(result)
        setError(null)
        if (result.status === 'PENDING') {
          attemptsRef.current += 1
          if (attemptsRef.current < MAX_POLL_ATTEMPTS) {
            timer = setTimeout(fetchOrder, POLL_INTERVAL_MS)
          } else {
            setPollTimedOut(true)
          }
        }
      } catch (err) {
        if (!cancelled) {
          setError(extractErrorMessage(err))
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    attemptsRef.current = 0
    setPollTimedOut(false)
    setLoading(true)
    void fetchOrder()

    return () => {
      cancelled = true
      if (timer) {
        clearTimeout(timer)
      }
    }
  }, [id])

  if (loading && !order) {
    return <p>Đang tải...</p>
  }
  if (error && !order) {
    return <ErrorBanner message={error} />
  }
  if (!order) {
    return null
  }

  return (
    <div className="order-detail">
      <h1>Đơn hàng #{order.id.slice(0, 8)}</h1>
      <ErrorBanner message={error} />
      <OrderStatusBadge status={order.status} />
      {order.status === 'PENDING' && !pollTimedOut && <p>Đang chờ xác nhận tồn kho, vui lòng đợi...</p>}
      {pollTimedOut && <p>Đang xử lý lâu hơn dự kiến — tải lại trang sau.</p>}
      <table className="order-items">
        <thead>
          <tr>
            <th>Sản phẩm</th>
            <th>Đơn giá</th>
            <th>SL</th>
            <th>Tạm tính</th>
          </tr>
        </thead>
        <tbody>
          {order.items.map((item) => (
            <tr key={item.productId}>
              <td>{item.productName}</td>
              <td>
                {item.unitPrice} {order.currency}
              </td>
              <td>{item.quantity}</td>
              <td>
                {item.subtotal} {order.currency}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="order-total">
        Tổng cộng: {order.totalAmount} {order.currency}
      </p>
    </div>
  )
}
