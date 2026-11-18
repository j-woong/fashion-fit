import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { getBodyProfile, type BodyProfile } from '../api/bodyProfile'
import { listGarments, type Garment } from '../api/garments'
import { useAuth } from '../auth/AuthContext'

const show = (value: number | null, unit: string) => (value == null ? '-' : `${value}${unit}`)

function HomePage() {
  const { me, logout } = useAuth()
  const [bodyProfile, setBodyProfile] = useState<BodyProfile | null>(null)
  const [mySizes, setMySizes] = useState<Garment[] | null>(null)

  useEffect(() => {
    getBodyProfile().then(setBodyProfile)
    listGarments().then(setMySizes)
  }, [])

  // 종류별로 하나씩만 보여준다 (목록은 최근순이라 처음 나오는 것이 최신)
  const sizeCategories = [...new Map((mySizes ?? []).map((g) => [g.category, g.categoryLabel])).values()]

  return (
    <main className="page">
      <header className="header">
        <h1>WorthIt</h1>
        <button type="button" className="text-button" onClick={logout}>
          로그아웃
        </button>
      </header>

      <section className="card">
        <h2>{me.nickname}님, 반가워요</h2>
        <p className="sub">{me.email}</p>
      </section>

      <section className="card">
        <div className="header">
          <h2>옷 비교하기</h2>
          <Link className="text-button" to="/size-checks">
            비교 기록
          </Link>
        </div>
        <p>옷 스크린샷으로 비슷한 옷을 찾고, 실측표를 넣어 내 사이즈와 비교해요.</p>
        <Link className="button primary" to="/compare">
          옷 비교하기
        </Link>
      </section>

      <section className="card">
        <div className="header">
          <h2>내 사이즈</h2>
          <Link className="text-button" to="/my-sizes">
            {sizeCategories.length > 0 ? '수정' : '입력'}
          </Link>
        </div>
        {mySizes === null && <p>불러오는 중...</p>}
        {mySizes !== null && sizeCategories.length === 0 && (
          <>
            <p>잘 맞는 옷의 실측을 한 번 넣어 두면, 실측표만으로 맞는 사이즈를 알려드려요.</p>
            <Link className="button primary" to="/my-sizes">
              내 사이즈 입력하기
            </Link>
          </>
        )}
        {sizeCategories.length > 0 && (
          <ul className="chips">
            {sizeCategories.map((label) => (
              <li key={label}>{label} ✓</li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <div className="header">
          <h2>내 신체 정보</h2>
          <Link className="text-button" to="/body-profile">
            수정
          </Link>
        </div>
        {bodyProfile ? (
          <dl className="stats">
            <div>
              <dt>발 사이즈</dt>
              <dd>{show(bodyProfile.footSizeMm, 'mm')}</dd>
            </div>
            <div>
              <dt>키</dt>
              <dd>{show(bodyProfile.heightCm, 'cm')}</dd>
            </div>
            <div>
              <dt>몸무게</dt>
              <dd>{show(bodyProfile.weightKg, 'kg')}</dd>
            </div>
          </dl>
        ) : (
          <p>불러오는 중...</p>
        )}
      </section>
    </main>
  )
}

export default HomePage
