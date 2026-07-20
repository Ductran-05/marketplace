interface PaginationProps {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }

  return (
    <div className="pagination">
      <button type="button" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ← Trước
      </button>
      <span>
        Trang {page + 1} / {totalPages}
      </span>
      <button type="button" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Sau →
      </button>
    </div>
  )
}
