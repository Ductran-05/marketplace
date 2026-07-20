import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import * as productsApi from '../api/products'
import * as ordersApi from '../api/orders'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'
import { useAuth } from '../auth/AuthContext'
import type { ProductResponse } from '../types/product'

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [product, setProduct] = useState<ProductResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState(false)
  const [quantity, setQuantity] = useState(1)
  const [buying, setBuying] = useState(false)

  useEffect(() => {
    if (!id) {
      return
    }
    setLoading(true)
    setError(null)
    productsApi
      .getProduct(id)
      .then(setProduct)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [id])

  async function handleDelete() {
    if (!id || !window.confirm('Xóa sản phẩm này?')) {
      return
    }
    setDeleting(true)
    try {
      await productsApi.deleteProduct(id)
      navigate('/')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setDeleting(false)
    }
  }

  async function handleBuy() {
    if (!product) {
      return
    }
    setError(null)
    setBuying(true)
    try {
      const { orderId } = await ordersApi.placeOrder([{ productId: product.id, quantity }])
      navigate(`/orders/${orderId}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setBuying(false)
    }
  }

  if (loading) {
    return <p>Đang tải...</p>
  }
  if (error && !product) {
    return <ErrorBanner message={error} />
  }
  if (!product) {
    return null
  }

  const isOwner = user?.userId === product.sellerId

  return (
    <div className="product-detail">
      {product.imageUrl && <img src={product.imageUrl} alt={product.name} />}
      <h1>{product.name}</h1>
      <p className="price">
        {product.price} {product.currency}
      </p>
      {product.description && <p>{product.description}</p>}
      <p>Còn lại: {product.stockQuantity}</p>
      <ErrorBanner message={error} />
      {isOwner && (
        <div className="form-actions">
          <Link to={`/products/${product.id}/edit`}>Sửa</Link>
          <button type="button" onClick={handleDelete} disabled={deleting}>
            Xóa
          </button>
        </div>
      )}
      {!isOwner && product.stockQuantity > 0 && (
        <div className="form-actions">
          {user ? (
            <>
              <input
                type="number"
                min={1}
                max={product.stockQuantity}
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                className="quantity-input"
              />
              <button type="button" onClick={handleBuy} disabled={buying}>
                Mua ngay
              </button>
            </>
          ) : (
            <Link to="/login">Đăng nhập để mua</Link>
          )}
        </div>
      )}
      {!isOwner && product.stockQuantity === 0 && <p>Hết hàng</p>}
    </div>
  )
}
