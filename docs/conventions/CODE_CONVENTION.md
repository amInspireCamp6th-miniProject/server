# 백엔드 코드 컨벤션

팀 Notion 「Code Convention」의 원칙을 백엔드(Java / Spring)에 맞게 옮긴 문서입니다.
`TBD` 항목은 [DECISIONS.md](../DECISIONS.md)에서 결정되는 대로 채웁니다.

## 기본 원칙

- 코드는 팀원 모두가 읽기 쉽고 유지보수하기 쉽게 작성합니다.
- 기능 구현보다도 일관된 네이밍, 명확한 구조, 불필요한 중복 제거를 중요하게 생각합니다.

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스, 인터페이스, enum, record | PascalCase | `IngredientService`, `StorageType` |
| 메서드 | camelCase, 동사로 시작 | `findExpiringIngredients()`, `calculateScore()` |
| 변수, 파라미터, 필드 | camelCase, 의미가 드러나게 | `expirationDate`, `remainingDays` |
| 상수 (`static final`) | 대문자 + 언더스코어 | `MAX_RETRY_COUNT`, `EXPIRING_THRESHOLD_DAYS` |
| enum 값 | 대문자 + 언더스코어 | `FRIDGE`, `FREEZER` |
| 패키지 | 소문자, 단어 연결 | `ingredient`, `recipe` |

역할별 클래스 이름은 접미사로 구분합니다.

| 역할 | 접미사 | 예시 |
| --- | --- | --- |
| 컨트롤러 | `Controller` | `IngredientController` |
| 서비스 | `Service` | `RecipeRecommendationService` |
| 리포지토리 | `Repository` | `UserRepository` |
| 요청 DTO | `Request` | `SignupRequest` |
| 응답 DTO | `Response` | `DashboardResponse` |

```java
private static final int EXPIRING_THRESHOLD_DAYS = 3;

public List<IngredientResponse> findExpiringIngredients(Long userId) {
    // ...
}
```

## 함수(메서드) 작성 규칙

- 하나의 메서드는 하나의 역할만 담당합니다.
- 너무 긴 메서드는 기능 단위로 분리합니다. (길이 기준: `TBD` [B-06](../DECISIONS.md))
- 중복 로직은 공통 메서드나 클래스로 분리합니다.
- 메서드 이름만 보고 역할을 이해할 수 있게 작성합니다.

## 주석 규칙

코드만으로 이해하기 어려운 부분에만 작성하고, 코드 내용을 되풀이하는 주석은 쓰지 않습니다.

```java
// 좋은 예: 왜 그렇게 하는지 설명
// 만료된 식재료는 추천 점수 계산에서 제외한다. (섭취 권장 대상이 아니므로)

// 피해야 할 예: 코드 내용 반복
// count를 1 증가시킨다.
count++;
```

## 패키지 구조

[PACKAGE_STRUCTURE.md](../architecture/PACKAGE_STRUCTURE.md)를 따릅니다.

## 포맷

| 항목 | 규칙 |
| --- | --- |
| 들여쓰기 | `TBD` (4칸 제안, [B-05](../DECISIONS.md)) |
| 포맷터 | `TBD` (Spotless + palantir-java-format 제안) |
| 파일 끝 | 빈 줄 하나로 끝냄 |
| 인코딩 / 줄바꿈 | UTF-8 / LF |

포맷터를 도입하면 `./gradlew spotlessApply`로 자동 정렬합니다.

## 커밋 전 확인 사항

- [ ] 불필요한 콘솔 출력 제거 (`System.out.println`, `e.printStackTrace()` 대신 로거 사용)
- [ ] 사용하지 않는 변수, import 제거
- [ ] API Key, 비밀번호, 개인정보 포함 여부 확인 (`application-local.yml`, `.env`는 커밋 금지)
- [ ] 로컬에서 `./gradlew build` 성공 확인
- [ ] 팀 코드 컨벤션 준수 여부 확인

## 규칙별 검사 방식

사람이 판단해야 하는 규칙은 PR 리뷰로, 도구가 판단할 수 있는 규칙은 CI로 검사합니다.

| 규칙 | 검사 방식 | 상태 |
| --- | --- | --- |
| 이름 형식 (PascalCase, camelCase, 상수) | Checkstyle `TypeName`, `MethodName`, `MemberName`, `LocalVariableName`, `ParameterName`, `ConstantName` | `TBD` [B-06](../DECISIONS.md) |
| 콘솔 출력 금지 | Checkstyle `RegexpSinglelineJava` | `TBD` [B-06](../DECISIONS.md) |
| 미사용 지역변수, import | Checkstyle `UnusedLocalVariable`, `UnusedImports` | `TBD` [B-06](../DECISIONS.md) |
| 메서드 길이 | Checkstyle `MethodLength` (기본값 150줄이므로 기준을 따로 지정) | `TBD` [B-06](../DECISIONS.md) |
| 포맷 | Spotless | `TBD` [B-05](../DECISIONS.md) |
| 패키지 의존 방향 | ArchUnit 테스트 | `TBD` [B-07](../DECISIONS.md) |
| 시크릿 포함 | GitHub 시크릿 스캔·push protection (공개 레포 무료). 필요 시 gitleaks 추가 (조직 레포는 무료 라이선스 키 필요) | `TBD` [C-15](../DECISIONS.md) |
| 빌드·테스트 | CI `test`, `build` 단계 | 준비됨 (프로젝트 생성 후 동작) |
| 이름의 의미, 메서드 역할, 주석, 중복 로직 | PR 리뷰 | PR 템플릿 체크리스트 |

도입 시 참고 사항:
- Checkstyle은 위 표의 모듈만 넣은 최소 설정을 사용합니다. `google_checks.xml`에는 들여쓰기 규칙이 있어 포맷터와 충돌합니다.
- Checkstyle 기본 설정은 위반을 경고로만 처리해 CI가 통과합니다. 강제하려면 `maxWarnings = 0` 또는 `severity=error`로 설정합니다.
- 최신 Checkstyle은 Java 21 이상이 필요합니다. Java 17에서 실행되는 버전으로 `toolVersion`을 고정합니다.
