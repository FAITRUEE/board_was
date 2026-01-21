# Board API

Java + Spring Boot + Gradle + mySQL 기반으로 구축한 게시판 백엔드 API 서버입니다.
RESTful API 구조로 게시글, 댓글, 인증 기능을 설계했으며,
AI API와 연동하여 글 작성 보조, 요약, 콘텐츠 필터링 기능을 서버 단에서 처리하도록 구현했습니다.
비즈니스 로직과 AI 처리 로직을 분리하여 확장성과 유지보수를 고려한 구조로 설계했습니다.

## 기술 스택

- **Java** 17+
- **Spring Boot** 3.x
- **Gradle** 8.x
- **Spring Data JPA**
- **Spring Security + JWT**
- **H2 Database** (개발용)
- **Ollama** - AI 작성 도우미

## 주요 기능

- JWT 기반 회원가입/로그인
- 게시글 CRUD
- 카테고리 관리 ✨
- 파일 업로드/다운로드
- 비밀글 기능 🔒
- AI 작성 도우미 (Ollama)
- 댓글 CRUD
- 좋아요 토글
- 조회수 증가
- 페이지네이션 & 정렬

---

## 📋 API 엔드포인트

### 인증 (Auth)
```
POST   /api/auth/signup    # 회원가입
POST   /api/auth/login     # 로그인
```

### 게시글 (Posts)
```
GET    /api/posts                          # 게시글 목록 (페이징, 정렬, 카테고리 필터)
GET    /api/posts/{id}                     # 게시글 상세
POST   /api/posts                          # 게시글 작성 (파일 업로드)
PUT    /api/posts/{id}                     # 게시글 수정
DELETE /api/posts/{id}                     # 게시글 삭제
POST   /api/posts/{id}/views               # 조회수 증가
POST   /api/posts/{id}/like                # 좋아요 토글
POST   /api/posts/{id}/verify-password     # 비밀글 비밀번호 확인
GET    /api/posts/attachments/{filename}   # 첨부파일 다운로드
```

### 카테고리 (Categories) ✨
```
GET    /api/categories        # 카테고리 목록
GET    /api/categories/{id}   # 카테고리 상세
POST   /api/categories        # 카테고리 생성
PUT    /api/categories/{id}   # 카테고리 수정
DELETE /api/categories/{id}   # 카테고리 삭제
```

### 댓글 (Comments)
```
GET    /api/posts/{postId}/comments                    # 댓글 목록
POST   /api/posts/{postId}/comments                    # 댓글 작성
PUT    /api/posts/{postId}/comments/{commentId}        # 댓글 수정
DELETE /api/posts/{postId}/comments/{commentId}        # 댓글 삭제
```

### AI 작성 도우미
```
POST   /api/ai/generate-post   # AI 게시글 생성
```

---

## 🎯 주요 기능 상세

### 1️⃣ 카테고리 시스템 ✨
- **계층 구조**: Category 엔티티로 관리
- **Post 연관**: @ManyToOne 관계
- **필터링**: categoryId로 게시글 필터링
- **속성**: 이름, 색상, 아이콘, 설명

### 2️⃣ 파일 업로드
- **저장 경로**: `uploads/` 디렉토리
- **파일명 변경**: UUID로 중복 방지
- **메타데이터**: 원본 파일명, 크기, 타입 저장
- **다운로드**: 첨부파일 다운로드 API

### 3️⃣ 비밀글 기능 🔒
- **비밀번호 암호화**: BCrypt로 암호화 저장
- **접근 제어**: 비밀번호 확인 API
- **작성자 우선**: 작성자는 비밀번호 없이 접근
- **목록 보호**: 비밀글은 요약만 표시

### 4️⃣ AI 작성 도우미
- **Ollama 연동**: 로컬 LLM 서버
- **모델**: EXAONE 3.5 (한국어 최적화)
- **생성**: 주제 → 제목 + 내용 자동 생성

### 5️⃣ 보안
- **JWT**: 256bit HS256 알고리즘
- **비밀번호**: BCrypt 암호화
- **권한 관리**: Spring Security
- **CORS**: 프론트엔드 도메인 허용

---

## 시작하기

### 필수 요구사항

- JDK 17 이상
- Gradle 8.x 이상
- Ollama (AI 기능 사용 시)

### Ollama 설치 (선택)

```bash
# Ollama 설치
curl -fsSL https://ollama.com/install.sh | sh

# EXAONE 모델 다운로드
ollama pull exaone3.5:7.8b

# Ollama 서버 실행
ollama serve
```

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

`src/main/resources/application.yml`:

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
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

jwt:
  secret: your-256-bit-secret-key-here
  expiration: 86400000  # 24시간

file:
  upload-dir: uploads

ollama:
  base-url: http://localhost:11434
  model: exaone3.5:7.8b
```

---

## 📁 백엔드 파일 구조

### 📦 Entity (엔티티)
**src/main/java/com/example/board/entity/**

- `User.java` - 사용자
- `Post.java` - 게시글 (category, isSecret, secretPassword)
- `Category.java` - 카테고리 ✨
- `PostAttachment.java` - 첨부파일
- `Comment.java` - 댓글
- `PostLike.java` - 좋아요

### 🗄️ Repository (데이터 접근 계층)
**src/main/java/com/example/board/repository/**

- `UserRepository.java` - 사용자 조회
- `PostRepository.java` - 게시글 조회 (카테고리 필터링) ✨
- `CategoryRepository.java` - 카테고리 조회 ✨
- `PostAttachmentRepository.java` - 첨부파일 조회
- `CommentRepository.java` - 댓글 조회
- `PostLikeRepository.java` - 좋아요 조회

### 🔐 Security (보안)
**src/main/java/com/example/board/security/**

- `JwtTokenProvider.java` - JWT 토큰 생성/검증
- `JwtAuthenticationFilter.java` - JWT 인증 필터

### ⚙️ Config (설정)
**src/main/java/com/example/board/config/**

- `SecurityConfig.java` - Spring Security 설정
- `WebConfig.java` - CORS 설정

### 💼 Service (비즈니스 로직)
**src/main/java/com/example/board/service/**

- `AuthService.java` - 인증 처리
- `PostService.java` - 게시글 CRUD (카테고리, 비밀글, 파일) ✨
- `CategoryService.java` - 카테고리 CRUD ✨
- `FileStorageService.java` - 파일 저장/로드 ✨
- `CommentService.java` - 댓글 CRUD
- `PostLikeService.java` - 좋아요 토글
- `OllamaService.java` - AI 생성 ✨

### 🎮 Controller (API 엔드포인트)
**src/main/java/com/example/board/controller/**

- `AuthController.java` - 인증 API
- `PostController.java` - 게시글 API (파일, 비밀글) ✨
- `CategoryController.java` - 카테고리 API ✨
- `CommentController.java` - 댓글 API
- `AIController.java` - AI 생성 API ✨

### 📋 DTO (데이터 전송 객체)

**Request:**
- `CreatePostRequest.java` - 게시글 작성 (categoryId, isSecret, secretPassword)
- `UpdatePostRequest.java` - 게시글 수정 (categoryId)
- `CreateCategoryRequest.java` - 카테고리 생성 ✨
- `UpdateCategoryRequest.java` - 카테고리 수정 ✨
- `AIGenerateRequest.java` - AI 생성 요청 ✨

**Response:**
- `PostResponse.java` - 게시글 응답 (category, attachments)
- `CategoryResponse.java` - 카테고리 응답 ✨
- `AIGenerateResponse.java` - AI 생성 응답 ✨

---

## 🗄️ 데이터베이스 스키마

### users
```sql
id, email (unique), username, password, created_at
```

### posts
```sql
id, title, content, author_id, category_id, views, like_count, 
comment_count, is_secret, secret_password, created_at, updated_at
```

### categories ✨
```sql
id, name (unique), color, icon, description, created_at
```

### post_attachments
```sql
id, post_id, original_file_name, stored_file_name, 
file_path, file_size, content_type, uploaded_at
```

### comments
```sql
id, post_id, author_id, content, created_at, updated_at
```

### post_likes
```sql
id, post_id, user_id, created_at
UNIQUE (post_id, user_id)
```

---

## 🔒 보안 설정

### 공개 API (인증 불필요)
- `POST /api/auth/**` - 회원가입, 로그인
- `GET /api/posts` - 게시글 목록
- `GET /api/posts/{id}` - 게시글 상세
- `GET /api/categories` - 카테고리 목록
- `GET /api/posts/attachments/**` - 첨부파일 다운로드

### 보호 API (인증 필요)
- 게시글 작성/수정/삭제
- 댓글 작성/수정/삭제
- 좋아요 토글
- 카테고리 관리
- AI 생성

---

## 개발 가이드

### 커밋 컨벤션

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드
- `chore`: 빌드 업무 수정

---

## 📊 성능 최적화

- **역정규화**: 댓글 수, 좋아요 수를 Post에 저장
- **인덱싱**: category_id, author_id에 인덱스
- **페이지네이션**: Spring Data JPA Pageable
- **Lazy Loading**: 연관 엔티티 지연 로딩
- **파일 저장**: 로컬 파일 시스템 (DB 부하 감소)

---

## 🚀 배포

### 프로덕션 설정

1. **데이터베이스**: H2 → MySQL/PostgreSQL 변경
2. **파일 저장소**: 로컬 → S3/Cloud Storage
3. **JWT Secret**: 강력한 비밀키로 변경
4. **CORS**: 프로덕션 도메인으로 제한

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/board
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```
