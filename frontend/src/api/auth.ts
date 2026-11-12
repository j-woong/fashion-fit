import { apiGet, apiPost } from './client'

// 백엔드 MeResponse(record)와 같은 모양
export type MeResponse = {
  id: number
  email: string
  nickname: string
  // false 면 온보딩(신체 정보 입력) 화면을 먼저 보여준다
  hasBodyProfile: boolean
}

export const getMe = () => apiGet<MeResponse>('/me')

export const logout = () => apiPost<void>('/auth/logout')

// fetch 로 부르는 API가 아니라, 브라우저가 "페이지 이동"으로 가야 하는 주소.
// 구글 로그인 화면으로 넘어갔다가 다시 돌아와야 하기 때문.
export const GOOGLE_LOGIN_URL = '/oauth2/authorization/google'
