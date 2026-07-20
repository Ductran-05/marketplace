import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { RequireAuth } from './auth/RequireAuth'
import { RequireRole } from './auth/RequireRole'
import { RegisterPage } from './pages/RegisterPage'
import { VerifyPage } from './pages/VerifyPage'
import { LoginPage } from './pages/LoginPage'
import { ProductListPage } from './pages/ProductListPage'
import { ProductDetailPage } from './pages/ProductDetailPage'
import { ProductFormPage } from './pages/ProductFormPage'
import { AccountPage } from './pages/AccountPage'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<ProductListPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify" element={<VerifyPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />

        <Route element={<RequireAuth />}>
          <Route path="/account" element={<AccountPage />} />
        </Route>

        <Route element={<RequireRole role="SELLER" />}>
          <Route path="/products/new" element={<ProductFormPage mode="create" />} />
          <Route path="/products/:id/edit" element={<ProductFormPage mode="edit" />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
