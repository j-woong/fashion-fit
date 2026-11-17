import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { listSizeChecks, type SizeCheckSummary } from '../api/sizeChecks'

// /size-checks : 내가 비교해 본 상품 목록
function SizeCheckListPage() {
  const [checks, setChecks] = useState<SizeCheckSummary[] | null>(null)

  useEffect(() => {
    listSizeChecks().then(setChecks)
  }, [])

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/">
          ← 홈
        </Link>
        <Link className="text-button strong" to="/compare">
          + 새로 비교
        </Link>
      </header>
      <h1>사이즈 비교 기록</h1>
      <p className="sub">사고 싶은 옷의 실측표를 내 옷과 비교한 결과예요.</p>

      {checks === null && <p>불러오는 중...</p>}
      {checks?.length === 0 && (
        <section className="card">
          <p>아직 비교한 상품이 없어요.</p>
          <Link className="button primary" to="/compare">
            첫 사이즈 비교하기
          </Link>
        </section>
      )}
      {checks?.map((c) => (
        <Link key={c.id} to={`/size-checks/${c.id}`} className="card card-link">
          <div className="header">
            <h2>{c.productName}</h2>
            <span className="badge">{c.recommendedSize ?? '비교 불가'}</span>
          </div>
          <p className="sub">
            {c.categoryLabel}
            
            {' · '}
            {new Date(c.createdAt).toLocaleDateString('ko-KR')}
          </p>
        </Link>
      ))}
    </main>
  )
}

export default SizeCheckListPage
