import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { getBodyProfile, type BodyProfile } from '../api/bodyProfile'
import { listGarments, type Garment } from '../api/garments'
import { listSizeChecks, type SizeCheckSummary } from '../api/sizeChecks'
import { useAuth } from '../auth/AuthContext'
import AppHeader from '../components/AppHeader'

// 종류마다 대표로 보여 줄 부위 (사이즈를 한눈에 알아보기 쉬운 곳)
const KEY_PART: Record<string, string> = {
  SHORT_SLEEVE: 'CHEST_WIDTH',
  LONG_SLEEVE: 'CHEST_WIDTH',
  OUTER: 'CHEST_WIDTH',
  PANTS: 'WAIST_WIDTH',
}

const RECENT_LIMIT = 8

const daysAgo = (iso: string) => {
  const days = Math.floor((Date.now() - new Date(iso).getTime()) / 86_400_000)
  return days <= 0 ? '오늘' : `${days}일 전`
}

// 로그인 후 첫 화면: "바로 찾기"가 가장 크게, 그 아래 최근 기록과 내 정보 요약.
// 자주 하는 일(옷 찾기)은 한 번에, 가끔 하는 일(정보 수정)은 작게 둔다.
function HomePage() {
  const { me } = useAuth()
  const [bodyProfile, setBodyProfile] = useState<BodyProfile | null>(null)
  const [mySizes, setMySizes] = useState<Garment[] | null>(null)
  const [recent, setRecent] = useState<SizeCheckSummary[] | null>(null)

  useEffect(() => {
    getBodyProfile().then(setBodyProfile)
    listGarments().then(setMySizes)
    listSizeChecks().then(setRecent)
  }, [])

  // 종류별로 가장 최근 것 하나씩 (목록은 최근순이라 처음 나오는 것이 최신)
  const latestByCategory = [...new Map((mySizes ?? []).map((g) => [g.category, g] as const)).values()].reverse()

  return (
    <div className="app">
      <AppHeader />

      <main className="dashboard">
        <section className="greeting">
          <h1>
            {me.nickname}님,
            <br />
            오늘은 <span className="accent">어떤 옷</span>을 찾아볼까요?
          </h1>
        </section>

        {/* 가장 큰 버튼: 비교하기로 바로 간다 */}
        <Link to="/compare" className="search-box">
          <span className="search-icon" aria-hidden="true">
            ＋
          </span>
          <span>
            <strong>스크린샷 올리기 · 상품 링크 붙여넣기</strong>
            <small>비슷한 옷을 찾고, 실측표로 내 사이즈와 비교해요</small>
          </span>
          <span className="search-arrow" aria-hidden="true">
            →
          </span>
        </Link>

        <section className="dash-section">
          <div className="dash-title">
            <h2>최근 비교한 옷</h2>
            {recent && recent.length > 0 && <Link to="/size-checks">전체 보기</Link>}
          </div>
          {recent === null && <p className="muted">불러오는 중...</p>}
          {recent?.length === 0 && (
            <div className="empty">
              <p>아직 비교한 옷이 없어요.</p>
              <Link to="/compare">첫 비교 시작하기 →</Link>
            </div>
          )}
          {recent && recent.length > 0 && (
            <ul className="recent-list">
              {recent.slice(0, RECENT_LIMIT).map((r) => (
                <li key={r.id}>
                  <Link to={`/size-checks/${r.id}`} className="recent-card">
                    <span className="recent-size">{r.recommendedSize ?? '–'}</span>
                    <strong>{r.productName}</strong>
                    <small>
                      {r.categoryLabel} · {daysAgo(r.createdAt)}
                    </small>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>

        <div className="dash-grid">
          <section className="dash-section panel">
            <div className="dash-title">
              <h2>내 사이즈</h2>
              <Link to="/my-sizes">{latestByCategory.length > 0 ? '수정' : '입력'}</Link>
            </div>
            {mySizes === null && <p className="muted">불러오는 중...</p>}
            {mySizes?.length === 0 && (
              <div className="empty">
                <p>잘 맞는 옷의 실측을 한 번만 넣어 두면, 실측표만으로 맞는 사이즈를 알려 드려요.</p>
                <Link to="/my-sizes">내 사이즈 입력하기 →</Link>
              </div>
            )}
            {latestByCategory.length > 0 && (
              <ul className="size-summary">
                {latestByCategory.map((g) => {
                  const key = g.measurements.find((m) => m.part === KEY_PART[g.category])
                  return (
                    <li key={g.category}>
                      <span>{g.categoryLabel.replace(/ \(.*\)$/, '')}</span>
                      <strong>{key ? `${key.label} ${key.valueCm}cm` : '입력 완료'}</strong>
                    </li>
                  )
                })}
              </ul>
            )}
          </section>

          <section className="dash-section panel">
            <div className="dash-title">
              <h2>내 신체 정보</h2>
              <Link to="/body-profile">수정</Link>
            </div>
            {bodyProfile ? (
              <dl className="body-stats">
                <div>
                  <dt>키</dt>
                  <dd>{bodyProfile.heightCm ?? '-'}<small>cm</small></dd>
                </div>
                <div>
                  <dt>몸무게</dt>
                  <dd>{bodyProfile.weightKg ?? '-'}<small>kg</small></dd>
                </div>
                <div>
                  <dt>발</dt>
                  <dd>{bodyProfile.footSizeMm}<small>mm</small></dd>
                </div>
              </dl>
            ) : (
              <p className="muted">불러오는 중...</p>
            )}
          </section>
        </div>
      </main>
    </div>
  )
}

export default HomePage
