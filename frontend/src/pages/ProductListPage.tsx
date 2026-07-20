import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as productsApi from '../api/products'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'
import { Pagination } from '../components/Pagination'
import type { PageResponse, ProductResponse } from '../types/product'

const PAGE_SIZE = 10

export function ProductListPage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PageResponse<ProductResponse> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    productsApi
      .listProducts(page, PAGE_SIZE)
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
      <h1>Sản phẩm</h1>
      <ErrorBanner message={error} />
      {loading && <p>Đang tải...</p>}
      {!loading && data?.items.length === 0 && <p>Chưa có sản phẩm nào.</p>}
      <div className="product-grid">
        {data?.items.map((product) => (
          <Link to={`/products/${product.id}`} key={product.id} className="product-card">
            {product.imageUrl ? (
              <img src={product.imageUrl} alt={product.name} />
            ) : (
              <div className="product-card-placeholder" />
            )}
            <h3>{product.name}</h3>
            <p>
              {product.price} {product.currency}
            </p>
          </Link>
        ))}
      </div>
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </div>
  )
}
