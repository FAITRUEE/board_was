CREATE TABLE collab_rooms (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id     BIGINT NOT NULL,
    created_by  BIGINT NOT NULL,
    title       VARCHAR(200),
    content     TEXT,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_post_id BIGINT,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    CONSTRAINT fk_collab_room_team    FOREIGN KEY (team_id)   REFERENCES team(id)  ON DELETE CASCADE,
    CONSTRAINT fk_collab_room_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);
