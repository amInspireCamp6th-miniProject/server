# API 응답 형식

> 상태: **초안** ([C-06](../DECISIONS.md) 결정 후 확정). 현재 인증 API는 아래 **A안**으로 구현되어 있습니다.

성공 응답과 실패 응답의 JSON 구조를 정합니다. 프론트엔드 axios 인터셉터와 백엔드 `global/response`가 이 형식을 따릅니다.

## 선택지

| | A안: 감싸개 없음 (**제안**, 현재 구현) | B안: 공통 감싸개 `ApiResponse<T>` |
| --- | --- | --- |
| 성공 | DTO를 그대로 반환 `{ "userId": 1, ... }` | `{ "success": true, "data": { "userId": 1, ... }, "error": null }` |
| 실패 | `{ "code": "...", "message": "...", "errors": [...] }` | `{ "success": false, "data": null, "error": { "code": "...", "message": "..." } }` |
| 성공/실패 구분 | HTTP 상태 코드 (2xx / 4xx·5xx) | HTTP 상태 코드 + `success` 필드 |
| 삭제(`204 No Content`) | 본문 없음. 다른 응답과 규칙이 같음 | 204는 본문을 담을 수 없어 **삭제만 예외**가 되거나, 200 + `data: null`로 바꿔야 함 (CLAUDE.md의 204 규칙과 충돌) |
| 명세와의 관계 | 명세에 적힌 필드만 내려감 | 명세에 없는 `success`, `data`, `error` 필드가 모든 응답에 추가됨 |
| 프론트 처리 | `response.data`가 곧 DTO. 실패는 `error.response.data.code` | `response.data.data`로 한 단계 더 꺼냄. axios 인터셉터에서 풀어 줄 수 있음 |
| 나중에 공통 필드 추가 | 어려움 (모든 응답 구조가 바뀜) | 쉬움 (예: 페이징 정보, 서버 시각) |

### A안을 제안하는 이유

- **C-06이 결정되지 않은 상태에서 명세에 없는 필드를 만들지 않기 위해서입니다.** CLAUDE.md는 "문서에서 확정되지 않은 응답 필드를 임의로 만들지 않는다"고 정하고 있습니다. `success`, `data`는 명세에 없는 필드라, 명세에 적힌 필드만 내려보내는 A안이 명세와 가장 가깝습니다.
- HTTP 상태 코드가 이미 성공/실패를 알려 주므로 `success` 필드는 같은 정보를 두 번 보내는 셈입니다. axios도 4xx·5xx를 자동으로 에러로 처리합니다.
- 삭제 성공 `204 No Content`는 본문이 없어서, 감싸개를 쓰면 삭제만 예외가 됩니다.
- 실패 응답은 A안에서도 `ErrorResponse` 하나로 통일되어 있어, 프론트는 에러일 때 `code`만 보고 분기하면 됩니다.

B안이 나은 경우: 페이징 정보처럼 모든 응답에 공통으로 붙일 값이 생길 예정이거나, 프론트가 한 가지 모양으로만 응답을 다루고 싶을 때입니다. 목록 페이징 여부는 [C-12](../DECISIONS.md)와 함께 보면 됩니다.

### 방식을 바꿀 때 드는 비용

B안으로 바꾸면 컨트롤러의 반환 타입(`ResponseEntity<UserResponse>` → `ResponseEntity<ApiResponse<UserResponse>>`)과 테스트의 JSON 경로(`$.email` → `$.data.email`)를 모두 고쳐야 합니다. 현재는 인증 API 5개뿐이라 비용이 작습니다. 다른 도메인 API가 늘어나기 전에 결정하는 편이 좋습니다.

## A안 상세 (현재 구현)

### 성공 응답

| 경우 | 상태 코드 | 본문 |
| --- | --- | --- |
| 등록 | `201 Created` | 등록된 데이터 DTO |
| 조회·수정 | `200 OK` | DTO 또는 DTO 배열 |
| 삭제 | `204 No Content` | 없음 |
| 로그아웃 | `204 No Content` | 없음. 돌려줄 데이터가 없어 삭제와 같은 규칙 적용 ([C-20](../DECISIONS.md), 🟡 잠정) |

```json
// POST /api/v1/auth/login → 200
// 헤더: Set-Cookie: refreshToken=...; HttpOnly; SameSite=Lax; Path=/api/v1/auth/refresh
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { "userId": 1, "email": "user@example.com", "nickname": "닉네임" }
}

// POST /api/v1/auth/refresh → 200 (요청 본문 없음. 쿠키로 인증)
// 헤더: Set-Cookie: refreshToken=...  ← 회전된 새 토큰
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Refresh Token은 응답 본문에 넣지 않습니다. HttpOnly 쿠키로만 주고받습니다. ([C-07](../DECISIONS.md), [D-20](../DECISIONS.md))

### 실패 응답

```json
// 일반 에러
{ "code": "DUPLICATE_EMAIL", "message": "이미 사용 중인 이메일입니다." }

// 입력값 검증 실패일 때만 errors가 붙음
{
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다.",
  "errors": [
    { "field": "email", "reason": "이메일 형식이 올바르지 않습니다." }
  ]
}
```

- `code`: 프론트가 분기에 쓰는 값. 에러 코드 enum 이름과 같습니다.
- `message`: 사용자에게 그대로 보여 줄 수 있는 한국어 문장.
- `errors`: 입력값 검증 실패(`INVALID_INPUT`)일 때만 포함. 거부된 입력값은 비밀번호가 섞일 수 있어 넣지 않습니다.
- Security 필터 단계의 에러(401, 403)도 같은 형식으로 응답합니다. (`ErrorResponseWriter`)

### 에러 코드 목록 (현재)

에러 코드는 도메인별 enum으로 나눠 `ErrorCode` 인터페이스를 구현합니다. 한 파일에 모으면 여러 명이 같은 파일을 동시에 수정하게 되기 때문입니다.

| 코드 | 상태 | 위치 | 발생 상황 |
| --- | --- | --- | --- |
| `INVALID_INPUT` | 400 | `GlobalErrorCode` | 검증 실패, JSON 형식 오류, 파라미터 누락·타입 불일치 |
| `UNAUTHORIZED` | 401 | `GlobalErrorCode` | 토큰 없이 인증이 필요한 API 호출 |
| `INVALID_TOKEN` | 401 | `GlobalErrorCode` | 서명 불일치, 변조, 형식 오류, **로그아웃한 토큰**, Refresh Token을 `Authorization` 헤더에 넣은 경우 |
| `TOKEN_EXPIRED` | 401 | `GlobalErrorCode` | 유효기간이 지난 토큰 |
| `ACCESS_DENIED` | 403 | `GlobalErrorCode` | 권한 부족 (현재는 역할 구분이 없어 발생하지 않음) |
| `RESOURCE_NOT_FOUND` | 404 | `GlobalErrorCode` | 없는 URL |
| `METHOD_NOT_ALLOWED` | 405 | `GlobalErrorCode` | 지원하지 않는 HTTP Method |
| `INTERNAL_SERVER_ERROR` | 500 | `GlobalErrorCode` | 처리되지 않은 예외 (서버 로그에만 상세 기록) |
| `INVALID_CREDENTIALS` | 401 | `AuthErrorCode` | 로그인 실패. 이메일이 없는 경우와 비밀번호가 틀린 경우를 구분하지 않음 (가입 여부 노출 방지) |
| `INVALID_REFRESH_TOKEN` | 401 | `AuthErrorCode` | 재발급 실패. 없는·만료된·이미 사용한·로그아웃으로 폐기된 Refresh Token을 구분하지 않음 |
| `DUPLICATE_EMAIL` | 409 | `UserErrorCode` | 이미 가입된 이메일로 회원가입 |
| `USER_NOT_FOUND` | 404 | `UserErrorCode` | 토큰은 유효하지만 회원이 DB에 없음 |

다른 도메인은 `ingredient/exception/IngredientErrorCode`처럼 각자 enum을 만들어 추가합니다. 코드 이름은 대문자와 언더스코어를 씁니다.

### 프론트 처리 참고 (401)

| 코드 | 권장 처리 |
| --- | --- |
| `TOKEN_EXPIRED` | **재발급 대상**. `POST /api/v1/auth/refresh` 호출 후 원래 요청 재시도 ([C-07](../DECISIONS.md)) |
| `INVALID_TOKEN`, `UNAUTHORIZED` | Access Token 삭제 후 로그인 화면으로 이동. 재발급해도 풀리지 않음 |
| `INVALID_CREDENTIALS` | 로그인 화면에서 `message` 표시 (이동하지 않음) |
| `INVALID_REFRESH_TOKEN` | 재발급 실패. Access Token을 지우고 로그인 화면으로 이동 (Refresh Token 쿠키는 서버가 지움) |

- 분기는 `code`로 합니다. `message`는 화면 표시용 문구라 바뀔 수 있으므로 분기 조건으로 쓰지 않습니다.
- 코드로 구분하지 않고 401이면 재발급을 한 번 시도한 뒤 실패하면 로그아웃하는 방식도 가능합니다. 이때는 무한 반복을 막는 재시도 플래그가 필요합니다. ([C-07 참고](../DECISIONS.md))

## B안을 채택할 경우 구현 메모

- `global/response/ApiResponse<T>`를 record로 만들고 `ApiResponse.success(data)`, `ApiResponse.failure(errorCode)` 정적 메서드를 둡니다.
- 컨트롤러마다 감싸는 방식과 `ResponseBodyAdvice`로 자동으로 감싸는 방식이 있습니다. 자동 방식은 코드가 짧지만 204, 에러 응답, 문자열 응답을 따로 예외 처리해야 합니다.
- `GlobalExceptionHandler`, `ErrorResponseWriter`도 `ApiResponse` 형식으로 바꿔야 합니다.
