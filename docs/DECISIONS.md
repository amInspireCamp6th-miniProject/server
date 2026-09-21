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

## 논의 필요: 공통 (백엔드 + 프론트엔드)

| ID | 안건 | 선택지 / 제안 | 영향 범위 |
| --- | --- | --- | --- |
| C-03 | 커밋 규칙 한국어 조정 | 타입은 소문자, 제목은 한국어 명사형 종결(`~추가`, `~수정`), 마침표 없음, 50자 이내 | 양 레포 |
| C-19 | 커밋 메시지 검사 방식 | 수동 머지라 모든 커밋에 규칙이 적용됨. 로컬 `commit-msg` 훅 / CI에서 PR 커밋 검사 / 자동 검사 없이 리뷰로만 확인 | 양 레포 |
| C-04 | 커밋 scope 사용 여부 | 사용 시 도메인 목록 정의 (예: `auth`, `ingredient`, `recipe`) | 양 레포 |
| C-05 | 이슈 연동 | 꼬리말 `Closes #N`. develop 머지 시에는 이슈가 자동으로 닫히지 않음 | 작업 관리 |
| C-06 | 공통 응답 형식 | 성공/실패 JSON 구조, 에러 코드 목록 | 백엔드 `global/response`, 프론트 axios |
| C-07 | 인증 토큰 처리 | Access Token 저장 위치(메모리 / localStorage / HttpOnly 쿠키), Refresh Token 도입 여부, 401 응답 시 처리 | 백엔드 security, 프론트 인터셉터 |
| C-08 | 로그아웃 방식 | 서버 무효화(블랙리스트) / 클라이언트 토큰 삭제만 | 백엔드 auth |
| C-09 | enum 표기 | 영문 코드로 주고받고 화면 표기는 프론트 담당. `storageType` 냉장=`REFRIGERATED` 확인됨 → 냉동·실온 코드 결정 필요. `category`는 enum으로 둘지 자유 입력(OCR 결과 `가공식품` 등)으로 둘지 | API, DB, 프론트 화면 |
| C-10 | 시간 형식·시간대 | 날짜는 확정(D-11). 남은 것: `createdAt` 등 시간 포함 값의 형식, 기준 시간대 `Asia/Seoul` | API 전반 |
| C-11 | 소비기한 정책 | 임박 기준(D-3? D-7?), 만료 식재료가 임박 목록에 포함되는지 | 대시보드, 임박 목록, 추천 |
| C-12 | 목록 개수·페이징 | 대시보드 "먼저 먹어야 할 식재료" 개수, 추천 레시피 개수, 페이징 여부 | API 응답 |
| C-13 | D-Day 계산 주체 | 서버가 D-Day 값을 내려줌 (**제안**, 명세의 "식재료 상세정보, D-Day") / 프론트가 계산 | API 응답 |
| C-14 | 로컬 개발 연동 | 백엔드 포트, CORS 허용 방식 또는 Vite proxy, 프론트 API 주소 환경변수 이름 | 개발 환경 |
| C-15 | 시크릿 관리 | `.env`, `application-local.yml` 레포 제외 + 환경변수 주입. 공개 레포이므로 GitHub 시크릿 스캔·push protection 활성화 확인 (gitleaks 추가 여부 포함) | 양 레포 |
| C-17 | 식재료 수정(`PATCH`) 방식 | 보낸 필드만 수정 (**제안**) / 모든 필드 필수 | 백엔드 DTO, 프론트 수정 화면 |
| C-18 | 이미지 업로드 형식 | `multipart/form-data`, 필드명(예: `image`), 허용 형식(jpg, png 등)과 최대 용량 | 백엔드 `ocr`, 프론트 업로드 화면 |

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
| B-14 | 식재료명 표기 통일 | 문자열 비교라 `두부`와 `두부 `, `순두부`가 다르게 취급됨. 비교 전 공백 제거, 레시피 재료명 목록 안에서만 선택하게 할지 등 | Notion 「API 명세서」 |
| B-15 | 추천 대상 범위 | 명세의 "추천 대상: 등록한 식재료 전체"와 "추천 점수: 임박 식재료 가중치 합산"이 서로 다름. 같은 이름 식재료가 여러 개일 때 가중치 계산 방법(가장 임박한 1개 기준 **제안**) | Notion 「API 명세서」 |
| B-16 | 인덱스·제약조건 | 명세에 없는 인덱스와 UNIQUE 조건 추가 여부 | Notion 「테이블 명세서」 |
| B-17 | Spring Boot 버전 상향 | 3.5.4 → 3.5 최신 패치(3.5.16, 설정 한 줄 변경) / 4.0·4.1로 상향(의존성 이름 등 변경 필요). 3.5 버전은 무료 지원 종료 | [README](../README.md) |

## 논의 필요: 프론트엔드

백엔드와 맞물리는 항목 위주입니다. 프론트 내부 구현은 프론트 팀이 결정합니다.

| ID | 안건 | 선택지 / 제안 |
| --- | --- | --- |
| F-01 | 폴더 구조 확정 | Notion 예시(`api/ components/ pages/ hooks/ utils/ styles/ assets/`)에 zustand 스토어 폴더(`stores/`) 추가 여부 |
| F-02 | API 호출 모듈 | axios 인스턴스 하나에 공통 설정(기본 주소, 토큰 헤더, 401 처리 인터셉터)을 모으는 방식 |
| F-03 | 커밋 규칙 적용 | 백엔드와 같은 규칙 적용. 검사 도구는 C-19와 함께 결정 (프론트는 husky + commitlint 사용 가능) |
| F-04 | 불필요한 머지 커밋 방지 | 팀원 로컬에 `git config pull.rebase true` 설정 (현재 이력에 `Merge branch 'main' of ...` 존재) |
