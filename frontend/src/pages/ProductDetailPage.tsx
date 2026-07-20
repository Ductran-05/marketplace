import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import * as productsApi from '../api/products'
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

  if (loading) {
    return <p>Đang tải...</p>
  }
  if (error) {
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
      {isOwner && (
        <div className="form-actions">
          <Link to={`/products/${product.id}/edit`}>Sửa</Link>
          <button type="button" onClick={handleDelete} disabled={deleting}>
            Xóa
          </button>
        </div>
      )}
    </div>
  )
}
