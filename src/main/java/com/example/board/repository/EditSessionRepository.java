package com.example.board.repository;

import com.example.board.entity.EditSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EditSessionRepository extends JpaRepository<EditSession, Long> {

    /**
     * 게시글 ID와 세션 ID로 조회
     */
    Optional<EditSession> findByPostIdAndSessionId(Long postId, String sessionId);

    /**
     * 게시글 ID로 모든 활성 세션 조회
     */
    List<EditSession> findByPostId(Long postId);

    /**
     * 게시글 ID와 사용자 ID로 삭제
     */
    void deleteByPostIdAndUserId(Long postId, Long userId);

    /**
     * 특정 시간 이전의 비활성 세션 삭제
     */
    int deleteByLastActiveBefore(LocalDateTime threshold);
}