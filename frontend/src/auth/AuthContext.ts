import { createContext, useContext } from 'react'
import type { MeResponse } from '../api/auth'

// 로그인한 사용자 정보를 화면 어디서나 꺼내 쓰기 위한 Context.
// props 로 App → Page → 컴포넌트 ... 계속 넘기지 않아도 된다.
export type AuthValue = {
  me: MeResponse
  updateMe: (patch: Partial<MeResponse>) => void
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthValue | null>(null)

export function useAuth(): AuthValue {
  const value = useContext(AuthContext)
  if (!value) {
    throw new Error('useAuth 는 로그인된 화면(AuthContext.Provider 안)에서만 쓸 수 있어요')
  }
  return value
}
