// src/main/java/com/example/board/dto/kanban/KanbanCardResponse.java
package com.example.board.dto.kanban;

import com.example.board.entity.KanbanCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardResponse {

    private Long id;
    private Long boardId;
    private String title;
    private String description;
    private String status;
    private Integer position;
    private Long assignedToId;
    private String assignedToUsername;
    private Long createdById;
    private String createdByUsername;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KanbanCardResponse from(KanbanCard card) {
        return KanbanCardResponse.builder()
                .id(card.getId())
                .boardId(card.getBoard().getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .status(card.getStatus().name())
                .position(card.getPosition())
                .assignedToId(card.getAssignedTo() != null ? card.getAssignedTo().getId() : null)
                .assignedToUsername(card.getAssignedTo() != null ? card.getAssignedTo().getUsername() : null)
                .createdById(card.getCreatedBy().getId())
                .createdByUsername(card.getCreatedBy().getUsername())
                .commentCount(card.getComments().size())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}