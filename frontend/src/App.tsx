import { useEffect, useState } from 'react'
import { getMe, logout, type MeResponse } from './api/auth'
import { getBodyProfile, type BodyProfile } from './api/bodyProfile'
import { UnauthorizedError } from './api/client'
import BodyProfilePage from './pages/BodyProfilePage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'

// 로그인 상태: 확인 중 / 로그인 안 됨 / 로그인 됨 / 서버 오류
type AuthState =
  | { kind: 'loading' }
  | { kind: 'guest' }
  | { kind: 'member'; me: MeResponse }
  | { kind: 'error'; message: string }

// 로그인한 사람이 지금 보고 있는 화면 (화면이 더 늘어나면 react-router 로 바꾼다)
type View = 'home' | 'editBodyProfile'

function App() {
  const [auth, setAuth] = useState<AuthState>({ kind: 'loading' })
  const [view, setView] = useState<View>('home')
  const [bodyProfile, setBodyProfile] = useState<BodyProfile | null>(null)

  // 구글 로그인에 실패하면 백엔드가 /?loginError=true 로 돌려보낸다
  const loginError = new URLSearchParams(window.location.search).has('loginError')

  // 1) 처음 뜰 때 "나 로그인 돼 있어?"
  useEffect(() => {
    getMe()
      .then((me) => setAuth({ kind: 'member', me }))
      .catch((e: Error) => {
        if (e instanceof UnauthorizedError) {
          setAuth({ kind: 'guest' })
        } else {
          setAuth({ kind: 'error', message: e.message })
        }
      })
  }, [])

  // 2) 로그인 돼 있고 신체 정보가 있으면 불러온다
  const hasBodyProfile = auth.kind === 'member' && auth.me.hasBodyProfile
  useEffect(() => {
    if (hasBodyProfile) {
      getBodyProfile().then(setBodyProfile)
    }
  }, [hasBodyProfile])

  const handleLogout = async () => {
    await logout()
    setAuth({ kind: 'guest' })
    setBodyProfile(null)
  }

  const handleBodyProfileSaved = (saved: BodyProfile) => {
    setBodyProfile(saved)
    if (auth.kind === 'member') {
      setAuth({ kind: 'member', me: { ...auth.me, hasBodyProfile: true } })
    }
    setView('home')
  }

  if (auth.kind === 'loading') {
    return <main className="page">불러오는 중...</main>
  }
  if (auth.kind === 'error') {
    return (
      <main className="page">
        <p className="error">
          서버에 연결할 수 없어요: {auth.message}
          <br />
          백엔드가 켜져 있는지 확인하세요.
        </p>
      </main>
    )
  }
  if (auth.kind === 'guest') {
    return <LoginPage loginError={loginError} />
  }

  // 처음 가입한 사람: 신체 정보부터
  if (!auth.me.hasBodyProfile) {
    return <BodyProfilePage mode="onboarding" initial={null} onSaved={handleBodyProfileSaved} />
  }
  if (view === 'editBodyProfile') {
    return (
      <BodyProfilePage
        mode="edit"
        initial={bodyProfile}
        onSaved={handleBodyProfileSaved}
        onCancel={() => setView('home')}
      />
    )
  }
  return (
    <HomePage
      me={auth.me}
      bodyProfile={bodyProfile}
      onEditBodyProfile={() => setView('editBodyProfile')}
      onLogout={handleLogout}
    />
  )
}

export default App
