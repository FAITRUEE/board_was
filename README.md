# Board API

Java + Spring Boot + Gradle로 구축한 게시판 백엔드 API 서버입니다.

## 기술 스택

- **Java** 17+
- **Spring Boot** 3.x
- **Gradle** 8.x
- **Spring Data JPA**
- **H2 Database** (개발용) / **MySQL** (프로덕션)

## 주요 기능

- 회원가입/로그인 (JWT 인증)
- 게시글 CRUD
- 댓글 CRUD
- 좋아요 토글
- 조회수 증가
- 페이지네이션

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

## 📁 백엔드 파일 구조 및 설명

### 📦 Entity (엔티티)
**src/main/java/com/example/board/entity/**

- `User.java` - 사용자 엔티티 (id, email, username, password)
- `Post.java` - 게시글 엔티티 (제목, 내용, 작성자, 조회수, 좋아요수, 댓글수)
- `Comment.java` - 댓글 엔티티 (내용, 게시글, 작성자)
- `PostLike.java` - 좋아요 엔티티 (게시글, 사용자, 중복 방지)

### 🗄️ Repository (데이터 접근 계층)
**src/main/java/com/example/board/repository/**

- `UserRepository.java` - 사용자 조회 (이메일, 사용자명 중복 체크)
- `PostRepository.java` - 게시글 조회 (페이지네이션, 정렬)
- `CommentRepository.java` - 댓글 조회 (게시글별 댓글 목록)
- `PostLikeRepository.java` - 좋아요 조회 (중복 체크, 좋아요 수 카운트)

### 🔐 Security (보안)
**src/main/java/com/example/board/security/**

- `JwtTokenProvider.java` - JWT 토큰 생성/검증 (256bit HS256)
- `JwtAuthenticationFilter.java` - HTTP 요청에서 JWT 토큰 추출 및 인증

### ⚙️ Config (설정)
**src/main/java/com/example/board/config/**

- `SecurityConfig.java` - Spring Security 설정 (JWT 인증, CORS, 권한 관리)

### 💼 Service (비즈니스 로직)
**src/main/java/com/example/board/service/**

- `AuthService.java` - 회원가입/로그인 처리, 비밀번호 암호화
- `PostService.java` - 게시글 CRUD, 조회수 증가, 좋아요 상태 확인
- `CommentService.java` - 댓글 CRUD, 작성자 권한 확인
- `PostLikeService.java` - 좋아요 토글 (추가/취소), 중복 방지

### 🎮 Controller (API 엔드포인트)
**src/main/java/com/example/board/controller/**

- `AuthController.java`
  - `POST /api/auth/signup` - 회원가입
  - `POST /api/auth/login` - 로그인
  
- `PostController.java`
  - `GET /api/posts` - 게시글 목록 (페이지네이션)
  - `GET /api/posts/{id}` - 게시글 상세
  - `POST /api/posts` - 게시글 작성 (인증 필요)
  - `PUT /api/posts/{id}` - 게시글 수정 (작성자만)
  - `DELETE /api/posts/{id}` - 게시글 삭제 (작성자만)
  - `POST /api/posts/{id}/views` - 조회수 증가
  - `POST /api/posts/{id}/like` - 좋아요 토글 (인증 필요)
  
- `CommentController.java`
  - `GET /api/posts/{postId}/comments` - 댓글 목록
  - `POST /api/posts/{postId}/comments` - 댓글 작성 (인증 필요)
  - `PUT /api/posts/{postId}/comments/{commentId}` - 댓글 수정 (작성자만)
  - `DELETE /api/posts/{postId}/comments/{commentId}` - 댓글 삭제 (작성자만)

- `HomeController.java` - 루트 경로 API 정보 제공

### 📋 DTO (데이터 전송 객체)
**src/main/java/com/example/board/dto/**

**Request:**
- `LoginRequest.java` - 로그인 요청 (email, password)
- `SignupRequest.java` - 회원가입 요청 (email, password, username)
- `CreatePostRequest.java` - 게시글 작성 (title, content)
- `UpdatePostRequest.java` - 게시글 수정 (title, content)
- `CreateCommentRequest.java` - 댓글 작성 (content)
- `UpdateCommentRequest.java` - 댓글 수정 (content)

**Response:**
- `AuthResponse.java` - 인증 응답 (token, user info)
- `PostResponse.java` - 게시글 응답 (모든 필드 + isLiked)
- `PostListResponse.java` - 게시글 목록 (posts, total, page, size, totalPages)
- `CommentResponse.java` - 댓글 응답 (모든 필드)
- `CommentListResponse.java` - 댓글 목록 (comments, total)

### 🚨 Exception (예외 처리)
**src/main/java/com/example/board/exception/**

- `GlobalExceptionHandler.java` - 전역 예외 처리 (400, 500 에러)

### ⚙️ 설정 파일
**src/main/resources/**

- `application.yml` - 애플리케이션 설정
  - H2 데이터베이스 (인메모리)
  - JPA 설정 (자동 DDL)
  - JWT secret key (256bit)
  - CORS 설정
  - 포트 8080

---

## 개발 가이드

### 커밋 컨벤션

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드
- `chore`: 빌드 업무 수정

