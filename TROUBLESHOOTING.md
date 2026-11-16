# 트러블슈팅 메모

나중에 글로 정리하기 위한 한 줄 기록. (증상 → 원인 → 해결)

| 날짜 | 증상 | 원인 | 해결 |
| --- | --- | --- | --- |
| 11-08 | `npm run dev` 실행 시 `styleText` SyntaxError | Node 18. Vite 8은 Node 20.19+/22.12+ 필요 | nvm으로 Node 22, `.nvmrc` 추가 |
| 11-08 | bootRun 시 `Port 8080 was already in use` | 다른 프로젝트 서버가 8080 점유 | `lsof -i :8080`로 PID 확인 후 종료 |
| 11-09 | 구글 로그인 리디렉션 URI 입력 시 "공백 포함" 오류 | 복사한 주소 앞에 들여쓰기 공백 | 공백 제거 후 직접 입력 |
| 11-10 | 구글 로그인 `401 invalid_client` | IntelliJ 실행 위치가 루트라 `./secret.yml`을 못 찾고 client-id 기본값 `none` 사용 | `spring.config.import`에 `./backend/secret.yml` 경로 추가, Working directory를 backend로 |
| 11-10 | 로그인 후 백엔드(8080)로 이동 | Vite proxy가 Host 헤더를 8080으로 바꿔서 Spring이 redirect_uri를 8080으로 생성 | `redirect-uri`, `app.frontend-url`을 yml에 명시 |
| 11-12 | 프론트가 5174에서 열려 로그인 후 엉뚱한 곳으로 이동 | 5173을 이전 dev 서버가 점유해 Vite가 자동으로 포트 변경 | `strictPort: true`로 조용히 바뀌지 않게 |
| 11-12 | 실측 수정 시 유니크 제약 위반 가능성 | Hibernate flush 순서: INSERT가 DELETE보다 먼저 | 컬렉션 clear 대신 값만 변경, 없어진 부위만 제거 |
| 11-15 | `ddl-auto: validate`에서 enum 컬럼 타입 불일치 | Hibernate 6+는 MySQL에서 enum을 native ENUM으로 기대 | `@JdbcTypeCode(SqlTypes.VARCHAR)` |
| 11-15 | 4단계 백엔드 파일이 커밋에서 누락 | `frontend` 폴더 안에서 `git add .` 실행 | 루트에서 `git add`, 누락분 별도 커밋 |
