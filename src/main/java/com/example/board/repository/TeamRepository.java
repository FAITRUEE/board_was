package com.example.board.repository;

import com.example.board.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 팀 생성자로 팀 조회
     */
    List<Team> findByCreatedBy_Id(Long userId);

    /**
     * 팀명으로 검색
     */
    List<Team> findByNameContaining(String name);

    /**
     * 특정 사용자가 속한 팀 조회 (멤버 포함)
     */
    @Query("SELECT DISTINCT t FROM Team t " +
            "JOIN t.members m " +
            "WHERE m.user.id = :userId")
    List<Team> findTeamsByUserId(@Param("userId") Long userId);

    /**
     * 팀 ID와 사용자 ID로 권한 확인
     */
    @Query("SELECT t FROM Team t " +
            "JOIN t.members m " +
            "WHERE t.id = :teamId AND m.user.id = :userId")
    Optional<Team> findTeamByIdAndUserId(@Param("teamId") Long teamId,
                                         @Param("userId") Long userId);
}