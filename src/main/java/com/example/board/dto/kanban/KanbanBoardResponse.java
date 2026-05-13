// src/main/java/com/example/board/dto/kanban/KanbanBoardResponse.java
package com.example.board.dto.kanban;

import com.example.board.entity.KanbanBoard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanBoardResponse {

    private Long id;
    private Long teamId;
    private String teamName;
    private String name;
    private String description;
    private Integer cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<KanbanCardResponse> cards;

    public static KanbanBoardResponse from(KanbanBoard board) {
        return KanbanBoardResponse.builder()
                .id(board.getId())
                .teamId(board.getTeam().getId())
                .teamName(board.getTeam().getName())
                .name(board.getName())
                .description(board.getDescription())
                .cardCount(board.getCards().size())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    public static KanbanBoardResponse fromWithCards(KanbanBoard board) {
        List<KanbanCardResponse> cards = board.getCards().stream()
                .map(KanbanCardResponse::from)
                .collect(Collectors.toList());

        return KanbanBoardResponse.builder()
                .id(board.getId())
                .teamId(board.getTeam().getId())
                .teamName(board.getTeam().getName())
                .name(board.getName())
                .description(board.getDescription())
                .cardCount(cards.size())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .cards(cards)
                .build();
    }
}