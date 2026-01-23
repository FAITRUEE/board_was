# 통합 협업과 AI 작성 도우미 게시판 플랫폼 - Backend API

Spring Boot + MySQL 기반 통합 협업 플랫폼 백엔드 API 서버입니다.
**게시판 시스템**과 **AI 작성 도우미**, **칸반 보드**를 RESTful API로 제공하며,
JWT 인증, 파일 관리, 팀 협업 기능을 포함합니다.

---

## 🎯 핵심 기능

### 📝 게시판 API
- **JWT 인증**: Spring Security + BCrypt
- **게시글 CRUD**: 페이지네이션, 정렬, 검색
- **카테고리 시스템**: 계층 구조, 색상/아이콘
- **태그 시스템**: M:N 관계, 인기 태그
- **파일 관리**: 로컬 파일 시스템, UUID 저장
- **비밀글**: BCrypt 암호화
- **댓글 시스템**: 계층형 댓글 (선택사항)
- **좋아요**: Unique 제약으로 중복 방지
- **조회수**: 자동 증가

### 📊 칸반 보드 API ⭐
- **팀 관리**: 팀 생성/초대, 권한 관리
- **보드 CRUD**: 팀별 보드 관리
- **카드 관리**: CRUD + 드래그앤드롭 이동
- **우선순위**: LOW/MEDIUM/HIGH/URGENT
- **체크리스트**: CRUD + 토글
- **담당자**: 팀원 할당
- **마감일**: 날짜 관리
- **태그**: 카드 분류

---

## 🛠️ 기술 스택

### Backend
- **Java** 17
- **Spring Boot** 3.2.0
- **Spring Security** - JWT 인증
- **Spring Data JPA** - ORM
- **Gradle** 8.x - 빌드 도구

### Database
- **MySQL** 8.0 - 운영 DB
- **Flyway** - DB 마이그레이션 (선택사항)

### Security
- **JWT** - HS512 알고리즘
- **BCrypt** - 비밀번호 암호화

### AI (선택사항)
- **Ollama** - AI 작성 도우미

---

## 📁 프로젝트 구조
```
src/main/java/com/example/board/
├── config/                      # 설정
│   ├── SecurityConfig.java          # Spring Security
│   ├── WebConfig.java               # CORS
│   └── WebSocketConfig.java         # WebSocket (선택)
│
├── controller/                  # REST API
│   ├── AuthController.java          # 인증
│   ├── PostController.java          # 게시글
│   ├── CommentController.java       # 댓글
│   ├── CategoryController.java      # 카테고리
│   ├── TagController.java           # 태그
│   ├── TeamController.java          # 팀 ⭐
│   └── KanbanController.java        # 칸반 ⭐
│
├── service/                     # 비즈니스 로직
│   ├── AuthService.java
│   ├── PostService.java
│   ├── CommentService.java
│   ├── CategoryService.java
│   ├── TagService.java
│   ├── TeamService.java             # ⭐
│   ├── KanbanService.java           # ⭐
│   └── FileStorageService.java
│
├── repository/                  # 데이터 접근
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   ├── CategoryRepository.java
│   ├── TagRepository.java
│   ├── TeamRepository.java          # ⭐
│   ├── KanbanBoardRepository.java   # ⭐
│   └── KanbanCardRepository.java    # ⭐
│
├── entity/                      # JPA 엔티티
│   ├── User.java
│   ├── Post.java
│   ├── Comment.java
│   ├── Category.java
│   ├── Tag.java
│   ├── PostAttachment.java
│   ├── PostLike.java
│   ├── Team.java                    # ⭐
│   ├── TeamMember.java              # ⭐
│   ├── KanbanBoard.java             # ⭐
│   ├── KanbanCard.java              # ⭐
│   └── ChecklistItem.java           # ⭐
│
├── dto/                         # 데이터 전송 객체
│   ├── request/
│   │   ├── CreatePostRequest.java
│   │   ├── UpdatePostRequest.java
│   │   ├── CreateTeamRequest.java   # ⭐
│   │   └── CreateCardRequest.java   # ⭐
│   └── response/
│       ├── PostResponse.java
│       ├── TeamResponse.java        # ⭐
│       └── CardResponse.java        # ⭐
│
├── security/                    # 보안
│   ├── JwtTokenProvider.java       # JWT 생성/검증
│   ├── JwtAuthenticationFilter.java # JWT 필터
│   └── UserPrincipal.java          # 인증 Principal ⭐
│
├── exception/                   # 예외 처리
│   └── GlobalExceptionHandler.java
│
└── util/                        # 유틸리티
    └── SecurityUtil.java            # ⭐
```

---

## 📋 API 엔드포인트

### 인증 (Auth)
```
POST   /api/auth/signup     # 회원가입
POST   /api/auth/login      # 로그인
```

### 게시글 (Posts)
```
GET    /api/posts                          # 목록 (페이징, 필터링)
GET    /api/posts/{id}                     # 상세
POST   /api/posts                          # 작성 (multipart/form-data)
PUT    /api/posts/{id}                     # 수정
DELETE /api/posts/{id}                     # 삭제
POST   /api/posts/{id}/views               # 조회수 증가
POST   /api/posts/{id}/like                # 좋아요 토글
POST   /api/posts/{id}/verify-password     # 비밀글 확인
GET    /api/posts/attachments/{filename}   # 첨부파일
```

### 카테고리 (Categories)
```
GET    /api/categories        # 목록
POST   /api/categories        # 생성
PUT    /api/categories/{id}   # 수정
DELETE /api/categories/{id}   # 삭제
```

### 태그 (Tags)
```
GET    /api/tags              # 목록
GET    /api/tags/popular      # 인기 태그
```

### 댓글 (Comments)
```
GET    /api/posts/{postId}/comments             # 목록
POST   /api/posts/{postId}/comments             # 작성
PUT    /api/posts/{postId}/comments/{id}        # 수정
DELETE /api/posts/{postId}/comments/{id}        # 삭제
```

### 팀 (Teams) ⭐
```
GET    /api/teams             # 내 팀 목록
POST   /api/teams             # 팀 생성
GET    /api/teams/{id}        # 팀 상세
POST   /api/teams/{id}/invite # 팀 초대
```

### 칸반 보드 (Kanban) ⭐
```
GET    /api/kanban/boards/my                     # 내 보드 목록
POST   /api/kanban/boards                        # 보드 생성
GET    /api/kanban/boards/{id}                   # 보드 상세 (카드 포함)
POST   /api/kanban/boards/{id}/cards             # 카드 생성
PUT    /api/kanban/boards/{id}/cards/{cardId}    # 카드 수정
DELETE /api/kanban/boards/{id}/cards/{cardId}    # 카드 삭제
PATCH  /api/kanban/boards/{id}/cards/{cardId}/move  # 카드 이동

# 체크리스트
POST   /api/kanban/boards/{id}/cards/{cardId}/checklist           # 체크리스트 추가
PATCH  /api/kanban/boards/{id}/cards/{cardId}/checklist/{itemId}/toggle  # 토글
DELETE /api/kanban/boards/{id}/cards/{cardId}/checklist/{itemId}  # 삭제
```

---

## 🚀 시작하기

### 필수 요구사항
- **JDK** 17 이상
- **MySQL** 8.0 이상
- **Gradle** 8.x 이상

### 데이터베이스 설정
```sql
CREATE DATABASE boarddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'boarduser'@'localhost' IDENTIFIED BY 'board1234';
GRANT ALL PRIVILEGES ON boarddb.* TO 'boarduser'@'localhost';
FLUSH PRIVILEGES;
```

### 설치 및 실행
```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 테스트
./gradlew test
```

### 환경 설정

`src/main/resources/application.properties`:
```properties
# Application
spring.application.name=board-api
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/boarddb?serverTimezone=Asia/Seoul
spring.datasource.username=boarduser
spring.datasource.password=board1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
jwt.secret=your-very-long-secret-key-at-least-256-bits-long
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=./uploads

# Logging
logging.level.com.example.board=DEBUG
```

### 접속
- API 서버: http://localhost:8080

---

## 🗄️ 데이터베이스 스키마

### users
```sql
id, email (unique), username, password, created_at
```

### posts
```sql
id, title, content, author_id, category_id, views, 
like_count, comment_count, is_secret, secret_password, 
created_at, updated_at
```

### categories
```sql
id, name (unique), color, icon, description, created_at
```

### tags
```sql
id, name (unique), use_count
```

### post_tags (M:N)
```sql
post_id, tag_id
```

### post_attachments
```sql
id, post_id, original_file_name, stored_file_name, 
file_size, content_type, uploaded_at
```

### comments
```sql
id, post_id, author_id, content, parent_id (nullable), 
created_at, updated_at
```

### post_likes
```sql
id, post_id, user_id, created_at
UNIQUE (post_id, user_id)
```

### teams ⭐
```sql
id, name, description, owner_id, created_at
```

### team_members ⭐
```sql
id, team_id, user_id, role (OWNER/MEMBER), joined_at
UNIQUE (team_id, user_id)
```

### kanban_boards ⭐
```sql
id, team_id, name, description, created_at, updated_at
```

### kanban_cards ⭐
```sql
id, board_id, title, description, status (TODO/IN_PROGRESS/DONE),
position, priority (LOW/MEDIUM/HIGH/URGENT), due_date,
assigned_to_id, created_by_id, created_at, updated_at
```

### checklist_items ⭐
```sql
id, card_id, text, completed, position, created_at
```

---

## 🔐 보안 설정

### 공개 API (인증 불필요)
- `POST /api/auth/**` - 인증
- `GET /api/posts` - 게시글 목록
- `GET /api/posts/{id}` - 게시글 상세
- `GET /api/categories` - 카테고리
- `GET /api/tags/**` - 태그
- `OPTIONS /**` - CORS Preflight

### 보호 API (JWT 필요)
- 게시글 작성/수정/삭제
- 댓글 작성/수정/삭제
- 좋아요
- 카테고리 관리
- 팀/칸반 모든 API

---

## 🔧 주요 구현 사항

### 1. JWT 인증 ⭐
- **UserPrincipal 기반**: SecurityContext에 UserPrincipal 저장
- **SecurityUtil**: 현재 사용자 ID 추출 유틸리티
- **JwtAuthenticationFilter**: Bearer Token 검증
```java
// UserPrincipal 사용 예시
@PostMapping
public ResponseEntity<PostResponse> createPost(...) {
    Long userId = SecurityUtil.getCurrentUserId();
    // ...
}
```

### 2. CORS 설정
```java
.setAllowedOriginPatterns("http://localhost:3000")
.setAllowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
.setAllowedHeaders("*")
.setAllowCredentials(true)
```

### 3. 파일 업로드
- **저장 경로**: `./uploads/`
- **파일명**: UUID로 중복 방지
- **메타데이터**: DB에 원본명, 크기, 타입 저장

### 4. 낙관적 업데이트 대응 ⭐
- **PATCH 메서드**: 카드 이동, 체크리스트 토글
- **즉시 응답**: 최신 상태 반환
- **역정규화**: checklistTotal, checklistCompleted

---

## 🐛 트러블슈팅

### 문제: ClassCastException in Controller
**원인**: `(Long) authentication.getPrincipal()` 시도
**해결**: `SecurityUtil.getCurrentUserId()` 사용

### 문제: CORS Preflight 실패
**원인**: OPTIONS 메서드 미허용
**해결**: `.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()`

### 문제: 체크리스트 UI 미반영
**원인**: 낙관적 업데이트 충돌
**해결**: 서버 응답 우선 처리

---

## 📊 성능 최적화

- **역정규화**: 댓글 수, 좋아요 수, 체크리스트 수
- **인덱싱**: category_id, tag_id, team_id, board_id
- **페이지네이션**: Pageable 활용
- **Lazy Loading**: 연관 엔티티 지연 로딩
- **쿼리 최적화**: Fetch Join으로 N+1 방지

---

## 🔮 향후 계획

- [ ] WebSocket 실시간 알림
- [ ] Redis 캐싱
- [ ] S3 파일 저장소
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인
- [ ] 단위/통합 테스트 확대
- [ ] API 문서 자동화 (Swagger)

---

## 👨‍💻 개발자
- **이름**: 성진 (Lee)
- **기간**: 2026.01
- **역할**: Full-Stack Developer

---

## 📄 라이선스
MIT License
