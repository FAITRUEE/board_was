package com.example.board.repository;

import com.example.board.entity.EditHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EditHistoryRepository extends JpaRepository<EditHistory, Long> {

    @EntityGraph(attributePaths = "user")
    List<EditHistory> findTop50ByRoomIdOrderByCreatedAtDescIdDesc(Long roomId);

    Optional<EditHistory> findFirstByRoomIdOrderByCreatedAtDescIdDesc(Long roomId);

    Optional<EditHistory> findByIdAndRoomId(Long id, Long roomId);
}
