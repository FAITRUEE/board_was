package com.example.board.controller;

import com.example.board.dto.websocket.CollaborativeEditMessage;
import com.example.board.dto.websocket.KanbanCardMoveMessage;
import com.example.board.service.CollaborativeEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CollaborativeEditController {

    private final CollaborativeEditService editService;

    /**
     * 게시글 공동 편집
     * 클라이언트 → /app/post/{postId}/edit
     * 브로드캐스트 → /topic/post/{postId}
     */
    @MessageMapping("/post/{postId}/edit")
    @SendTo("/topic/post/{postId}")
    public CollaborativeEditMessage handleEdit(
            @DestinationVariable Long postId,
            CollaborativeEditMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        message.setTimestamp(System.currentTimeMillis());
        message.setPostId(postId);

        // 세션 관리
        if (message.getType() == CollaborativeEditMessage.MessageType.JOIN) {
            editService.addEditSession(
                    postId,
                    message.getUserId(),
                    headerAccessor.getSessionId()
            );
        } else if (message.getType() == CollaborativeEditMessage.MessageType.LEAVE) {
            editService.removeEditSession(postId, message.getUserId());
        }

        return message;
    }

    /**
     * 칸반 카드 이동
     * 클라이언트 → /app/kanban/{boardId}/move
     * 브로드캐스트 → /topic/kanban/{boardId}
     */
    @MessageMapping("/kanban/{boardId}/move")
    @SendTo("/topic/kanban/{boardId}")
    public KanbanCardMoveMessage handleCardMove(
            @DestinationVariable Long boardId,
            KanbanCardMoveMessage message) {

        message.setTimestamp(System.currentTimeMillis());
        message.setBoardId(boardId);

        return message;
    }
}