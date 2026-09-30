package com.example.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "edit_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Column(name = "last_active", nullable = false)
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        if (connectedAt == null) {
            connectedAt = LocalDateTime.now();
        }
        if (lastActive == null) {
            lastActive = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastActive = LocalDateTime.now();
    }

    public enum TargetType {
        POST,       // 게시글 공동 편집  → /topic/post/{id}
        COLLAB_ROOM // 공동 편집 방     → /topic/collab-room/{id}
        ;

        public String topic(Long targetId) {
            return this == POST ? "/topic/post/" + targetId : "/topic/collab-room/" + targetId;
        }
    }
}
