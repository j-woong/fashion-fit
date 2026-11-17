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
| 11-17 | 네이버 개발자센터 앱 등록에 "검색", "데이터랩" API가 선택지에 없음 | 신청 경로 변경 (NAVER API Hub 쪽으로 이동한 것으로 보임, 미확인) | 상품 검색 API 의존을 빼고, LLM이 만든 검색어로 쇼핑몰 검색 결과 링크를 생성하는 방식으로 설계 변경 |
| 11-17 | `./gradlew test` 시 `JAVA_HOME is set to an invalid directory` | JDK 업데이트로 버전 번호가 박힌 경로가 사라짐 | `export JAVA_HOME=$(/usr/libexec/java_home -v 25)` |
| 11-17 | 서버 실행 시 `Communications link failure` / `Connection refused` | Docker Desktop 재시작 후 MySQL 컨테이너가 꺼진 상태 | `docker compose up -d`, compose에 `restart: unless-stopped` 추가 |
| 11-17 | Gradle `Could not find org.projectlombok:lombok:.` (모든 의존성 버전이 비어 있음) | `build.gradle`의 BOM 좌표가 실수로 수정됨 (`org.springframework.a`) → BOM 로드 실패로 버전 관리 전체가 깨짐 | `git diff`로 원인 확인 후 복구 |
| 11-17 | 실측표 스크린샷 자동 입력이 10초쯤 걸려서 "입력이 너무 오래 걸린다"는 피드백 | 실측이 글자로 적힌 쇼핑몰까지 전부 이미지 → LLM 으로 읽고 있었음 | 빠른 길부터 타게 변경: ① 같은 상품 링크면 저장해 둔 실측표 재사용(V3 인덱스) ② 글자 붙여넣기는 정규식 파서로 즉시 ③ 이미지는 올리기 전에 축소해서 LLM 으로 |
