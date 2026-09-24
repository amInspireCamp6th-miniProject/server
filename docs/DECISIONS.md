# 의사결정 기록

팀에서 결정한 사항과 논의가 필요한 사항을 한곳에서 관리합니다.
논의가 끝나면 해당 항목을 **확정 사항**으로 옮기고, 관련 문서의 `TBD` 표시를 갱신합니다.

- ✅ 확정 · 🟡 잠정(변경 가능) · ⬜ 논의 필요

## 확정 사항

| ID | 항목 | 결정 내용 | 관련 문서 |
| --- | --- | --- | --- |
| D-01 | 형상관리 | GitHub 조직 `amInspireCamp6th-miniProject`, 레포 `server`(백엔드) / `front`(프론트엔드) | - |
| D-02 | 브랜치 | ✅ `main`, `develop`, 개인 브랜치 `develop-ham`·`develop-na`·`develop-kang` 생성. CI는 이 브랜치들의 push·PR에서 실행 | [COMMIT_CONVENTION](conventions/COMMIT_CONVENTION.md) |
| D-03 | Java 버전 | ✅ Java 17 | - |
| D-04 | Spring Boot 버전 | ✅ 3.5.4 (Gradle 8.14.5). 3.5 버전은 무료 지원이 2026-06-30에 종료됨 → 최신 패치(3.5.16) 또는 4.x 상향 검토는 B-17 | [README](../README.md) |
| D-05 | 코드 컨벤션 기본 원칙 | ✅ 팀 Notion 「Code Convention」 채택 (네이밍, 함수, 주석, 커밋 전 확인 사항) | [CODE_CONVENTION](conventions/CODE_CONVENTION.md) |
| D-06 | 커밋 메시지 기본 형식 | ✅ `type: 제목` 형식, 타입 10종 (feat, fix, build, chore, ci, docs, style, refactor, test, perf) | [COMMIT_CONVENTION](conventions/COMMIT_CONVENTION.md) |
| D-07 | API 명세 v1 | ✅ 팀 Notion 「API 명세서」 (인증 / 식재료 이미지 인식 / 식재료 / 대시보드 / 레시피 추천). 2026-09-18 개정: 식재료 기준정보 API 삭제, 이미지 인식 API 추가 | Notion 「API 명세서」 |
| D-08 | 레포 공개 여부 | ✅ `server`, `front` 모두 공개(public) 레포 (2026-09-18 GitHub API로 확인). 규칙 세트(Rulesets), 시크릿 스캔·push protection을 무료로 사용 가능 | - |
| D-09 | 테이블 명세 | ✅ `USERS`, `INGREDIENTS`, `RECIPES`, `RECIPE_INGREDIENTS`, `RECIPE_STEPS` 5개 테이블 | Notion 「테이블 명세서」 |
| D-10 | 식재료 매칭 방식 | ✅ 식재료 기준정보 테이블 없이 `ingredient_name` 문자열 비교로 사용자 식재료와 레시피 재료를 연결 | Notion 「API 명세서」 |
| D-11 | 날짜 형식 | ✅ `yyyy-MM-dd` (API 명세 예시 기준) | Notion 「API 명세서」 |
| D-12 | 빌드 설정 | ✅ Gradle Groovy DSL (`build.gradle`), group `com.example`, version `0.0.1-SNAPSHOT` | [README](../README.md) |
| D-13 | 머지 방식 | ✅ 팀원이 직접(수동) 머지. squash / merge commit 선택이나 규칙 세트 강제는 논의 대상에서 제외 | [COMMIT_CONVENTION](conventions/COMMIT_CONVENTION.md) |
| D-14 | 브랜치 전략 | ✅ `feat/*`·`fix/*` → `develop-<이름>`(개인 통합, 백엔드 3인) → `develop` → `main`. 브랜치명은 소문자와 하이픈 | [COMMIT_CONVENTION](conventions/COMMIT_CONVENTION.md) |
| D-15 | 초기 세팅 반영 | ✅ 프로젝트 초기 세팅(문서·CI·Spring Boot 골격)은 작업 브랜치에서 `main`으로 직접 머지. 이후 `develop`과 개인 브랜치는 `main`으로 fast-forward | [COMMIT_CONVENTION](conventions/COMMIT_CONVENTION.md) |
| D-16 | DB | ✅ MariaDB (드라이버 `org.mariadb.jdbc:mariadb-java-client`). 테스트는 H2 메모리 DB(MySQL 호환 모드) | Notion 「테이블 명세서」 |
| D-17 | 보관상태 값 | ✅ `storage_type`: `REFRIGERATED`(냉장), `FROZEN`(냉동), `ROOM_TEMP`(실온). 팀 공유 DDL의 CHECK 제약조건과 동일 | Notion 「테이블 명세서」 |
| D-18 | 테이블 생성 방식 | ✅ 팀 공유 DDL 스크립트로 생성. JPA는 `ddl-auto: validate`로 엔티티와 테이블 일치 여부만 검사. 테스트(H2)는 엔티티 기준으로 자동 생성 | - |
| D-20 | Refresh Token | ✅ 도입. **JWT**(Access Token과 같은 비밀키, `type` 클레임으로 구분)로 발급하고 발급 목록을 서버 메모리에 보관. **HttpOnly 쿠키**(`refreshToken`, `Path=/api/v1/auth/refresh`)로 전달, 유효기간 14일, 재발급 시 회전(기존 토큰 폐기 후 새 쿠키 발급), 로그아웃 시 폐기·쿠키 삭제. 재발급 API `POST /api/v1/auth/refresh`는 **명세에 없어 추가 필요** | [API_RESPONSE](conventions/API_RESPONSE.md) |
| D-19 | 로그아웃 방식 | ✅ 서버 무효화(블랙리스트). 로그아웃 요청에 쓴 Access Token을 원래 만료 시각까지 거부(`401 INVALID_TOKEN`)하고, 그 회원의 Refresh Token을 모두 폐기한 뒤 쿠키를 삭제(`Max-Age=0`). 응답은 `204`(C-20). 프론트는 보관 중인 Access Token을 삭제해야 함. 저장소는 B-18 | [API_RESPONSE](conventions/API_RESPONSE.md) |

## 논의 필요: 공통 (백엔드 + 프론트엔드)

| ID | 안건 | 선택지 / 제안 | 영향 범위 |
| --- | --- | --- | --- |
| C-03 | 커밋 규칙 한국어 조정 | 타입은 소문자, 제목은 한국어 명사형 종결(`~추가`, `~수정`), 마침표 없음, 50자 이내 | 양 레포 |
| C-19 | 커밋 메시지 검사 방식 | 수동 머지라 모든 커밋에 규칙이 적용됨. 로컬 `commit-msg` 훅 / CI에서 PR 커밋 검사 / 자동 검사 없이 리뷰로만 확인 | 양 레포 |
| C-04 | 커밋 scope 사용 여부 | 사용 시 도메인 목록 정의 (예: `auth`, `ingredient`, `recipe`) | 양 레포 |
| C-05 | 이슈 연동 | 꼬리말 `Closes #N`. develop 머지 시에는 이슈가 자동으로 닫히지 않음 | 작업 관리 |
| C-06 | 공통 응답 형식 | 감싸개 없이 DTO 그대로 반환(**제안**, 현재 구현) / `ApiResponse<T>`로 감싸기. 실패는 `{code, message, errors}`. 초안과 에러 코드 목록은 [API_RESPONSE](conventions/API_RESPONSE.md) | 백엔드 `global/response`, 프론트 axios |
| C-07 | 인증 토큰 처리 | ✅ 프론트와 합의: **Access Token은 응답 본문 → `Authorization: Bearer` 헤더**(저장 위치는 프론트 재량), **Refresh Token은 HttpOnly 쿠키**(D-20). 남은 것: 배포 도메인 구성에 따른 쿠키 옵션(`secure`, `same-site`)과 401 응답 시 처리 방식. 자세한 내용은 아래 「C-07 참고」 | 백엔드 security, 프론트 인터셉터 |
| C-09 | enum 표기 | 보관상태는 확정(D-17). 남은 것: `category`를 enum으로 둘지 자유 입력(OCR 결과 `가공식품` 등)으로 둘지 | API, DB, 프론트 화면 |
| C-10 | 시간 형식·시간대 | 날짜는 확정(D-11). 남은 것: `createdAt` 등 시간 포함 값의 형식, 기준 시간대 `Asia/Seoul`. 현재 `Clock` 빈이 **서버 기본 시간대**를 따르므로, 배포 서버가 UTC면 소비기한 D-Day가 하루 어긋날 수 있음 → `Clock`을 `Asia/Seoul`로 고정할지 결정 필요 | API 전반 |
| C-11 | 소비기한 정책 | 임박 기준(D-3? D-7?), 만료 식재료가 임박 목록에 포함되는지 | 대시보드, 임박 목록, 추천 |
| C-12 | 목록 개수·페이징 | 대시보드 "먼저 먹어야 할 식재료" 개수, 추천 레시피 개수, 페이징 여부 | API 응답 |
| C-13 | D-Day 계산 주체 | 서버가 D-Day 값을 내려줌 (**제안**, 명세의 "식재료 상세정보, D-Day") / 프론트가 계산 | API 응답 |
| C-14 | 로컬 개발 연동 | 백엔드 포트, CORS 허용 방식 또는 Vite proxy, 프론트 API 주소 환경변수 이름. **서버 CORS는 구현됨**(허용 출처 기본값 `http://localhost:5173`, 환경변수 `CORS_ALLOWED_ORIGINS`로 변경). 남은 것: 프론트 개발 서버 포트 확인, Vite proxy를 쓸지 여부 | 개발 환경 |
| C-15 | 시크릿 관리 | `.env`, `application-local.yml` 레포 제외 + 환경변수 주입. 공개 레포이므로 GitHub 시크릿 스캔·push protection 활성화 확인 (gitleaks 추가 여부 포함) | 양 레포 |
| C-17 | 식재료 수정(`PATCH`) 방식 | 보낸 필드만 수정 (**제안**) / 모든 필드 필수 | 백엔드 DTO, 프론트 수정 화면 |
| C-18 | 이미지 업로드 형식 | `multipart/form-data`, 필드명(예: `image`), 허용 형식(jpg, png 등)과 최대 용량 | 백엔드 `ocr`, 프론트 업로드 화면 |
| C-20 | 삭제·로그아웃 응답 본문 | 명세는 응답 데이터 "처리 결과"인데 CLAUDE.md는 삭제 `204 No Content`(본문 없음). 로그아웃은 🟡 **`204 No Content`로 구현**(CLAUDE.md 삭제 규칙과 통일, "처리 결과"는 상태 코드로 알린다고 해석). 남은 것: 팀 확인, 명세 "처리 결과" 표기 갱신 여부 | 백엔드 auth·ingredient, 프론트 |
| C-21 | 회원가입 입력 규칙 | 명세에 없어 백엔드가 잠정으로 정함(아래 「구현하며 정한 사항」). 프론트 입력 검증과 맞출지 확인 필요: 비밀번호 8~64자·공백 없는 영문·숫자·특수문자, 이메일 100자·닉네임 50자 이하 | 백엔드 auth, 프론트 회원가입 화면 |

### C-07 참고: 401 응답 처리와 Refresh Token

현재 서버는 401을 세 가지 코드로 구분해 내려줍니다. 응답 본문의 `code` 값입니다.

| 코드 | 상황 | Refresh Token으로 재발급하면 풀리는가 |
| --- | --- | --- |
| `UNAUTHORIZED` | 토큰을 보내지 않음 | ❌ |
| `INVALID_TOKEN` | 서명 불일치·변조·형식 오류, **로그아웃한 토큰** | ❌ |
| `TOKEN_EXPIRED` | 유효기간이 지남 | ⭕ **재발급 대상은 이 경우뿐** |

- 분기는 `code`로 합니다. `message`는 화면에 보여 주는 문구라 바뀔 수 있으므로 분기에 쓰지 않습니다.
- Refresh Token이 도입되어(D-20) 두 방식 중 하나를 고르면 됩니다.
  - `code === 'TOKEN_EXPIRED'`일 때만 재발급 시도 (불필요한 요청 없음)
  - 코드를 보지 않고 401이면 재발급을 한 번 시도하고, 실패하면 로그아웃 (구현이 단순함. 무한 반복을 막는 재시도 플래그 필요)
  - 어느 쪽이든 서버는 이미 `code`를 내려주므로 추가 작업이 없습니다.
- 재발급까지 실패하면(`INVALID_REFRESH_TOKEN`) 보관 중인 Access Token을 지우고 로그인 화면으로 보냅니다. Refresh Token 쿠키는 서버가 지웁니다.
- 구분 방법으로 상태 코드를 나누는 방식(예: 만료는 419)은 표준이 아니어서 쓰지 않습니다. `WWW-Authenticate: Bearer error="invalid_token"` 헤더(RFC 6750)를 덧붙일 수는 있지만, 표준 값만으로는 만료와 변조가 구분되지 않아 본문 `code`를 대체하지 못합니다.

#### 재발급 흐름 (D-20)

```
로그인  → 본문 { accessToken(24시간), user }
        + 헤더 Set-Cookie: refreshToken=...; HttpOnly; SameSite=Lax; Path=/api/v1/auth/refresh; Max-Age=14일
        ↓ accessToken 만료 → 401 TOKEN_EXPIRED
재발급  POST /api/v1/auth/refresh   (요청 본문 없음. 브라우저가 쿠키를 자동 전송)
        → 본문 { accessToken } + 새 쿠키(회전)
        ↓ 실패하면 401 INVALID_REFRESH_TOKEN
로그아웃 → Access Token 블랙리스트 등록 + Refresh Token 전부 폐기 + 쿠키 삭제(Max-Age=0)
```

- **프론트는 Refresh Token을 저장하거나 첨부하지 않습니다.** HttpOnly라 JavaScript가 읽을 수 없고, 브라우저가 알아서 보냅니다. 회전도 서버가 새 쿠키로 덮어쓰므로 프론트 작업이 없습니다.
- **프론트는 요청에 `withCredentials: true`가 필요합니다.** 프론트(5173)와 API(8080)는 출처가 달라, 이 설정이 없으면 브라우저가 쿠키를 저장하지도 보내지도 않습니다. 서버 CORS는 `allowCredentials = true`로 열려 있습니다.
- 재발급 실패(`INVALID_REFRESH_TOKEN`)의 이유(쿠키 없음·만료·이미 사용·로그아웃)는 구분해서 알려주지 않습니다. 모두 다시 로그인해야 하는 상황이라 같은 코드로 응답합니다.
- 요청이 동시에 여러 개 401을 받으면 재발급도 여러 번 호출될 수 있습니다. 회전 방식에서는 두 번째 호출이 실패하므로, **프론트는 재발급 요청을 하나로 묶어야 합니다**(진행 중인 재발급 Promise를 공유하는 방식).
- 쿠키 옵션은 설정값입니다. 로컬은 `secure: false`, `same-site: Lax`입니다. **배포에서 프론트와 API 도메인이 다르면** `REFRESH_COOKIE_SECURE=true`, `REFRESH_COOKIE_SAME_SITE=None`(HTTPS 필수)으로 바꿔야 하고, 이 경우 CSRF 대책을 다시 검토해야 합니다. 같은 도메인 아래(`example.com` ↔ `api.example.com`)로 배포하면 `Lax` 그대로 둡니다.
- Refresh Token은 서버 메모리에 저장되므로 **서버를 재시작하면 모두 무효**가 되어 다시 로그인해야 합니다. (B-18)
- Refresh Token도 JWT입니다. Access Token과 구분하기 위해 `type` 클레임(`access` / `refresh`)을 넣고, 검증할 때 종류가 맞는지 확인합니다. 구분이 없으면 유효기간이 긴 Refresh Token을 `Authorization` 헤더에 넣어 API를 호출할 수 있게 됩니다.
- 서명만으로는 회전과 로그아웃 폐기를 할 수 없어, 발급한 Refresh Token 목록을 서버에 함께 보관합니다(`RefreshTokenStore`). 검증은 **서명·유효기간·종류 + 목록에 있는지**를 모두 확인합니다.

## 논의 필요: 백엔드

| ID | 안건 | 선택지 / 제안 | 관련 문서 |
| --- | --- | --- | --- |
| B-01 | 기본 패키지명 | 현재 `com.example.server`로 생성됨 (group `com.example` + 레포 이름, **가정**). 이대로 확정할지 | [PACKAGE_STRUCTURE](architecture/PACKAGE_STRUCTURE.md) |
| B-02 | 패키지 구조 | **도메인별 (제안)** / 계층별 | [PACKAGE_STRUCTURE](architecture/PACKAGE_STRUCTURE.md) |
| B-05 | Java 포맷 스타일 | 4칸 들여쓰기(`palantir-java-format`, **제안**) / 2칸(`google-java-format`) | [CODE_CONVENTION](conventions/CODE_CONVENTION.md) |
| B-06 | 정적 분석 도입 | Checkstyle 최소 설정, 처음엔 경고 → 안정화 후 에러로 전환 시점. 메서드 길이 기준(줄 수) | [CODE_CONVENTION](conventions/CODE_CONVENTION.md) |
| B-07 | 구조 테스트 | ArchUnit으로 패키지 의존 방향 검사 도입 여부 | [PACKAGE_STRUCTURE](architecture/PACKAGE_STRUCTURE.md) |
| B-08 | 레시피 데이터 출처 | 초기 데이터 직접 적재 / 외부 레시피 API 연동 | Notion 「API 명세서」 |
| B-09 | 추천 가중치 공식 | 예: `임박기준일 - 남은일수 + 1`, 만료 식재료 포함 여부 | Notion 「API 명세서」 |
| B-10 | 보유/추가 필요 재료 판정 | `ingredient_name` 일치 여부만 비교(**제안**) / 수량·단위까지 비교 (`RECIPE_INGREDIENTS.amount`, `unit`은 NULL 허용이라 비교가 불완전) | Notion 「API 명세서」 |
| B-11 | 타인 식재료 접근 응답 | 404 반환(**제안**, 존재 여부 노출 방지) / 403 반환 | Notion 「API 명세서」 |
| B-12 | API 문서화 | springdoc(Swagger UI) 도입 여부 (Spring Boot 3.5는 springdoc 2.x 사용) | - |
| B-13 | 이미지 인식 구현 | 외부 서비스 선택(OCR API, 이미지 인식이 되는 LLM 등), 제품명 → 일반 식재료명·카테고리 변환 방법, 인식 실패 시 응답, API 키 관리 | Notion 「API 명세서」 |
| B-14 | 식재료명 표기 통일 | 문자열 비교라 `두부`와 `두부 `, `순두부`가 다르게 취급됨. 비교 전 공백 제거, 레시피 재료명 목록 안에서만 선택하게 할지 등. **비교를 DB 쿼리에서 하느냐 Java에서 하느냐에 따라 결과가 달라짐**(아래 「B-14 참고」) | Notion 「API 명세서」 |
| B-15 | 추천 대상 범위 | 명세의 "추천 대상: 등록한 식재료 전체"와 "추천 점수: 임박 식재료 가중치 합산"이 서로 다름. 같은 이름 식재료가 여러 개일 때 가중치 계산 방법(가장 임박한 1개 기준 **제안**) | Notion 「API 명세서」 |
| B-16 | 인덱스·제약조건 | 명세에 없는 인덱스와 UNIQUE 조건 추가 여부 | Notion 「테이블 명세서」 |
| B-17 | Spring Boot 버전 상향 | 3.5.4 → 3.5 최신 패치(3.5.16, 설정 한 줄 변경) / 4.0·4.1로 상향(의존성 이름 등 변경 필요). 3.5 버전은 무료 지원 종료 | [README](../README.md) |
| B-18 | 토큰 저장소 (블랙리스트·Refresh Token) | 서버 메모리(**현재 구현**, 추가 설치 없음. 서버 재시작 시 블랙리스트가 비어 로그아웃한 토큰이 만료 전까지 다시 유효해지고, **저장된 Refresh Token도 사라져 전원 재로그인**. 서버 여러 대면 공유 안 됨) / Redis(재시작·다중 서버 대응, 설치·운영 필요) / DB 테이블(테이블 명세에 없어 명세 추가 필요). 토큰 유효기간(현재 24시간)을 줄이면 메모리 방식의 약점도 줄어듦 | - |

### B-14 참고: 식재료명 비교 위치에 따른 차이

같은 두 문자열이라도 **어디서 비교하느냐**에 따라 같다/다르다가 달라집니다. 비교 규칙을 한 곳에 두려면 비교 위치도 함께 정해야 합니다.

| 비교 위치 | `Tofu` vs `tofu` | `두부` vs `두부 `(끝 공백) | 비고 |
| --- | --- | --- | --- |
| MariaDB 쿼리 (`WHERE ingredient_name = ?`, `IN (...)`) | **같음** | **같음** | 기본 collation(`utf8mb4_general_ci`, 11.x는 `utf8mb4_uca1400_ai_ci`)이 대소문자를 구분하지 않고, 끝 공백을 무시(PAD SPACE)함. `_ai` collation은 악센트도 무시 |
| Java (`String.equals`, `Map` 키, `Set.contains`) | 다름 | 다름 | 글자 하나까지 정확히 비교 |
| 테스트 H2 (MySQL 호환 모드) | 다름 | 설정에 따라 다름 | 기본적으로 대소문자를 구분함. **MariaDB와 결과가 달라 테스트로 잡히지 않음** |

- 예: 추천 후보 레시피를 DB 쿼리로 찾고, 보유/추가 필요 재료 판정(B-10)은 Java에서 하면 `Tofu`를 가진 사용자에게 `tofu` 레시피가 추천되지만 상세 화면에서는 "추가 필요 재료"로 나올 수 있습니다.
- 한글에는 대소문자가 없으므로 주로 영문 식재료명(예: `Tofu`, `Bacon`)과 공백에서 생깁니다. 중간 공백(`방울 토마토` / `방울토마토`)은 DB와 Java 모두 다르게 취급합니다.
- 실제 collation은 팀 공유 DDL 스크립트와 DB 설정에 따라 다르므로 확인이 필요합니다. (`SHOW FULL COLUMNS FROM INGREDIENTS;`)

선택지:

| 방식 | 내용 | 장점 | 단점 |
| --- | --- | --- | --- |
| 저장 시 정규화 | 등록·수정할 때 식재료명을 한 가지 규칙(앞뒤 공백 제거, 연속 공백 하나로, 영문 소문자)으로 바꿔 저장. 레시피 데이터도 같은 규칙으로 적재 | DB와 Java 어디서 비교해도 결과가 같음 | 사용자가 입력한 표기(`Tofu`)가 그대로 보이지 않음 |
| 비교 시 정규화 | 저장은 그대로, 비교는 항상 Java의 정규화 메서드 하나로만 수행. DB 쿼리로 이름을 비교하지 않음 | 입력 표기 유지 | 추천 시 레시피 재료를 모두 읽어 와 Java에서 비교해야 함 |
| DB collation에 맡김 | 비교를 항상 DB 쿼리로만 수행 | 구현이 간단 | 규칙이 DB 설정에 숨어 있고, H2 테스트와 결과가 다름. 중간 공백은 해결 안 됨 |
| 선택 입력 | 식재료명을 레시피 재료명 목록에서만 고르게 함 | 표기 차이가 원천적으로 없음 | OCR 결과(`ingredientName`)를 목록에 맞춰 변환해야 함 (B-13과 연결) |

## 구현하며 정한 사항 (🟡 잠정, 팀 확인 필요)

인증 작업 중 명세에 없어 백엔드가 잠정으로 정한 내용입니다. 이견이 있으면 해당 안건에서 논의합니다.

| 항목 | 내용 | 관련 안건 |
| --- | --- | --- |
| 이메일 정규화 | 저장·조회 시 앞뒤 공백 제거 + 소문자 변환. `User@Example.com`과 `user@example.com`은 같은 계정 | C-21 |
| 닉네임 | 앞뒤 공백 제거. 중복 허용 (명세에 UNIQUE 없음) | C-21 |
| 비밀번호 | 8~64자, 공백 없는 ASCII(영문·숫자·특수문자)만. BCrypt가 72바이트를 넘는 입력을 처리하지 못해서 제한 | C-21 |
| 로그인 실패 | 이메일이 없는 경우와 비밀번호가 틀린 경우 모두 `401 INVALID_CREDENTIALS` (가입 여부 노출 방지) | C-06 |
| JWT 내용 | 서명 HS256. `sub`(회원 ID), `type`(`access`/`refresh`), `jti`(중복 방지), `iat`, `exp`만 담음 (이메일 등 개인정보 제외). Access·Refresh 토큰이 같은 비밀키를 쓰므로 `type`으로 구분. 비밀키는 환경변수 `JWT_SECRET`(Base64, 32바이트 이상) | C-07, C-15, D-20 |
| 토큰 유효기간 | Access Token 24시간 (`jwt.access-token-validity`), Refresh Token 14일 (`jwt.refresh-token-validity`, 쿠키 `Max-Age`도 같은 값) | D-20, B-18 |
| 에러 코드 구조 | 도메인별 enum(`AuthErrorCode`, `UserErrorCode` 등)이 `ErrorCode` 인터페이스 구현 | C-06 |
| `updated_at` | 등록 시 NULL, 수정될 때만 채움 (`@EnableJpaAuditing(modifyOnCreate = false)`, 테이블 명세의 기본값 NULL과 일치) | D-09 |
| 테이블 이름 대소문자 | `@Table(name = "USERS")`처럼 DDL과 같은 대문자. Linux MariaDB는 테이블 이름 대소문자를 구분 | D-18 |
| CORS | `SecurityConfig`에서 설정. 허용 출처는 `cors.allowed-origins`(기본 `http://localhost:5173`), 허용 메서드 GET·POST·PATCH·DELETE·OPTIONS, `allowCredentials=true`(Refresh Token 쿠키 전송에 필요), 사전 요청 캐시 1시간, 적용 경로 `/api/**` | C-14, C-07 |
| 컴파일 인코딩 | `build.gradle`에 UTF-8 명시 (Windows 기본 MS949로 컴파일하면 한글 메시지가 깨짐) | - |
| 기본 계정 비활성화 | `UserDetailsServiceAutoConfiguration` 제외. JWT만 쓰므로 기본 계정이 필요 없고, 제외하지 않으면 자동 생성 비밀번호가 시작 로그에 찍힘 | - |

## 논의 필요: 프론트엔드

백엔드와 맞물리는 항목 위주입니다. 프론트 내부 구현은 프론트 팀이 결정합니다.

| ID | 안건 | 선택지 / 제안 |
| --- | --- | --- |
| F-01 | 폴더 구조 확정 | Notion 예시(`api/ components/ pages/ hooks/ utils/ styles/ assets/`)에 zustand 스토어 폴더(`stores/`) 추가 여부 |
| F-02 | API 호출 모듈 | axios 인스턴스 하나에 공통 설정(기본 주소, 토큰 헤더, 401 처리 인터셉터)을 모으는 방식 |
| F-03 | 커밋 규칙 적용 | 백엔드와 같은 규칙 적용. 검사 도구는 C-19와 함께 결정 (프론트는 husky + commitlint 사용 가능) |
| F-04 | 불필요한 머지 커밋 방지 | 팀원 로컬에 `git config pull.rebase true` 설정 (현재 이력에 `Merge branch 'main' of ...` 존재) |
