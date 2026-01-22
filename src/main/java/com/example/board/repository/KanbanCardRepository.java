package com.example.board.repository;

import com.example.board.entity.KanbanCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanCardRepository extends JpaRepository<KanbanCard, Long> {

    /**
     * 보드 ID로 카드 조회 (position 순)
     */
    List<KanbanCard> findByBoard_IdOrderByPositionAsc(Long boardId);

    /**
     * 보드 ID와 상태로 카드 조회 (position 순)
     */
    List<KanbanCard> findByBoard_IdAndStatusOrderByPositionAsc(Long boardId, KanbanCard.CardStatus status);

    /**
     * 특정 position 이상의 카드들 조회
     */
    @Query("SELECT c FROM KanbanCard c " +
            "WHERE c.board.id = :boardId AND c.status = :status AND c.position >= :position " +
            "ORDER BY c.position ASC")
    List<KanbanCard> findCardsAfterPosition(@Param("boardId") Long boardId,
                                            @Param("status") KanbanCard.CardStatus status,
                                            @Param("position") Integer position);

    /**
     * 카드 ID와 보드 ID로 조회
     */
    Optional<KanbanCard> findByIdAndBoard_Id(Long cardId, Long boardId);
}