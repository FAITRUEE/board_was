package com.example.board.repository;

import com.example.board.entity.KanbanCardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanCardCommentRepository extends JpaRepository<KanbanCardComment, Long> {

    /**
     * 카드 ID로 댓글 조회
     */
    List<KanbanCardComment> findByCard_IdOrderByCreatedAtAsc(Long cardId);

    /**
     * 카드 ID로 댓글 개수 조회
     */
    int countByCard_Id(Long cardId);
}