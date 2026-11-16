# Fashion Fit

인스타그램에서 본 옷과 비슷한 상품을 찾고, 내가 가진 옷의 실측을 기준으로 사이즈를 추천하는 웹 서비스.

| 폴더 | 내용 |
| --- | --- |
| `frontend/` | React + TypeScript + Vite (모바일 반응형 웹) |
| `backend/` | Spring Boot 4 + Java 25 + MySQL 8.4 (Flyway) |

## 실행

준비물: Docker Desktop, Java 25, Node 22.22 이상

```bash
# 0) MySQL (docker-compose.yml): localhost:3306
docker compose up -d

# 1) 백엔드: http://localhost:8080  (처음 뜰 때 Flyway 가 테이블을 만든다)
cd backend
./gradlew bootRun

# 2) 프론트엔드: http://localhost:5173
cd frontend
npm install
npm run dev
```

브라우저에서 http://localhost:5173 을 열면 "서버 연결 상태: 연결됨"이 보여야 한다.

## 요청 흐름

브라우저(5173) → `/api/health` → Vite proxy → Spring(8080) `HealthController` → JSON 응답 → React 화면

## 테스트

```bash
cd backend
./gradlew test   # Docker 가 켜져 있어야 한다 (Testcontainers 가 테스트용 MySQL 을 띄움)
```
