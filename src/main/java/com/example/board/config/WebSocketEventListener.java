package com.example.board.config;

import com.example.board.dto.websocket.CollaborativeEditMessage;
import com.example.board.service.CollaborativeEditService;
import com.example.board.service.CollaborativeEditService.RemovedSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공동 편집 세션 생명주기 관리
 * - 브라우저를 닫는 등 LEAVE 없이 연결이 끊긴 경우 세션 정리 + 남은 편집자에게 알림
 * - 오래된 세션 주기적 정리
 * - 서버 재시작 시 이전 세션 초기화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private static final long INACTIVE_MINUTES = 30;

    private final CollaborativeEditService editService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void clearStaleSessionsOnStartup() {
        editService.clearAllSessions();
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        List<RemovedSession> removed = editService.removeSessionsBySessionId(event.getSessionId());
        broadcastLeave(removed);
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void cleanupInactiveSessions() {
        List<RemovedSession> removed =
                editService.cleanupInactiveSessions(LocalDateTime.now().minusMinutes(INACTIVE_MINUTES));
        broadcastLeave(removed);
    }

    private void broadcastLeave(List<RemovedSession> removed) {
        for (RemovedSession session : removed) {
            CollaborativeEditMessage message = CollaborativeEditMessage.builder()
                    .postId(session.targetId())
                    .userId(session.userId())
                    .type(CollaborativeEditMessage.MessageType.LEAVE)
                    .timestamp(System.currentTimeMillis())
                    .editors(editService.getActiveEditors(session.targetType(), session.targetId()))
                    .build();
            messagingTemplate.convertAndSend(session.targetType().topic(session.targetId()), message);
        }
    }
}
