// src/main/java/com/example/board/service/TeamService.java
package com.example.board.service;

import com.example.board.dto.team.TeamCreateRequest;
import com.example.board.dto.team.TeamMemberInviteRequest;
import com.example.board.dto.team.TeamMemberResponse;
import com.example.board.dto.team.TeamResponse;
import com.example.board.entity.Team;
import com.example.board.entity.TeamMember;
import com.example.board.entity.User;
import com.example.board.repository.TeamMemberRepository;
import com.example.board.repository.TeamRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    /**
     * 팀 생성
     */
    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request, Long currentUserId) {
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserId));

        // 팀 생성
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        Team savedTeam = teamRepository.save(team);

        // 생성자를 OWNER로 자동 추가
        TeamMember ownerMember = TeamMember.builder()
                .team(savedTeam)
                .user(creator)
                .role(TeamMember.TeamRole.OWNER)
                .build();

        teamMemberRepository.save(ownerMember);

        log.info("팀 생성 완료 - teamId: {}, createdBy: {}", savedTeam.getId(), currentUserId);

        return TeamResponse.from(savedTeam);
    }

    /**
     * 내가 속한 팀 목록 조회
     */
    public List<TeamResponse> getMyTeams(Long currentUserId) {
        List<Team> teams = teamRepository.findTeamsByUserId(currentUserId);
        return teams.stream()
                .map(TeamResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 팀 상세 조회
     */
    public TeamResponse getTeam(Long teamId, Long currentUserId) {
        Team team = teamRepository.findTeamByIdAndUserId(teamId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없거나 접근 권한이 없습니다: " + teamId));

        return TeamResponse.from(team);
    }

    /**
     * 팀 멤버 목록 조회
     */
    public List<TeamMemberResponse> getTeamMembers(Long teamId, Long currentUserId) {
        // 권한 확인
        checkTeamMembership(teamId, currentUserId);

        List<TeamMember> members = teamMemberRepository.findByTeam_Id(teamId);
        return members.stream()
                .map(TeamMemberResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 팀원 초대
     */
    @Transactional
    public TeamMemberResponse inviteMember(Long teamId, TeamMemberInviteRequest request, Long currentUserId) {
        // 권한 확인 (OWNER 또는 ADMIN만 가능)
        checkTeamAdminPermission(teamId, currentUserId);

        // 초대할 사용자 확인
        User invitedUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 이미 멤버인지 확인
        if (teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, request.getUserId())) {
            throw new IllegalStateException("이미 팀 멤버입니다");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));

        // 역할 설정 (기본값: MEMBER)
        TeamMember.TeamRole role = TeamMember.TeamRole.MEMBER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = TeamMember.TeamRole.ADMIN;
        }

        TeamMember newMember = TeamMember.builder()
                .team(team)
                .user(invitedUser)
                .role(role)
                .build();

        TeamMember savedMember = teamMemberRepository.save(newMember);

        log.info("팀원 초대 완료 - teamId: {}, invitedUserId: {}, role: {}", teamId, request.getUserId(), role);

        return TeamMemberResponse.from(savedMember);
    }

    /**
     * 팀원 제거
     */
    @Transactional
    public void removeMember(Long teamId, Long memberId, Long currentUserId) {
        // 권한 확인 (OWNER 또는 ADMIN만 가능)
        checkTeamAdminPermission(teamId, currentUserId);

        // OWNER는 제거 불가
        TeamMember targetMember = teamMemberRepository.findByTeam_IdAndUser_Id(teamId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("팀 멤버를 찾을 수 없습니다"));

        if (targetMember.getRole() == TeamMember.TeamRole.OWNER) {
            throw new IllegalStateException("팀 OWNER는 제거할 수 없습니다");
        }

        teamMemberRepository.deleteByTeam_IdAndUser_Id(teamId, memberId);

        log.info("팀원 제거 완료 - teamId: {}, removedUserId: {}", teamId, memberId);
    }

    /**
     * 팀 삭제 (OWNER만 가능)
     */
    @Transactional
    public void deleteTeam(Long teamId, Long currentUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));

        // OWNER 확인
        if (!team.getCreatedBy().getId().equals(currentUserId)) {
            throw new IllegalStateException("팀 삭제 권한이 없습니다 (OWNER만 가능)");
        }

        teamRepository.delete(team);

        log.info("팀 삭제 완료 - teamId: {}, deletedBy: {}", teamId, currentUserId);
    }

    // === 권한 확인 헬퍼 메서드 ===

    private void checkTeamMembership(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, userId)) {
            throw new IllegalStateException("팀 멤버가 아닙니다");
        }
    }

    private void checkTeamAdminPermission(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_Id(teamId, userId)
                .orElseThrow(() -> new IllegalStateException("팀 멤버가 아닙니다"));

        if (member.getRole() != TeamMember.TeamRole.OWNER &&
                member.getRole() != TeamMember.TeamRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다 (OWNER 또는 ADMIN만 가능)");
        }
    }
}