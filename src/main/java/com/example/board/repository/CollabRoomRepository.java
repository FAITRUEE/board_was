package com.example.board.repository;

import com.example.board.entity.CollabRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollabRoomRepository extends JpaRepository<CollabRoom, Long> {

    @Query("SELECT r FROM CollabRoom r JOIN r.team t JOIN t.members m WHERE m.user.id = :userId AND r.isPublished = false ORDER BY r.createdAt DESC")
    List<CollabRoom> findActiveRoomsByUserId(Long userId);

    @Query("SELECT r FROM CollabRoom r LEFT JOIN FETCH r.team t LEFT JOIN FETCH t.members LEFT JOIN FETCH r.createdBy WHERE r.id = :roomId")
    Optional<CollabRoom> findByIdWithTeamAndMembers(@Param("roomId") Long roomId);
}
