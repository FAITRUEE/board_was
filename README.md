# Board API

Java + Spring Boot + Gradle로 구축한 게시판 백엔드 API 서버입니다.

## 기술 스택

- **Java** 17+
- **Spring Boot** 3.x
- **Gradle** 8.x
- **Spring Data JPA**
- **H2 Database** (개발용) / **MySQL** (프로덕션)

## 주요 기능

- ✅ 게시글 CRUD API
- ✅ RESTful API 설계
- ✅ JPA를 통한 데이터 관리
- ✅ CORS 설정

## API 엔드포인트

### 게시글 (Posts)

```
GET    /api/posts          # 게시글 목록 조회
GET    /api/posts/{id}     # 게시글 상세 조회
POST   /api/posts          # 게시글 작성
PUT    /api/posts/{id}     # 게시글 수정
DELETE /api/posts/{id}     # 게시글 삭제
```

## 시작하기

### 필수 요구사항

- JDK 17 이상
- Gradle 8.x 이상

### 설치 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test
```

### 환경 설정

`application.yml` 또는 `application.properties` 파일에서 설정을 변경할 수 있습니다:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:boarddb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 프로젝트 구조

```
src/main/java/
├── controller/     # REST 컨트롤러
├── service/        # 비즈니스 로직
├── repository/     # 데이터 접근 계층
├── entity/         # JPA 엔티티
├── dto/            # 데이터 전송 객체
└── config/         # 설정 클래스
```

## 개발 가이드

### 코드 스타일

- Google Java Style Guide 준수
- Lombok 사용으로 보일러플레이트 코드 최소화

### 커밋 컨벤션

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드
- `chore`: 빌드 업무 수정

## 라이선스

MIT
