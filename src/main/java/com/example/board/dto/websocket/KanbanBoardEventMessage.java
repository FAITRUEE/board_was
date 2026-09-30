package com.example.board.dto.websocket;

import lombok.*;

/**
 * 칸반 보드 변경 알림 (REST로 변경이 저장된 뒤 서버가 /topic/kanban/{boardId}로 브로드캐스트)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanBoardEventMessage {
    private Long boardId;
    private Long cardId;
    private EventType type;
    private String status;      // CARD_MOVED일 때 이동한 컬럼 (TODO, IN_PROGRESS, DONE)
    private Integer position;   // CARD_MOVED일 때 이동한 위치
    private Long userId;
    private String username;
    private Long timestamp;

    public enum EventType {
        CARD_CREATED,
        CARD_UPDATED,
        CARD_MOVED,
        CARD_DELETED,
        CHECKLIST_CHANGED,
        COMMENT_CHANGED
    }
}
