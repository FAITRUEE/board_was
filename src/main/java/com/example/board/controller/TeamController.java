package com.example.board.controller;

import com.example.board.dto.team.TeamCreateRequest;
import com.example.board.dto.team.TeamMemberInviteRequest;
import com.example.board.dto.team.TeamMemberResponse;
import com.example.board.dto.team.TeamResponse;
import com.example.board.security.UserPrincipal;
import com.example.board.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;

    /**
     * 팀 생성
     */
    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("========================================");
        log.info("🎯 TeamController.createTeam 진입");
        log.info("📝 Request: {}", request);
        log.info("👤 CurrentUser: {}", currentUser);
        log.info("👤 UserId: {}", currentUser != null ? currentUser.getId() : "NULL");
        log.info("========================================");

        try {
            TeamResponse response = teamService.createTeam(request, currentUser.getId());
            log.info("✅ 팀 생성 성공: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("❌ 팀 생성 실패", e);
            throw e;
        }
    }

    /**
     * 내가 속한 팀 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<TeamResponse>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("========================================");
        log.info("🎯 TeamController.getMyTeams 진입");
        log.info("👤 CurrentUser: {}", currentUser);
        log.info("👤 UserId: {}", currentUser != null ? currentUser.getId() : "NULL");
        log.info("========================================");

        try {
            List<TeamResponse> teams = teamService.getMyTeams(currentUser.getId());
            log.info("✅ 팀 조회 성공: {} 개", teams.size());
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("❌ 팀 조회 실패", e);
            throw e;
        }
    }

    /**
     * 팀 상세 조회
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TeamResponse team = teamService.getTeam(teamId, currentUser.getId());
        return ResponseEntity.ok(team);
    }

    /**
     * 팀 멤버 목록 조회
     */
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<TeamMemberResponse> members = teamService.getTeamMembers(teamId, currentUser.getId());
        return ResponseEntity.ok(members);
    }

    /**
     * 팀원 초대
     */
    @PostMapping("/{teamId}/members")
    public ResponseEntity<TeamMemberResponse> inviteMember(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamMemberInviteRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TeamMemberResponse member = teamService.inviteMember(teamId, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    /**
     * 팀원 제거
     */
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        teamService.removeMember(teamId, memberId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 팀 삭제
     */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        teamService.deleteTeam(teamId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}