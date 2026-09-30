package com.example.board.repository;

import com.example.board.entity.EditSession;
import com.example.board.entity.EditSession.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EditSessionRepository extends JpaRepository<EditSession, Long> {

    Optional<EditSession> findByTargetTypeAndTargetIdAndSessionId(TargetType targetType, Long targetId, String sessionId);

    /**
     * 대상(게시글/방)의 모든 활성 세션
     */
    List<EditSession> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * WebSocket 세션에 연결된 모든 편집 세션 (연결 끊김 처리용)
     */
    List<EditSession> findBySessionId(String sessionId);

    List<EditSession> findByLastActiveBefore(LocalDateTime threshold);

    @Modifying
    @Query("UPDATE EditSession s SET s.lastActive = :now " +
            "WHERE s.targetType = :targetType AND s.targetId = :targetId AND s.sessionId = :sessionId")
    int touch(@Param("targetType") TargetType targetType,
              @Param("targetId") Long targetId,
              @Param("sessionId") String sessionId,
              @Param("now") LocalDateTime now);
}
