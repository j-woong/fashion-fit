import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { getBodyProfile, type BodyProfile } from '../api/bodyProfile'
import { listGarments, type Garment } from '../api/garments'
import { useAuth } from '../auth/AuthContext'

const show = (value: number | null, unit: string) => (value == null ? '-' : `${value}${unit}`)

function HomePage() {
  const { me, logout } = useAuth()
  const [bodyProfile, setBodyProfile] = useState<BodyProfile | null>(null)
  const [garments, setGarments] = useState<Garment[] | null>(null)

  // 홈에 필요한 두 가지를 동시에 불러온다
  useEffect(() => {
    getBodyProfile().then(setBodyProfile)
    listGarments().then(setGarments)
  }, [])

  return (
    <main className="page">
      <header className="header">
        <h1>Fashion Fit</h1>
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

      <section className="card">
        <div className="header">
          <h2>내 옷장</h2>
          <Link className="text-button" to="/closet">
            전체 보기
          </Link>
        </div>
        {garments === null && <p>불러오는 중...</p>}
        {garments?.length === 0 && (
          <>
            <p>잘 맞는 옷 하나의 실측을 등록하면 사이즈 비교를 시작할 수 있어요.</p>
            <Link className="button primary" to="/closet/new">
              첫 기준 옷 등록하기
            </Link>
          </>
        )}
        {garments && garments.length > 0 && <p>기준 옷 {garments.length}벌이 등록돼 있어요.</p>}
      </section>
    </main>
  )
}

export default HomePage
