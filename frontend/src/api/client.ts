// 모든 API 호출이 지나가는 한 곳.
// CSRF 토큰 붙이기, 401/400 구분 같은 공통 처리를 여기서만 한다.

// 백엔드 ErrorResponse(record)와 같은 모양
export type ErrorResponse = {
  message: string
  fieldErrors: Record<string, string>
}

// 로그인이 안 된 상태(401)는 "에러"라기보다 "로그인 화면을 보여줘야 함"이라서 따로 구분한다.
export class UnauthorizedError extends Error {
  constructor() {
    super('로그인이 필요합니다')
  }
}

// 서버가 ErrorResponse 를 보낸 경우 (예: 400 입력값 오류). 화면이 fieldErrors 를 입력칸 아래에 보여준다.
export class ApiError extends Error {
  readonly status: number
  readonly body: ErrorResponse

  constructor(status: number, body: ErrorResponse) {
    super(body.message)
    this.status = status
    this.body = body
  }
}

// 서버가 내려준 XSRF-TOKEN 쿠키 값을 읽는다
function readCookie(name: string): string | undefined {
  const found = document.cookie.split('; ').find((c) => c.startsWith(`${name}=`))
  return found ? decodeURIComponent(found.slice(name.length + 1)) : undefined
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  const method = (init.method ?? 'GET').toUpperCase()

  // 저장/수정/삭제 요청에는 CSRF 토큰을 헤더로 같이 보낸다 (SecurityConfig 참고)
  if (method !== 'GET') {
    const token = readCookie('XSRF-TOKEN')
    if (token) headers.set('X-XSRF-TOKEN', token)
  }

  const res = await fetch(`/api${path}`, { ...init, headers })

  if (res.status === 401) {
    throw new UnauthorizedError()
  }
  if (!res.ok) {
    const body = (await res.json().catch(() => null)) as ErrorResponse | null
    throw new ApiError(res.status, body ?? { message: `API 요청 실패: ${res.status}`, fieldErrors: {} })
  }
  // 204 No Content 처럼 본문이 없는 응답
  if (res.status === 204) {
    return undefined as T
  }
  return res.json() as Promise<T>
}

const jsonInit = (method: string, body?: unknown): RequestInit => ({
  method,
  headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
  body: body === undefined ? undefined : JSON.stringify(body),
})

export const apiGet = <T>(path: string) => request<T>(path)
export const apiPost = <T>(path: string, body?: unknown) => request<T>(path, jsonInit('POST', body))
export const apiPut = <T>(path: string, body?: unknown) => request<T>(path, jsonInit('PUT', body))
