-- 칸반 보드 테이블
CREATE TABLE kanban_board (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kanban_board_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 칸반 카드 테이블
CREATE TABLE kanban_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('TODO', 'IN_PROGRESS', 'DONE') NOT NULL DEFAULT 'TODO',
    position INT NOT NULL DEFAULT 0,
    assigned_to BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kanban_card_board FOREIGN KEY (board_id) REFERENCES kanban_board(id) ON DELETE CASCADE,
    CONSTRAINT fk_kanban_card_assignee FOREIGN KEY (assigned_to) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT fk_kanban_card_creator FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 칸반 카드 댓글 테이블
CREATE TABLE kanban_card_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kanban_comment_card FOREIGN KEY (card_id) REFERENCES kanban_card(id) ON DELETE CASCADE,
    CONSTRAINT fk_kanban_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_kanban_board_team ON kanban_board(team_id);
CREATE INDEX idx_kanban_card_board ON kanban_card(board_id);
CREATE INDEX idx_kanban_card_status ON kanban_card(status);
CREATE INDEX idx_kanban_card_position ON kanban_card(board_id, status, position);
CREATE INDEX idx_kanban_card_assignee ON kanban_card(assigned_to);
CREATE INDEX idx_kanban_comment_card ON kanban_card_comment(card_id);