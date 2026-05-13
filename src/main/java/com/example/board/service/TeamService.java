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
        log.info("========================================");
        log.info("팀 생성 시작 - userId: {}, teamName: {}", currentUserId, request.getName());

        // ✅ 사용자 조회
        log.info("사용자 조회 시도 - userId: {}", currentUserId);

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> {
                    log.error("❌ 사용자를 찾을 수 없음 - userId: {}", currentUserId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserId);
                });

        log.info("✅ 사용자 조회 성공 - userId: {}, username: {}, email: {}",
                creator.getId(), creator.getUsername(), creator.getEmail());

        // ✅ NULL 체크
        if (creator.getId() == null) {
            log.error("❌ creator.getId()가 null입니다!");
            throw new IllegalStateException("사용자 ID가 null입니다");
        }

        log.info("Team 엔티티 생성 시작 - createdBy ID: {}", creator.getId());

        // ✅ Team 생성 - createdBy 명시적 설정
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setCreatedBy(creator);

        // ✅ 저장 전 확인
        if (team.getCreatedBy() == null || team.getCreatedBy().getId() == null) {
            log.error("❌ team.getCreatedBy() 또는 getId()가 null! createdBy: {}", team.getCreatedBy());
            throw new IllegalStateException("Team의 createdBy가 null입니다");
        }

        log.info("Team 저장 시도 - createdBy.id: {}", team.getCreatedBy().getId());

        Team savedTeam = teamRepository.save(team);
        log.info("✅ 팀 저장 성공 - teamId: {}, createdBy: {}",
                savedTeam.getId(), savedTeam.getCreatedBy().getId());

        // 생성자를 OWNER로 자동 추가
        TeamMember ownerMember = new TeamMember();
        ownerMember.setTeam(savedTeam);
        ownerMember.setUser(creator);
        ownerMember.setRole(TeamMember.TeamRole.OWNER);

        teamMemberRepository.save(ownerMember);
        log.info("✅ 팀 멤버 저장 성공");
        log.info("========================================");

        return TeamResponse.from(savedTeam);
    }

    // ... 나머지 메서드들은 그대로 유지

    public List<TeamResponse> getMyTeams(Long currentUserId) {
        List<Team> teams = teamRepository.findTeamsByUserId(currentUserId);
        return teams.stream()
                .map(TeamResponse::from)
                .collect(Collectors.toList());
    }

    public TeamResponse getTeam(Long teamId, Long currentUserId) {
        Team team = teamRepository.findTeamByIdAndUserId(teamId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없거나 접근 권한이 없습니다: " + teamId));
        return TeamResponse.from(team);
    }

    public List<TeamMemberResponse> getTeamMembers(Long teamId, Long currentUserId) {
        checkTeamMembership(teamId, currentUserId);
        List<TeamMember> members = teamMemberRepository.findByTeam_Id(teamId);
        return members.stream()
                .map(TeamMemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamMemberResponse inviteMember(Long teamId, TeamMemberInviteRequest request, Long currentUserId) {
        checkTeamAdminPermission(teamId, currentUserId);

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다: " + request.getEmail()));

        if (teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, invitedUser.getId())) {
            throw new IllegalStateException("이미 팀 멤버입니다");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));

        TeamMember.TeamRole role = TeamMember.TeamRole.MEMBER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = TeamMember.TeamRole.ADMIN;
        }

        TeamMember newMember = new TeamMember();
        newMember.setTeam(team);
        newMember.setUser(invitedUser);
        newMember.setRole(role);

        TeamMember savedMember = teamMemberRepository.save(newMember);

        log.info("팀원 초대 완료 - teamId: {}, invitedEmail: {}, role: {}", teamId, request.getEmail(), role);

        return TeamMemberResponse.from(savedMember);
    }

    @Transactional
    public void removeMember(Long teamId, Long memberId, Long currentUserId) {
        checkTeamAdminPermission(teamId, currentUserId);

        TeamMember targetMember = teamMemberRepository.findByTeam_IdAndUser_Id(teamId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("팀 멤버를 찾을 수 없습니다"));

        if (targetMember.getRole() == TeamMember.TeamRole.OWNER) {
            throw new IllegalStateException("팀 OWNER는 제거할 수 없습니다");
        }

        teamMemberRepository.deleteByTeam_IdAndUser_Id(teamId, memberId);

        log.info("팀원 제거 완료 - teamId: {}, removedUserId: {}", teamId, memberId);
    }

    @Transactional
    public void deleteTeam(Long teamId, Long currentUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));

        if (!team.getCreatedBy().getId().equals(currentUserId)) {
            throw new IllegalStateException("팀 삭제 권한이 없습니다 (OWNER만 가능)");
        }

        teamRepository.delete(team);

        log.info("팀 삭제 완료 - teamId: {}, deletedBy: {}", teamId, currentUserId);
    }

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