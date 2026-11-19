import { useEffect, useRef, type ReactNode } from 'react'
import { Link } from 'react-router'
import { GOOGLE_LOGIN_URL } from '../api/auth'

type Props = {
  loginError: boolean
  // 로그인한 사람이 로고를 눌러 들어온 경우. 버튼이 구글 로그인 대신 앱 화면으로 간다
  member?: boolean
}

// 로그인 전: 구글 로그인 페이지로 "화면 이동"(<a href>)
// 로그인 후: 앱 안에서 이동(<Link>). 새로고침 없이 바로 넘어간다
function Go({ member, to, className, children }: { member: boolean; to: string; className: string; children: ReactNode }) {
  return member ? (
    <Link className={className} to={to}>
      {children}
    </Link>
  ) : (
    <a className={className} href={GOOGLE_LOGIN_URL}>
      {children}
    </a>
  )
}

// 이용 방법 3단계. 문구만 바꾸면 화면이 같이 바뀐다
const STEPS = [
  {
    title: '스크린샷 한 장 올리기',
    body: '인스타그램, 쇼핑몰 어디서 본 옷이든 캡처해서 올리거나 붙여 넣어요. 상품 링크를 붙여 넣어도 돼요.',
  },
  {
    title: 'AI가 옷을 하나씩 찾아요',
    body: '사진 속 상의·하의·신발·가방을 따로 알아보고, 여러 쇼핑몰에서 비슷한 상품을 가격과 함께 모아 와요.',
  },
  {
    title: '가성비와 내 사이즈 확인',
    body: '가격 대비 재질이 괜찮은지, 내 사이즈엔 어떻게 맞는지 보고 마음에 들면 쇼핑몰로 바로 이동해요.',
  },
]

const FEATURES = [
  {
    title: '품목별 합리적인 가격대',
    body: '반팔 6만 원과 패딩 6만 원은 다르죠. 옷 종류마다 납득할 만한 가격 범위로 걸러 봐요.',
  },
  {
    title: '재질과 가성비 평가',
    body: '부드러운지, 거친지, 보풀은 잘 생기는지. 소재를 풀어 설명하고 가격 대비 품질을 알려 줘요.',
  },
  {
    title: '내 사이즈 기준 핏 판정',
    body: '가입할 때 한 번 넣은 내 사이즈로, 이 옷이 레귤러핏인지 오버핏인지 미리 알려 줘요.',
  },
]

// 로그인 전 첫 화면. 서비스가 무엇인지 보여 주고 구글 로그인으로 보낸다.
// <button onClick={fetch}> 가 아니라 <a href> 인 이유: 구글 로그인 페이지로 화면 자체가 이동해야 해서.
function LandingPage({ loginError, member = false }: Props) {
  const rootRef = useRef<HTMLDivElement>(null)

  // 스크롤해서 보이게 된 구역을 부드럽게 나타나게 한다.
  // 상태(state)를 바꾸지 않고 class 만 붙여서, 스크롤할 때마다 화면을 다시 그리지 않는다.
  useEffect(() => {
    const targets = rootRef.current?.querySelectorAll('.reveal') ?? []
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('shown')
            observer.unobserve(entry.target)
          }
        }
      },
      { threshold: 0.2 },
    )
    targets.forEach((el) => observer.observe(el))
    return () => observer.disconnect()
  }, [])

  return (
    <div className="landing" ref={rootRef}>
      <header className="landing-nav">
        <span className="logo">
          Worth<em>It</em>
        </span>
        <Go member={member} to="/" className="nav-login">
          {member ? '내 홈' : '로그인'}
        </Go>
      </header>

      <main>
        <section className="hero">
          <p className="eyebrow">AI 의류 비교·탐색</p>
          <h1>
            내 돈을
            <br />
            <span className="accent">가장 가치 있게.</span>
          </h1>
          <p className="hero-sub">
            스크린샷 한 장이면 비슷한 옷을 더 합리적인 가격에 찾아 드려요.
            <br />
            재질과 가성비, 내 사이즈까지 한 번에.
          </p>
          {loginError && <p className="error">로그인에 실패했어요. 다시 시도해 주세요.</p>}
          <div className="hero-actions">
            <Go member={member} to="/compare" className="cta">
              스크린샷 하나로 가치 있는 대체품 찾기
            </Go>
            <a className="cta-ghost" href="#how">
              어떻게 쓰나요? ↓
            </a>
          </div>

          {/* 결과 화면을 미리 보여 주는 예시 카드 (실제 상품 아님) */}
          <div className="preview" aria-hidden="true">
            <div className="preview-row original">
              <span className="thumb" />
              <div>
                <small>찾고 싶은 옷</small>
                <strong>경량 후드 패딩</strong>
              </div>
              <span className="price strike">129,000원</span>
            </div>
            <div className="preview-row">
              <span className="thumb alt" />
              <div>
                <small>비슷한 옷 · 폴리에스터 100%</small>
                <strong>라이트 클린 경량 패딩</strong>
              </div>
              <span className="price">74,800원</span>
            </div>
            <div className="preview-tags">
              <span className="tag on">가성비 좋음</span>
              <span className="tag">내 사이즈 L · 레귤러핏</span>
              <span className="tag">부드러운 촉감</span>
            </div>
          </div>
        </section>

        <section id="how" className="steps">
          <h2 className="section-title reveal">이렇게 써요</h2>
          <ol>
            {STEPS.map((step, i) => (
              <li key={step.title} className="guide-step reveal">
                <span className="step-no">STEP {i + 1}</span>
                <h3>{step.title}</h3>
                <p>{step.body}</p>
              </li>
            ))}
          </ol>
        </section>

        <section className="features">
          <h2 className="section-title reveal">싸기만 한 옷 말고, 값어치 하는 옷</h2>
          <div className="feature-grid">
            {FEATURES.map((f) => (
              <article key={f.title} className="feature reveal">
                <h3>{f.title}</h3>
                <p>{f.body}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="final-cta reveal">
          <h2>지금 보고 있는 그 옷, 더 싸게 찾을 수 있을지도 몰라요</h2>
          <Go member={member} to="/compare" className="cta">
            {member ? '지금 옷 비교하기' : 'Google로 시작하기'}
          </Go>
          {!member && <p className="muted">가입하면 내 사이즈를 한 번만 입력해요. 이후 모든 비교에 쓰여요.</p>}
        </section>
      </main>

      <footer className="landing-footer">© 2026 WorthIt · 포트폴리오 프로젝트</footer>
    </div>
  )
}

export default LandingPage
