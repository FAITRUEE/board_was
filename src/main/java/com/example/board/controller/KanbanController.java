package com.example.board.controller;

import com.example.board.dto.kanban.*;
import com.example.board.security.UserPrincipal;
import com.example.board.service.KanbanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kanban")
@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService kanbanService;

    // ========================================
    // 칸반 보드 API
    // ========================================

    /**
     * 칸반 보드 생성
     */
    @PostMapping("/boards")
    public ResponseEntity<KanbanBoardResponse> createBoard(
            @Valid @RequestBody KanbanBoardCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanBoardResponse response = kanbanService.createBoard(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내가 접근 가능한 칸반 보드 목록 조회
     */
    @GetMapping("/boards/my")
    public ResponseEntity<List<KanbanBoardResponse>> getMyBoards(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<KanbanBoardResponse> boards = kanbanService.getMyBoards(currentUser.getId());
        return ResponseEntity.ok(boards);
    }

    /**
     * 팀의 칸반 보드 목록 조회
     */
    @GetMapping("/teams/{teamId}/boards")
    public ResponseEntity<List<KanbanBoardResponse>> getTeamBoards(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<KanbanBoardResponse> boards = kanbanService.getTeamBoards(teamId, currentUser.getId());
        return ResponseEntity.ok(boards);
    }

    /**
     * 칸반 보드 상세 조회 (카드 포함)
     */
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<KanbanBoardResponse> getBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanBoardResponse board = kanbanService.getBoard(boardId, currentUser.getId());
        return ResponseEntity.ok(board);
    }

    /**
     * 칸반 보드 삭제
     */
    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        kanbanService.deleteBoard(boardId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // 칸반 카드 API
    // ========================================

    /**
     * 칸반 카드 생성
     */
    @PostMapping("/boards/{boardId}/cards")
    public ResponseEntity<KanbanCardResponse> createCard(
            @PathVariable Long boardId,
            @Valid @RequestBody KanbanCardCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanCardResponse response = kanbanService.createCard(boardId, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 칸반 카드 수정
     */
    @PutMapping("/boards/{boardId}/cards/{cardId}")
    public ResponseEntity<KanbanCardResponse> updateCard(
            @PathVariable Long boardId,
            @PathVariable Long cardId,
            @Valid @RequestBody KanbanCardUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanCardResponse response = kanbanService.updateCard(boardId, cardId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 칸반 카드 이동 (드래그 앤 드롭)
     */
    @PatchMapping("/boards/{boardId}/cards/{cardId}/move")
    public ResponseEntity<KanbanCardResponse> moveCard(
            @PathVariable Long boardId,
            @PathVariable Long cardId,
            @Valid @RequestBody KanbanCardMoveRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanCardResponse response = kanbanService.moveCard(boardId, cardId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 칸반 카드 삭제
     */
    @DeleteMapping("/boards/{boardId}/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long boardId,
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        kanbanService.deleteCard(boardId, cardId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ========================================
// 체크리스트 API
// ========================================

    /**
     * 체크리스트 아이템 추가
     */
    @PostMapping("/boards/{boardId}/cards/{cardId}/checklist")
    public ResponseEntity<KanbanCardResponse> addChecklistItem(
            @PathVariable Long boardId,
            @PathVariable Long cardId,
            @RequestBody ChecklistItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanCardResponse response = kanbanService.addChecklistItem(boardId, cardId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 체크리스트 아이템 토글
     */
    @PatchMapping("/boards/{boardId}/cards/{cardId}/checklist/{itemId}/toggle")
    public ResponseEntity<KanbanCardResponse> toggleChecklistItem(
            @PathVariable Long boardId,
            @PathVariable Long cardId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanCardResponse response = kanbanService.toggleChecklistItem(boardId, cardId, itemId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 체크리스트 아이템 삭제
     */
    @DeleteMapping("/boards/{boardId}/cards/{cardId}/checklist/{itemId}")
    public ResponseEntity<KanbanCardResponse> deleteChecklistItem(
            @PathVariable Long boardId,
            @PathVariable Long cardId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        KanbanCardResponse response = kanbanService.deleteChecklistItem(boardId, cardId, itemId, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}