import { useEffect, useState } from 'react'
import { Navigate, Route, Routes } from 'react-router'
import { getMe, logout, type MeResponse } from './api/auth'
import { UnauthorizedError } from './api/client'
import { AuthContext, type AuthValue } from './auth/AuthContext'
import BodyProfileEditPage from './pages/BodyProfileEditPage'
import BodyProfilePage from './pages/BodyProfilePage'
import ClosetPage from './pages/ClosetPage'
import GarmentFormPage from './pages/GarmentFormPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import SizeCheckFormPage from './pages/SizeCheckFormPage'
import SizeCheckListPage from './pages/SizeCheckListPage'
import SizeCheckResultPage from './pages/SizeCheckResultPage'

// 로그인 상태: 확인 중 / 로그인 안 됨 / 로그인 됨 / 서버 오류
type AuthState =
  | { kind: 'loading' }
  | { kind: 'guest' }
  | { kind: 'member'; me: MeResponse }
  | { kind: 'error'; message: string }

function App() {
  const [auth, setAuth] = useState<AuthState>({ kind: 'loading' })

  // 구글 로그인에 실패하면 백엔드가 /?loginError=true 로 돌려보낸다
  const loginError = new URLSearchParams(window.location.search).has('loginError')

  // 처음 뜰 때 "나 로그인 돼 있어?"
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

  // ---- 1) 로그인 전에 보여줄 화면들 ----
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

  // ---- 2) 로그인한 사람: Context 에 담아서 아래 화면들이 useAuth() 로 꺼내 쓰게 한다 ----
  const me = auth.me
  const authValue: AuthValue = {
    me,
    updateMe: (patch) => setAuth({ kind: 'member', me: { ...me, ...patch } }),
    logout: async () => {
      await logout()
      setAuth({ kind: 'guest' })
    },
  }

  return (
    <AuthContext.Provider value={authValue}>
      {me.hasBodyProfile ? (
        // 주소별 화면
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/body-profile" element={<BodyProfileEditPage />} />
          <Route path="/closet" element={<ClosetPage />} />
          <Route path="/closet/new" element={<GarmentFormPage />} />
          <Route path="/closet/:garmentId/edit" element={<GarmentFormPage />} />
          <Route path="/size-checks" element={<SizeCheckListPage />} />
          <Route path="/size-checks/new" element={<SizeCheckFormPage />} />
          <Route path="/size-checks/:sizeCheckId" element={<SizeCheckResultPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      ) : (
        // 처음 가입한 사람: 어느 주소로 들어와도 신체 정보부터
        <BodyProfilePage
          mode="onboarding"
          initial={null}
          onSaved={() => authValue.updateMe({ hasBodyProfile: true })}
        />
      )}
    </AuthContext.Provider>
  )
}

export default App
