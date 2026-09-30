package com.example.board.service;

import com.example.board.dto.websocket.CollaborativeEditMessage.Editor;
import com.example.board.entity.EditSession;
import com.example.board.entity.EditSession.TargetType;
import com.example.board.entity.User;
import com.example.board.repository.EditSessionRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CollaborativeEditService {

    private final EditSessionRepository editSessionRepository;
    private final UserRepository userRepository;

    /**
     * 연결이 끊기거나 정리되어 사라진 편집 세션 (브로드캐스트 대상 계산용)
     */
    public record RemovedSession(TargetType targetType, Long targetId, Long userId) {}

    /**
     * 편집 세션 추가 (같은 WebSocket 세션으로 다시 JOIN하면 활동 시간만 갱신)
     */
    @Transactional
    public void addEditSession(TargetType targetType, Long targetId, Long userId, String sessionId) {
        log.debug("Adding edit session - {} {}, userId: {}, sessionId: {}", targetType, targetId, userId, sessionId);

        editSessionRepository.findByTargetTypeAndTargetIdAndSessionId(targetType, targetId, sessionId)
                .ifPresentOrElse(
                        session -> session.setLastActive(LocalDateTime.now()),
                        () -> editSessionRepository.save(EditSession.builder()
                                .targetType(targetType)
                                .targetId(targetId)
                                .userId(userId)
                                .sessionId(sessionId)
                                .build())
                );
    }

    /**
     * 편집 세션 제거 (해당 WebSocket 세션만 제거하므로 같은 사용자의 다른 탭은 유지됨)
     */
    @Transactional
    public void removeEditSession(TargetType targetType, Long targetId, String sessionId) {
        log.debug("Removing edit session - {} {}, sessionId: {}", targetType, targetId, sessionId);
        editSessionRepository.findByTargetTypeAndTargetIdAndSessionId(targetType, targetId, sessionId)
                .ifPresent(editSessionRepository::delete);
    }

    /**
     * 편집 활동 시 마지막 활동 시간 갱신. 정리 작업으로 세션이 지워졌다면 다시 등록한다.
     */
    @Transactional
    public void touch(TargetType targetType, Long targetId, Long userId, String sessionId) {
        int updated = editSessionRepository.touch(targetType, targetId, sessionId, LocalDateTime.now());
        if (updated == 0 && userId != null) {
            addEditSession(targetType, targetId, userId, sessionId);
        }
    }

    /**
     * 현재 편집 중인 사용자 목록 (여러 탭으로 접속해도 한 번만, 먼저 들어온 순)
     */
    public List<Editor> getActiveEditors(TargetType targetType, Long targetId) {
        List<Long> userIds = editSessionRepository.findByTargetTypeAndTargetId(targetType, targetId).stream()
                .sorted(Comparator.comparing(EditSession::getConnectedAt))
                .map(EditSession::getUserId)
                .distinct()
                .toList();

        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return userIds.stream()
                .filter(users::containsKey)
                .map(id -> new Editor(id, users.get(id).getUsername()))
                .toList();
    }

    /**
     * WebSocket 연결 끊김 시 해당 세션의 모든 편집 세션 제거
     */
    @Transactional
    public List<RemovedSession> removeSessionsBySessionId(String sessionId) {
        return deleteAll(editSessionRepository.findBySessionId(sessionId));
    }

    /**
     * 비활성 세션 정리 (연결 끊김 이벤트를 놓친 경우 대비)
     */
    @Transactional
    public List<RemovedSession> cleanupInactiveSessions(LocalDateTime threshold) {
        List<RemovedSession> removed = deleteAll(editSessionRepository.findByLastActiveBefore(threshold));
        if (!removed.isEmpty()) {
            log.info("Cleaned up {} inactive edit sessions", removed.size());
        }
        return removed;
    }

    /**
     * 서버 재시작 시 이전 WebSocket 세션은 모두 무효이므로 전부 삭제
     */
    @Transactional
    public void clearAllSessions() {
        editSessionRepository.deleteAllInBatch();
    }

    private List<RemovedSession> deleteAll(List<EditSession> sessions) {
        editSessionRepository.deleteAll(sessions);
        return sessions.stream()
                .map(s -> new RemovedSession(s.getTargetType(), s.getTargetId(), s.getUserId()))
                .distinct()
                .toList();
    }
}
