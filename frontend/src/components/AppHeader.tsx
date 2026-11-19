import { Link, NavLink } from 'react-router'
import { useAuth } from '../auth/AuthContext'

// 로그인 후 모든 화면 위에 붙는 메뉴.
// 로고는 서비스 소개(랜딩) 페이지로 간다. 대부분의 서비스가 쓰는 방식이라 따로 설명하지 않아도 사용자가 알아챈다.
function AppHeader() {
  const { logout } = useAuth()

  return (
    <header className="app-header">
      <Link className="logo" to="/intro" title="서비스 소개 보기">
        Worth<em>It</em>
      </Link>
      <nav className="app-nav">
        {/* NavLink 는 지금 보고 있는 화면의 메뉴에 자동으로 active class 를 붙여 준다 */}
        <NavLink to="/" end>
          홈
        </NavLink>
        <NavLink to="/compare">비교하기</NavLink>
        <NavLink to="/size-checks">기록</NavLink>
        <NavLink to="/my-sizes">내 사이즈</NavLink>
        <button type="button" className="nav-logout" onClick={logout}>
          로그아웃
        </button>
      </nav>
    </header>
  )
}

export default AppHeader
