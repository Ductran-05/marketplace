import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import * as productsApi from '../api/products'
import { extractErrorMessage } from '../api/errors'
import { ErrorBanner } from '../components/ErrorBanner'

interface ProductFormPageProps {
  mode: 'create' | 'edit'
}

export function ProductFormPage({ mode }: ProductFormPageProps) {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [currency, setCurrency] = useState('VND')
  const [stockQuantity, setStockQuantity] = useState('0')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(mode === 'edit')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (mode !== 'edit' || !id) {
      return
    }
    setLoading(true)
    productsApi
      .getProduct(id)
      .then((product) => {
        setName(product.name)
        setDescription(product.description ?? '')
        setPrice(String(product.price))
        setCurrency(product.currency)
        setStockQuantity(String(product.stockQuantity))
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [mode, id])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const request = {
        name,
        description: description || undefined,
        price: Number(price),
        currency,
        stockQuantity: Number(stockQuantity),
      }

      const productId = mode === 'create' ? (await productsApi.createProduct(request)).productId : id!
      if (mode === 'edit') {
        await productsApi.updateProduct(productId, request)
      }
      if (imageFile) {
        await productsApi.uploadProductImage(productId, imageFile)
      }
      navigate(`/products/${productId}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <p>Đang tải...</p>
  }

  return (
    <form onSubmit={handleSubmit} className="form">
      <h1>{mode === 'create' ? 'Tạo sản phẩm' : 'Sửa sản phẩm'}</h1>
      <ErrorBanner message={error} />
      <label>
        Tên sản phẩm
        <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={255} />
      </label>
      <label>
        Mô tả
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} maxLength={5000} />
      </label>
      <label>
        Giá
        <input type="number" min="0" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} required />
      </label>
      <label>
        Đơn vị tiền tệ (3 ký tự)
        <input
          value={currency}
          onChange={(e) => setCurrency(e.target.value.toUpperCase())}
          required
          minLength={3}
          maxLength={3}
        />
      </label>
      <label>
        Số lượng tồn kho
        <input
          type="number"
          min="0"
          step="1"
          value={stockQuantity}
          onChange={(e) => setStockQuantity(e.target.value)}
          required
        />
      </label>
      <label>
        Ảnh sản phẩm (JPEG/PNG/WebP, tối đa 5MB)
        <input type="file" accept="image/jpeg,image/png,image/webp" onChange={(e) => setImageFile(e.target.files?.[0] ?? null)} />
      </label>
      <button type="submit" disabled={submitting}>
        {mode === 'create' ? 'Tạo' : 'Lưu'}
      </button>
    </form>
  )
}
