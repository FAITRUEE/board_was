package com.example.board.service;

import com.example.board.entity.EditSession;
import com.example.board.repository.EditSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborativeEditService {

    private final EditSessionRepository editSessionRepository;

    /**
     * 편집 세션 추가
     */
    @Transactional
    public void addEditSession(Long postId, Long userId, String sessionId) {
        log.info("Adding edit session - postId: {}, userId: {}, sessionId: {}", postId, userId, sessionId);

        // 기존 세션이 있는지 확인
        editSessionRepository.findByPostIdAndSessionId(postId, sessionId)
                .ifPresentOrElse(
                        session -> {
                            // 이미 있으면 마지막 활동 시간만 업데이트
                            session.setLastActive(LocalDateTime.now());
                            editSessionRepository.save(session);
                            log.info("Updated existing session");
                        },
                        () -> {
                            // 없으면 새로 생성
                            EditSession newSession = EditSession.builder()
                                    .postId(postId)
                                    .userId(userId)
                                    .sessionId(sessionId)
                                    .connectedAt(LocalDateTime.now())
                                    .lastActive(LocalDateTime.now())
                                    .build();
                            editSessionRepository.save(newSession);
                            log.info("Created new session");
                        }
                );
    }

    /**
     * 편집 세션 제거
     */
    @Transactional
    public void removeEditSession(Long postId, Long userId) {
        log.info("Removing edit session - postId: {}, userId: {}", postId, userId);
        editSessionRepository.deleteByPostIdAndUserId(postId, userId);
    }

    /**
     * 특정 게시글의 활성 세션 조회
     */
    public List<EditSession> getActiveSessionsByPostId(Long postId) {
        return editSessionRepository.findByPostId(postId);
    }

    /**
     * 비활성 세션 정리 (5분 이상 활동 없는 세션)
     */
    @Transactional
    public void cleanupInactiveSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        int deletedCount = editSessionRepository.deleteByLastActiveBefore(threshold);
        log.info("Cleaned up {} inactive sessions", deletedCount);
    }
}