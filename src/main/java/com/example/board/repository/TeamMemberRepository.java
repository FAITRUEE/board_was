package com.example.board.repository;

import com.example.board.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * 팀 ID로 멤버 조회
     */
    List<TeamMember> findByTeam_Id(Long teamId);

    /**
     * 팀 ID와 사용자 ID로 멤버 조회
     */
    Optional<TeamMember> findByTeam_IdAndUser_Id(Long teamId, Long userId);

    /**
     * 팀 ID와 사용자 ID로 멤버 존재 여부 확인
     */
    boolean existsByTeam_IdAndUser_Id(Long teamId, Long userId);

    /**
     * 팀 ID와 사용자 ID로 멤버 삭제
     */
    void deleteByTeam_IdAndUser_Id(Long teamId, Long userId);
}