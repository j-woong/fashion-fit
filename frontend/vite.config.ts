import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 5173 이 이미 쓰이고 있으면 5174 로 몰래 옮기지 말고 에러를 낸다.
    // 구글 리디렉션 주소와 백엔드 app.frontend-url 이 5173 으로 고정돼 있기 때문.
    port: 5173,
    strictPort: true,
    // 아래 주소로 시작하는 요청은 Spring 서버(8080)로 넘긴다.
    // 브라우저 입장에서는 전부 같은 주소(5173)라서 CORS 설정이 필요 없고, 로그인 세션 쿠키도 그대로 공유된다.
    proxy: {
      '/api': 'http://localhost:8080',
      // 구글 로그인 시작 주소
      '/oauth2': 'http://localhost:8080',
      // 구글 로그인 후 돌아오는 주소 (구글 콘솔의 "승인된 리디렉션 URI"와 같아야 함)
      '/login/oauth2': 'http://localhost:8080',
    },
  },
})
