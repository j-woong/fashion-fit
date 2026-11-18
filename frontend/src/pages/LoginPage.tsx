import { GOOGLE_LOGIN_URL } from '../api/auth'

type Props = {
  loginError: boolean
}

function LoginPage({ loginError }: Props) {
  return (
    <main className="page">
      <h1>WorthIt</h1>
      <p className="sub">인스타에서 본 그 옷, 나한테 맞는 사이즈로 찾기</p>

      <section className="card">
        <h2>시작하기</h2>
        <p>내 옷 실측을 한 번만 등록하면, 사고 싶은 옷의 사이즈를 비교해 드려요.</p>
        {loginError && <p className="error">로그인에 실패했어요. 다시 시도해 주세요.</p>}
        {/* <button onClick={fetch...}> 가 아니라 <a href> : 구글 페이지로 화면이 이동해야 한다 */}
        <a className="button google" href={GOOGLE_LOGIN_URL}>
          Google로 시작하기
        </a>
      </section>
    </main>
  )
}

export default LoginPage
