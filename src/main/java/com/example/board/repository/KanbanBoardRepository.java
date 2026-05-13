package com.example.board.repository;

import com.example.board.entity.KanbanBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanBoardRepository extends JpaRepository<KanbanBoard, Long> {

    /**
     * 팀 ID로 칸반 보드 조회
     */
    List<KanbanBoard> findByTeam_Id(Long teamId);

    /**
     * 칸반 보드 ID와 팀 ID로 조회 (권한 확인용)
     */
    @Query("SELECT b FROM KanbanBoard b WHERE b.id = :boardId AND b.team.id = :teamId")
    Optional<KanbanBoard> findByIdAndTeamId(@Param("boardId") Long boardId,
                                            @Param("teamId") Long teamId);

    /**
     * 특정 사용자가 접근 가능한 칸반 보드 조회
     */
    @Query("SELECT DISTINCT b FROM KanbanBoard b " +
            "JOIN b.team t " +
            "JOIN t.members m " +
            "WHERE m.user.id = :userId")
    List<KanbanBoard> findAccessibleBoardsByUserId(@Param("userId") Long userId);
}