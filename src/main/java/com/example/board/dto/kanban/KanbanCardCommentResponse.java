package com.example.board.dto.kanban;

import com.example.board.entity.KanbanCardComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardCommentResponse {

    private Long id;
    private Long cardId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    public static KanbanCardCommentResponse from(KanbanCardComment comment) {
        return KanbanCardCommentResponse.builder()
                .id(comment.getId())
                .cardId(comment.getCard().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
