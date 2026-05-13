package com.example.board.dto.websocket;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardMoveMessage {
    private Long cardId;
    private Long boardId;
    private String status;      // TODO, IN_PROGRESS, DONE
    private Integer position;
    private Long userId;
    private String username;
    private Long timestamp;
}