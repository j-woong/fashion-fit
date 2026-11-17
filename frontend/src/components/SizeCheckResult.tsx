import { Link } from 'react-router'
import type { PartDiff, SizeCheck } from '../api/sizeChecks'

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
        {diff.productCm} / 내 사이즈 {diff.referenceCm}
      </span>
      <strong className={`diff ${tone}`}>{text}</strong>
    </li>
  )
}

// 사이즈 비교 결과: 추천 사이즈 + 사이즈별 핏과 부위 차이. 비교 화면과 기록 화면이 같이 쓴다.
function SizeCheckResult({ check }: { check: SizeCheck }) {
  const recommended = check.sizes.find((s) => s.recommended)

  return (
    <>
      <section className={`card result ${check.verdict.toLowerCase()}`}>
        {check.recommendedSize !== null && (
          <>
            <p className="muted">추천 사이즈</p>
            <p className="big-size">{check.recommendedSize}</p>
          </>
        )}
        <p>{check.verdictLabel}</p>
        {recommended?.fitDescription && <p>{recommended.fitDescription}</p>}
        {check.reference ? (
          <p className="muted">기준: {check.reference.name}</p>
        ) : (
          <Link className="button primary" to="/my-sizes">
            내 사이즈 입력하러 가기
          </Link>
        )}
      </section>

      {check.sizes.map((size) => (
        <section key={size.label} className={`card ${size.recommended ? 'highlight' : ''}`}>
          <div className="header">
            <h2>
              {size.label} {size.recommended && <span className="badge">추천</span>}{' '}
              {size.fitLabel && <span className="badge light">{size.fitLabel}</span>}
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
            <p className="muted">내 사이즈와 같이 잰 부위가 없어요.</p>
          )}
        </section>
      ))}
    </>
  )
}

export default SizeCheckResult
