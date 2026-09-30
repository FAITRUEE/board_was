-- V8의 edit_session / edit_history는 실제로 사용하지 않는 레거시 post, user 테이블을 참조하고 있어
-- 세션 저장이 항상 FK 위반으로 실패했음. 두 테이블 모두 저장된 데이터가 없으므로 재생성한다.
DROP TABLE IF EXISTS edit_session;
DROP TABLE IF EXISTS edit_history;

-- 실시간 편집 세션 (게시글 / 공동 편집 방 공용)
CREATE TABLE edit_session (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type  VARCHAR(20)  NOT NULL,
    target_id    BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    session_id   VARCHAR(100) NOT NULL,
    connected_at DATETIME     NOT NULL,
    last_active  DATETIME     NOT NULL,
    CONSTRAINT uk_edit_session UNIQUE (target_type, target_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_edit_session_session ON edit_session(session_id);
CREATE INDEX idx_edit_session_last_active ON edit_session(last_active);

-- 공동 편집 방 버전 기록
CREATE TABLE edit_history (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id            BIGINT       NOT NULL,
    user_id            BIGINT       NOT NULL,
    title              VARCHAR(200),
    content_snapshot   TEXT,
    change_description VARCHAR(500),
    created_at         DATETIME     NOT NULL,
    CONSTRAINT fk_edit_history_room FOREIGN KEY (room_id) REFERENCES collab_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_edit_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_edit_history_room_created ON edit_history(room_id, created_at DESC);
