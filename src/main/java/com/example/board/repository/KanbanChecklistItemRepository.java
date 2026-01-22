package com.example.board.repository;

import com.example.board.entity.KanbanChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanChecklistItemRepository extends JpaRepository<KanbanChecklistItem, Long> {

    List<KanbanChecklistItem> findByCard_IdOrderByPositionAsc(Long cardId);

    void deleteByCard_Id(Long cardId);
}