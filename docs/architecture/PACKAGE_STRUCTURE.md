# 백엔드 패키지 구조

> 상태: **제안** ([B-01, B-02](../DECISIONS.md) 결정 후 확정)

API 명세의 도메인(인증, 이미지 인식, 식재료, 대시보드, 레시피)에 맞춰 **도메인별로 패키지를 나눕니다.**
레시피 추천처럼 로직이 큰 도메인이 있어서, 계층별보다 도메인별로 묶는 편이 코드를 찾고 고치기 쉽습니다.

## 구조

```
src/main/java/com/example/server/     # 기본 패키지명 (가정, B-01에서 확정)
├── global/                           # 여러 도메인이 함께 쓰는 공통 영역
│   ├── config/                       # SecurityConfig(인증·CORS), CorsProperties, WebConfig, JpaAuditingConfig, ClockConfig
│   ├── security/                     # JwtProvider, JwtAuthenticationFilter, TokenBlacklist, RefreshTokenStore, 쿠키 발급, @LoginUser 주입
│   ├── exception/                    # ErrorCode(인터페이스), GlobalErrorCode, BusinessException, GlobalExceptionHandler
│   ├── response/                     # ErrorResponse 실패 응답. 성공 응답은 감싸지 않음 (초안: API_RESPONSE, C-06)
│   └── entity/                       # BaseTimeEntity(created_at, updated_at), BaseCreatedTimeEntity(created_at만)
│
├── auth/                             # /api/v1/auth/**
│   ├── controller/                   # AuthController: signup, login, refresh, logout, me
│   ├── service/                      # AuthService
│   ├── exception/                    # AuthErrorCode
│   └── dto/                          # SignupRequest, LoginRequest, LoginResponse, TokenResponse, LoginResult, TokenResult
│
├── user/                             # USERS 테이블
│   ├── entity/                       # User
│   ├── repository/                   # UserRepository
│   ├── service/                      # UserService: 가입·조회 (이메일 정규화)
│   ├── exception/                    # UserErrorCode
│   └── dto/                          # UserResponse
│
├── ocr/                              # /api/v1/ocr/ingredients (저장 없이 인식 결과만 반환)
│   ├── controller/                   # OcrController
│   ├── service/                      # OcrService: 이미지 검증, 인식 결과 가공
│   ├── client/                       # 외부 인식 서비스 호출 (서비스 선택 TBD: B-13)
│   └── dto/                          # OcrIngredientResponse
│
├── ingredient/                       # /api/v1/ingredients/** (expiring, expired 포함), INGREDIENTS 테이블
│   ├── controller/                   # IngredientController
│   ├── service/                      # IngredientService
│   ├── entity/                       # Ingredient, StorageType(enum)
│   ├── domain/                       # ExpirationPolicy: 임박/만료 판정, D-Day 계산
│   ├── repository/                   # IngredientRepository
│   └── dto/
│
├── dashboard/                        # /api/v1/dashboard (엔티티 없음, 조회·집계만)
│   ├── controller/                   # DashboardController
│   ├── service/                      # DashboardService
│   └── dto/                          # DashboardResponse
│
└── recipe/                           # /api/v1/recipes/**, RECIPES·RECIPE_INGREDIENTS·RECIPE_STEPS 테이블
    ├── controller/                   # RecipeController
    ├── service/                      # RecipeService, RecipeRecommendationService
    ├── entity/                       # Recipe, RecipeIngredient, RecipeStep
    ├── repository/
    └── dto/
```

## 도메인 간 의존 규칙

| 도메인 | 참조할 수 있는 도메인 |
| --- | --- |
| `auth` | `user`, `global` |
| `user` | `global` |
| `ocr` | `global` |
| `ingredient` | `user`, `global` |
| `dashboard` | `ingredient`, `global` |
| `recipe` | `ingredient`, `global` |
| `global` | 없음 |

- 표에 없는 방향의 참조는 금지합니다. (예: `ingredient`가 `recipe`를 참조하면 안 됨)
- 다른 도메인의 기능이 필요하면 그 도메인의 **service**를 호출합니다. repository를 직접 참조하는 것은 조회 전용일 때만 허용합니다. `TBD`
- 이 규칙은 ArchUnit 테스트로 자동 검사할 수 있습니다. ([B-07](../DECISIONS.md))

## 설계 메모

- **`auth`와 `user` 분리**: 로그인 회원 정보는 여러 도메인이 참조하므로 엔티티는 `user`에 둡니다. 패키지와 엔티티 이름은 테이블 명세(`USERS`, `user_id`)에 맞췄습니다.
  - 엔티티 이름 `User`는 Spring Security의 `org.springframework.security.core.userdetails.User`와 이름이 같습니다. 같은 파일에서 둘 다 쓸 때는 패키지명까지 적어서 구분합니다.
- **`ocr`은 저장하지 않습니다**: 인식 결과를 돌려주기만 하고, 저장은 사용자가 확인한 뒤 식재료 등록 API가 합니다. 그래서 `ingredient`를 참조하지 않습니다.
  - 외부 서비스 호출은 `client/`에 모아서, 서비스를 바꾸거나 테스트용 가짜 구현으로 바꾸기 쉽게 합니다.
- **임박/만료 조회는 `ingredient`에 둡니다**: 명세에서는 대시보드 섹션에 있지만 URL이 `/ingredients/**`이고 반환값도 식재료 목록입니다. `/ingredients/expiring`처럼 고정된 경로는 Spring이 `/ingredients/{ingredientId}`보다 먼저 매칭합니다.
- **소비기한 판정은 `ExpirationPolicy` 한 곳에서만 합니다**: 식재료 상세의 D-Day, 대시보드, 레시피 추천이 같은 기준을 써야 합니다. 오늘 날짜는 `Clock`을 주입받아 계산해서 테스트에서 날짜를 고정할 수 있게 합니다.
- **식재료명 비교 규칙도 한 곳에 둡니다**: 추천과 보유 재료 판정이 모두 `ingredient_name` 문자열 비교를 쓰므로, 비교 전 정리(앞뒤 공백 제거 등)를 한 메서드로 통일합니다. 규칙은 [B-14](../DECISIONS.md)에서 정합니다.
- **추천 점수 계산은 `RecipeRecommendationService`로 분리**해 단위 테스트합니다.
- 식재료 조회·수정·삭제는 **본인 소유인지 검증**합니다. ([B-11](../DECISIONS.md))
- **에러 코드는 도메인별 enum**(`ingredient/exception/IngredientErrorCode` 등)으로 만들어 `ErrorCode`를 구현하고, `BusinessException`으로 던집니다. 응답 형식은 [API_RESPONSE](../conventions/API_RESPONSE.md)를 따릅니다.
- **로그아웃은 서버 무효화**입니다([D-19](../DECISIONS.md)). 로그아웃한 토큰은 `TokenBlacklist`에 올라가 인증 필터에서 거부되고, 그 회원의 Refresh Token도 모두 폐기됩니다. 두 저장소 모두 서버 메모리입니다([B-18](../DECISIONS.md)).
- **Refresh Token도 JWT**입니다([D-20](../DECISIONS.md)). `type` 클레임으로 Access Token과 구분하고, `JwtProvider`가 종류까지 확인합니다. 회전·로그아웃 폐기를 위해 발급 목록을 `RefreshTokenStore`에 함께 보관합니다.
- **Refresh Token은 HttpOnly 쿠키로 주고받습니다**([C-07](../DECISIONS.md)). 쿠키 생성·삭제는 `RefreshTokenCookieFactory` 한 곳에서 하고, 환경별 옵션(`secure`, `same-site`, `path`)은 `RefreshCookieProperties`로 분리했습니다. 토큰을 본문과 쿠키 중 어디에 담을지는 전달 방식의 문제라 **컨트롤러**가 정하고, 서비스는 값만 돌려줍니다(`LoginResult`, `TokenResult`).

## 다른 도메인에서 공통 기능 쓰는 법

| 필요한 것 | 사용법 |
| --- | --- |
| 로그인 사용자 ID | 컨트롤러 파라미터에 `@LoginUser Long userId`. 인증 정보가 없으면 401 |
| 테스트에서 로그인 상태 | `@SpringBootTest` + `@AutoConfigureMockMvc` 테스트에 `@WithLoginUser(userId = 1L)`. 토큰 필터를 거치지 않으므로 토큰 자체가 필요한 테스트(로그아웃 등)는 실제 로그인으로 토큰을 받아 씀 |
| 오늘 날짜 | `Clock`을 주입받아 `LocalDate.now(clock)`. 테스트에서는 `Clock.fixed(...)`로 고정. 시간대는 [C-10](../DECISIONS.md) |
| 생성·수정 시각 | `created_at`·`updated_at`이 있는 테이블은 `BaseTimeEntity`, `created_at`만 있으면 `BaseCreatedTimeEntity` 상속 |
| 인증 없이 열어야 하는 API | `SecurityConfig`의 공개 경로에 추가. 현재 공개 경로는 회원가입·로그인뿐 (API 명세의 인증 X 항목) |
| 브라우저에서 호출되는 새 경로 | `/api/**`는 CORS가 이미 적용됨. 그 밖의 경로를 추가하면 `SecurityConfig`의 CORS 등록 경로도 함께 확인 |

## 엔티티

테이블 컬럼과 타입은 팀 Notion 「테이블 명세서」를 따릅니다.

| 엔티티 | 테이블 | 주요 필드 |
| --- | --- | --- |
| `User` | `users` | id, email(unique), password(암호화), nickname |
| `Ingredient` | `ingredients` | id, user, productName, ingredientName, category, quantity, unit, purchaseDate, expirationDate, storageType |
| `Recipe` | `recipes` | id, name, description, cookingTime, imageUrl |
| `RecipeIngredient` | `recipe_ingredients` | id, recipe, ingredientName, amount, unit |
| `RecipeStep` | `recipe_steps` | id, recipe, stepNo, description |
