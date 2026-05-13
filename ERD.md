# ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    users {
        bigint id PK
        varchar email UK
        varchar username UK
        varchar password
        datetime created_at
        datetime updated_at
    }

    categories {
        bigint id PK
        varchar name UK
        varchar color
        varchar icon
        varchar description
        datetime created_at
    }

    tags {
        bigint id PK
        varchar name UK
        int use_count
        datetime created_at
    }

    posts {
        bigint id PK
        varchar title
        text content
        int views
        int like_count
        int comment_count
        boolean is_secret
        varchar secret_password
        boolean is_collaborative
        bigint author_id FK
        bigint team_id FK
        bigint category_id FK
        datetime created_at
        datetime updated_at
    }

    post_tags {
        bigint post_id FK
        bigint tag_id FK
    }

    comments {
        bigint id PK
        text content
        bigint post_id FK
        bigint author_id FK
        datetime created_at
        datetime updated_at
    }

    post_likes {
        bigint id PK
        bigint post_id FK
        bigint user_id FK
        datetime created_at
    }

    post_attachments {
        bigint id PK
        varchar original_file_name
        varchar stored_file_name
        varchar file_path
        bigint file_size
        varchar content_type
        boolean is_image
        bigint post_id FK
        datetime created_at
    }

    team {
        bigint id PK
        varchar name
        varchar description
        bigint created_by FK
        datetime created_at
        datetime updated_at
    }

    team_member {
        bigint id PK
        bigint team_id FK
        bigint user_id FK
        varchar role
        datetime joined_at
    }

    kanban_board {
        bigint id PK
        varchar name
        varchar description
        bigint team_id FK
        datetime created_at
        datetime updated_at
    }

    kanban_card {
        bigint id PK
        varchar title
        text description
        varchar status
        int position
        varchar priority
        datetime due_date
        bigint board_id FK
        bigint assigned_to FK
        bigint created_by FK
        datetime created_at
        datetime updated_at
    }

    kanban_checklist_item {
        bigint id PK
        varchar text
        boolean completed
        int position
        bigint card_id FK
        datetime created_at
    }

    kanban_card_comment {
        bigint id PK
        text content
        bigint card_id FK
        bigint user_id FK
        datetime created_at
    }

    collab_rooms {
        bigint id PK
        varchar title
        text content
        boolean is_published
        bigint published_post_id
        bigint team_id FK
        bigint created_by FK
        datetime created_at
        datetime updated_at
    }

    edit_session {
        bigint id PK
        varchar session_id
        bigint post_id
        bigint user_id
        datetime connected_at
        datetime last_active
    }

    edit_history {
        bigint id PK
        text content_snapshot
        varchar change_description
        bigint post_id
        bigint user_id
        datetime created_at
    }

    %% ── 사용자 관계 ──
    users ||--o{ posts             : "작성"
    users ||--o{ comments          : "작성"
    users ||--o{ post_likes        : "좋아요"
    users ||--o{ team              : "팀 생성"
    users ||--o{ team_member       : "팀 소속"
    users ||--o{ kanban_card_comment : "댓글 작성"
    users ||--o{ collab_rooms      : "방 생성"

    %% ── 게시글 관계 ──
    categories ||--o{ posts        : "카테고리"
    team       ||--o{ posts        : "팀 게시글"
    posts      ||--o{ post_tags    : ""
    tags       ||--o{ post_tags    : ""
    posts      ||--o{ comments     : "댓글"
    posts      ||--o{ post_likes   : "좋아요"
    posts      ||--o{ post_attachments : "첨부파일"

    %% ── 팀 / 칸반 관계 ──
    team       ||--o{ team_member  : "구성원"
    team       ||--o{ kanban_board : "보드"
    team       ||--o{ collab_rooms : "편집 방"
    kanban_board ||--o{ kanban_card : "카드"
    users      ||--o{ kanban_card  : "담당자(assigned_to)"
    users      ||--o{ kanban_card  : "생성자(created_by)"
    kanban_card ||--o{ kanban_checklist_item : "체크리스트"
    kanban_card ||--o{ kanban_card_comment   : "댓글"
```

## 테이블 요약

| 테이블 | 설명 |
|--------|------|
| `users` | 회원 (이메일/유저명 UNIQUE) |
| `posts` | 게시글 (비밀글, 팀 게시글, 협업 게시글 포함) |
| `comments` | 게시글 댓글 |
| `post_likes` | 게시글 좋아요 (post_id + user_id UNIQUE) |
| `post_attachments` | 첨부파일 (이미지 포함) |
| `categories` | 게시글 카테고리 (색상/아이콘) |
| `tags` | 태그 (사용 횟수 집계) |
| `post_tags` | 게시글-태그 N:M 중간 테이블 |
| `team` | 팀 |
| `team_member` | 팀 구성원 (OWNER / ADMIN / MEMBER) |
| `kanban_board` | 칸반 보드 (팀 단위) |
| `kanban_card` | 칸반 카드 (TODO / IN_PROGRESS / DONE) |
| `kanban_checklist_item` | 카드 체크리스트 항목 |
| `kanban_card_comment` | 카드 댓글 |
| `collab_rooms` | 공동 편집 방 (발행 시 posts로 전환) |
| `edit_session` | WebSocket 편집 세션 (레거시) |
| `edit_history` | 편집 이력 스냅샷 (레거시) |
