# server

backend Server

## 기술 스택

| 항목 | 버전 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.5.4 |
| 빌드 | Gradle 8.14.5 (wrapper 포함, Groovy DSL) |
| 주요 의존성 | Spring Web, Validation, Data JPA, Lombok |
| DB | MariaDB (테스트는 H2 메모리 DB) |

## 실행 방법

### 준비
- JDK 17을 설치합니다. Gradle은 wrapper(`gradlew`)가 자동으로 받으므로 따로 설치하지 않아도 됩니다.
- `JAVA_HOME`은 JDK 17을 권장합니다. (Gradle 8.14가 공식 지원하는 실행 JDK는 24까지입니다.)
- IntelliJ는 **Lombok 플러그인**과 **Settings → Build → Compiler → Annotation Processors → Enable annotation processing**을 켜야 합니다.

### 빌드·테스트

```bash
# macOS / Linux
./gradlew build        # 컴파일 + 테스트
./gradlew test         # 테스트만

# Windows
gradlew.bat build
```

`gradlew`는 실행 권한이 저장된 상태로 커밋되어 있어, macOS·Linux에서 `chmod` 없이 바로 실행됩니다.

테스트는 H2 메모리 DB를 쓰므로 MariaDB 없이 실행됩니다.

### 서버 실행

DB 접속 정보는 레포에 올리지 않습니다. 두 가지 방법 중 하나를 쓰면 됩니다.

**1) 로컬 설정 파일 (권장)**

`src/main/resources/application-local.yml.example`을 같은 폴더에 `application-local.yml`로 복사하고 접속 정보를 채웁니다. 이 파일은 `.gitignore`에 등록되어 커밋되지 않습니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

**2) 환경변수**

| 환경변수 | 예시 |
| --- | --- |
| `DB_URL` | `jdbc:mariadb://localhost:3306/{DB이름}` |
| `DB_USERNAME` | 로컬 DB 계정 |
| `DB_PASSWORD` | 로컬 DB 비밀번호 |

```bash
DB_URL=jdbc:mariadb://localhost:3306/{DB이름} DB_USERNAME=... DB_PASSWORD=... ./gradlew bootRun
```

## 문서

- [문서 목록](docs/README.md)
- [의사결정 기록 / 논의 안건](docs/DECISIONS.md)
- [코드 컨벤션](docs/conventions/CODE_CONVENTION.md) · [커밋·브랜치 컨벤션](docs/conventions/COMMIT_CONVENTION.md)
- [패키지 구조](docs/architecture/PACKAGE_STRUCTURE.md)

API 명세와 테이블 명세는 팀 Notion 문서를 참고합니다.
