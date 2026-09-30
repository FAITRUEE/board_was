package com.example.board.controller;

import com.example.board.dto.websocket.CollaborativeEditMessage;
import com.example.board.dto.websocket.CollaborativeEditMessage.MessageType;
import com.example.board.entity.EditSession.TargetType;
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

        return handle(TargetType.POST, postId, message, headerAccessor.getSessionId());
    }

    /**
     * 공동 편집 방
     * 클라이언트 → /app/collab-room/{roomId}/edit
     * 브로드캐스트 → /topic/collab-room/{roomId}
     */
    @MessageMapping("/collab-room/{roomId}/edit")
    @SendTo("/topic/collab-room/{roomId}")
    public CollaborativeEditMessage handleRoomEdit(
            @DestinationVariable Long roomId,
            CollaborativeEditMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        return handle(TargetType.COLLAB_ROOM, roomId, message, headerAccessor.getSessionId());
    }

    private CollaborativeEditMessage handle(TargetType targetType, Long targetId,
                                            CollaborativeEditMessage message, String sessionId) {
        message.setTimestamp(System.currentTimeMillis());
        message.setPostId(targetId);

        if (message.getUserId() != null) {
            MessageType type = message.getType();
            if (type == MessageType.JOIN) {
                editService.addEditSession(targetType, targetId, message.getUserId(), sessionId);
            } else if (type == MessageType.LEAVE) {
                editService.removeEditSession(targetType, targetId, sessionId);
            } else if (type == MessageType.CONTENT_CHANGE) {
                editService.touch(targetType, targetId, message.getUserId(), sessionId);
            }

            if (type == MessageType.JOIN || type == MessageType.LEAVE) {
                message.setEditors(editService.getActiveEditors(targetType, targetId));
            }
        }

        return message;
    }
}
