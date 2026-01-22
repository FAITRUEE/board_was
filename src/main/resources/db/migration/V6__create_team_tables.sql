-- src/main/resources/db/migration/V6__create_team_tables.sql

-- 팀 테이블
CREATE TABLE team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    CONSTRAINT fk_team_creator FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 팀 멤버 테이블
CREATE TABLE team_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT unique_team_user UNIQUE (team_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기존 post 테이블에 팀 관련 컬럼 추가
ALTER TABLE post
ADD COLUMN team_id BIGINT DEFAULT NULL AFTER author_id,
ADD COLUMN is_collaborative BOOLEAN DEFAULT FALSE AFTER is_secret;

-- 외래키 제약조건 추가
ALTER TABLE post
ADD CONSTRAINT fk_post_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE SET NULL;

-- 인덱스 생성
CREATE INDEX idx_team_created_by ON team(created_by);
CREATE INDEX idx_team_member_team ON team_member(team_id);
CREATE INDEX idx_team_member_user ON team_member(user_id);
CREATE INDEX idx_post_team ON post(team_id);