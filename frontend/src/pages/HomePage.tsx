import type { MeResponse } from '../api/auth'
import type { BodyProfile } from '../api/bodyProfile'

type Props = {
  me: MeResponse
  bodyProfile: BodyProfile | null
  onEditBodyProfile: () => void
  onLogout: () => void
}

const show = (value: number | null, unit: string) => (value == null ? '-' : `${value}${unit}`)

function HomePage({ me, bodyProfile, onEditBodyProfile, onLogout }: Props) {
  return (
    <main className="page">
      <header className="header">
        <h1>Fashion Fit</h1>
        <button type="button" className="text-button" onClick={onLogout}>
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
          <button type="button" className="text-button" onClick={onEditBodyProfile}>
            수정
          </button>
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
        <h2>다음 단계</h2>
        <p>내 옷장에 첫 기준 옷(잘 맞는 옷)의 실측을 등록할 거예요.</p>
      </section>
    </main>
  )
}

export default HomePage
