// src/main/java/com/example/board/entity/EditHistory.java
package com.example.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "edit_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_snapshot", columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
