import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ApiError } from '../api/client'
import { deleteSizeCheck, getSizeCheck, type SizeCheck } from '../api/sizeChecks'
import SizeCheckResult from '../components/SizeCheckResult'

// /size-checks/:id : 저장된 비교 결과 다시 보기
function SizeCheckResultPage() {
  const { sizeCheckId } = useParams()
  const navigate = useNavigate()
  const [check, setCheck] = useState<SizeCheck | null>(null)
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    getSizeCheck(Number(sizeCheckId))
      .then(setCheck)
      .catch((e) => {
        if (e instanceof ApiError && e.status === 404) setNotFound(true)
      })
  }, [sizeCheckId])

  const handleDelete = async () => {
    if (!check || !window.confirm('이 비교 기록을 삭제할까요?')) return
    await deleteSizeCheck(check.id)
    navigate('/size-checks')
  }

  if (notFound) {
    return (
      <main className="page">
        <p>비교 기록을 찾을 수 없어요.</p>
        <Link to="/size-checks">비교 기록으로</Link>
      </main>
    )
  }
  if (!check) {
    return <main className="page">불러오는 중...</main>
  }

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/size-checks">
          ← 비교 기록
        </Link>
        <button type="button" className="text-button danger" onClick={handleDelete}>
          삭제
        </button>
      </header>

      <h1>{check.productName}</h1>
      <p className="sub">
        {check.categoryLabel}
        {check.productUrl && (
          <>
            {' · '}
            <a href={check.productUrl} target="_blank" rel="noreferrer">
              상품 보기
            </a>
          </>
        )}
      </p>

      <SizeCheckResult check={check} />
    </main>
  )
}

export default SizeCheckResultPage
