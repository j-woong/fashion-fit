import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ApiError } from '../api/client'
import { deleteSizeCheck, getSizeCheck, type PartDiff, type SizeCheck } from '../api/sizeChecks'

// 부위별 차이를 사람이 읽는 말로: 1cm 이내는 "비슷", 그보다 크면 "n cm 커요/작아요"
function describeDiff(diff: number) {
  if (Math.abs(diff) <= 1) return { text: diff === 0 ? '같아요' : `비슷 (${diff > 0 ? '+' : ''}${diff})`, tone: 'same' }
  return diff > 0 ? { text: `${diff}cm 커요`, tone: 'bigger' } : { text: `${-diff}cm 작아요`, tone: 'smaller' }
}

function PartRow({ diff }: { diff: PartDiff }) {
  const { text, tone } = describeDiff(diff.diffCm)
  return (
    <li className="diff-row">
      <span>{diff.label}</span>
      <span className="muted">
        {diff.productCm} / 내 옷 {diff.referenceCm}
      </span>
      <strong className={`diff ${tone}`}>{text}</strong>
    </li>
  )
}

// /size-checks/:id : 비교 결과
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

  const canRecommend = check.recommendedSize !== null

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

      <section className={`card result ${check.verdict.toLowerCase()}`}>
        {canRecommend ? (
          <>
            <p className="muted">추천 사이즈</p>
            <p className="big-size">{check.recommendedSize}</p>
          </>
        ) : null}
        <p>{check.verdictLabel}</p>
        {check.reference ? (
          <p className="muted">
            기준: {check.reference.name} ({check.reference.fitFeelLabel})
          </p>
        ) : (
          <Link className="button primary" to="/closet/new">
            기준 옷 등록하러 가기
          </Link>
        )}
      </section>

      {check.sizes.map((size) => (
        <section key={size.label} className={`card ${size.recommended ? 'highlight' : ''}`}>
          <div className="header">
            <h2>
              {size.label} {size.recommended && <span className="badge">추천</span>}
            </h2>
            {size.score !== null && <span className="muted">평균 {size.score}cm 차이</span>}
          </div>
          {size.parts.length > 0 ? (
            <ul className="diff-list">
              {size.parts.map((diff) => (
                <PartRow key={diff.part} diff={diff} />
              ))}
            </ul>
          ) : (
            <p className="muted">기준 옷과 같이 잰 부위가 없어요.</p>
          )}
        </section>
      ))}
    </main>
  )
}

export default SizeCheckResultPage
