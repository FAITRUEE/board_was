package com.example.board.service;

import com.example.board.dto.kanban.*;
import com.example.board.entity.*;
import com.example.board.repository.*;
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
public class KanbanService {

    private final KanbanBoardRepository boardRepository;
    private final KanbanCardRepository cardRepository;
    private final KanbanCardCommentRepository commentRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final KanbanChecklistItemRepository checklistItemRepository;

    // ========================================
    // 칸반 보드 CRUD
    // ========================================

    /**
     * 칸반 보드 생성
     */
    @Transactional
    public KanbanBoardResponse createBoard(KanbanBoardCreateRequest request, Long currentUserId) {
        // 팀 멤버 확인
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(request.getTeamId(), currentUserId)) {
            throw new IllegalStateException("팀 멤버만 보드를 생성할 수 있습니다");
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + request.getTeamId()));

        KanbanBoard board = KanbanBoard.builder()
                .team(team)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        KanbanBoard savedBoard = boardRepository.save(board);

        log.info("칸반 보드 생성 완료 - boardId: {}, teamId: {}, createdBy: {}",
                savedBoard.getId(), request.getTeamId(), currentUserId);

        return KanbanBoardResponse.from(savedBoard);
    }

    /**
     * 내가 접근 가능한 칸반 보드 목록 조회
     */
    public List<KanbanBoardResponse> getMyBoards(Long currentUserId) {
        List<KanbanBoard> boards = boardRepository.findAccessibleBoardsByUserId(currentUserId);
        return boards.stream()
                .map(KanbanBoardResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 팀의 칸반 보드 목록 조회
     */
    public List<KanbanBoardResponse> getTeamBoards(Long teamId, Long currentUserId) {
        // 팀 멤버 확인
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(teamId, currentUserId)) {
            throw new IllegalStateException("팀 멤버만 보드를 조회할 수 있습니다");
        }

        List<KanbanBoard> boards = boardRepository.findByTeam_Id(teamId);
        return boards.stream()
                .map(KanbanBoardResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 칸반 보드 상세 조회 (카드 포함)
     */
    public KanbanBoardResponse getBoard(Long boardId, Long currentUserId) {
        KanbanBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("보드를 찾을 수 없습니다: " + boardId));

        // 팀 멤버 확인
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(board.getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        return KanbanBoardResponse.fromWithCards(board);
    }

    /**
     * 칸반 보드 삭제
     */
    @Transactional
    public void deleteBoard(Long boardId, Long currentUserId) {
        KanbanBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("보드를 찾을 수 없습니다: " + boardId));

        // OWNER 또는 ADMIN 확인
        checkTeamAdminPermission(board.getTeam().getId(), currentUserId);

        boardRepository.delete(board);

        log.info("칸반 보드 삭제 완료 - boardId: {}, deletedBy: {}", boardId, currentUserId);
    }

    // ========================================
    // 칸반 카드 CRUD
    // ========================================

    // createCard 메서드 수정
    @Transactional
    public KanbanCardResponse createCard(Long boardId, KanbanCardCreateRequest request, Long currentUserId) {
        KanbanBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("보드를 찾을 수 없습니다: " + boardId));

        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(board.getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("팀 멤버만 카드를 생성할 수 있습니다");
        }

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserId));

        KanbanCard.CardStatus status = parseStatus(request.getStatus());
        KanbanCard.Priority priority = parsePriority(request.getPriority()); // ✅ 추가

        List<KanbanCard> existingCards = cardRepository.findByBoard_IdAndStatusOrderByPositionAsc(boardId, status);
        int newPosition = existingCards.isEmpty() ? 0 : existingCards.get(existingCards.size() - 1).getPosition() + 1;

        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo()).orElse(null);
        }

        KanbanCard card = KanbanCard.builder()
                .board(board)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .position(newPosition)
                .assignedTo(assignedTo)
                .createdBy(creator)
                .dueDate(request.getDueDate()) // ✅ 추가
                .priority(priority) // ✅ 추가
                .build();

        KanbanCard savedCard = cardRepository.save(card);

        log.info("칸반 카드 생성 완료 - cardId: {}, boardId: {}, createdBy: {}",
                savedCard.getId(), boardId, currentUserId);

        return KanbanCardResponse.from(savedCard);
    }

    // updateCard 메서드 수정
    @Transactional
    public KanbanCardResponse updateCard(Long boardId, Long cardId, KanbanCardUpdateRequest request, Long currentUserId) {
        KanbanCard card = cardRepository.findByIdAndBoard_Id(cardId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(card.getBoard().getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        if (request.getTitle() != null) {
            card.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            card.setDescription(request.getDescription());
        }
        if (request.getAssignedTo() != null) {
            User assignedTo = userRepository.findById(request.getAssignedTo()).orElse(null);
            card.setAssignedTo(assignedTo);
        }
        if (request.getDueDate() != null) { // ✅ 추가
            card.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) { // ✅ 추가
            card.setPriority(parsePriority(request.getPriority()));
        }

        KanbanCard updatedCard = cardRepository.save(card);

        log.info("칸반 카드 수정 완료 - cardId: {}, updatedBy: {}", cardId, currentUserId);

        return KanbanCardResponse.from(updatedCard);
    }

    // ✅ 추가: Priority 파싱 헬퍼 메서드
    private KanbanCard.Priority parsePriority(String priority) {
        if (priority == null) {
            return KanbanCard.Priority.MEDIUM;
        }
        try {
            return KanbanCard.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return KanbanCard.Priority.MEDIUM;
        }
    }

    /**
     * 칸반 카드 이동 (상태 변경 + 위치 변경)
     */
    @Transactional
    public KanbanCardResponse moveCard(Long boardId, Long cardId, KanbanCardMoveRequest request, Long currentUserId) {
        KanbanCard card = cardRepository.findByIdAndBoard_Id(cardId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        // 팀 멤버 확인
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(card.getBoard().getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        KanbanCard.CardStatus newStatus = parseStatus(request.getStatus());
        KanbanCard.CardStatus oldStatus = card.getStatus();
        int newPosition = request.getPosition();

        // 같은 컬럼 내 이동
        if (oldStatus == newStatus) {
            reorderCardsInSameColumn(boardId, newStatus, card.getPosition(), newPosition);
        }
        // 다른 컬럼으로 이동
        else {
            // 기존 컬럼에서 제거 후 재정렬
            reorderAfterRemoval(boardId, oldStatus, card.getPosition());
            // 새 컬럼에 삽입
            insertCardIntoColumn(boardId, newStatus, newPosition);
        }

        card.setStatus(newStatus);
        card.setPosition(newPosition);

        KanbanCard movedCard = cardRepository.save(card);

        log.info("칸반 카드 이동 완료 - cardId: {}, from: {}/{} to: {}/{}",
                cardId, oldStatus, card.getPosition(), newStatus, newPosition);

        return KanbanCardResponse.from(movedCard);
    }

    /**
     * 칸반 카드 삭제
     */
    @Transactional
    public void deleteCard(Long boardId, Long cardId, Long currentUserId) {
        KanbanCard card = cardRepository.findByIdAndBoard_Id(cardId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        // 팀 멤버 확인
        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(card.getBoard().getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        // 삭제 후 position 재정렬
        reorderAfterRemoval(boardId, card.getStatus(), card.getPosition());

        cardRepository.delete(card);

        log.info("칸반 카드 삭제 완료 - cardId: {}, deletedBy: {}", cardId, currentUserId);
    }

    // ========================================
    // 헬퍼 메서드
    // ========================================

    private KanbanCard.CardStatus parseStatus(String status) {
        if (status == null) {
            return KanbanCard.CardStatus.TODO;
        }
        try {
            return KanbanCard.CardStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return KanbanCard.CardStatus.TODO;
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

    private void reorderCardsInSameColumn(Long boardId, KanbanCard.CardStatus status, int oldPosition, int newPosition) {
        List<KanbanCard> cards = cardRepository.findByBoard_IdAndStatusOrderByPositionAsc(boardId, status);

        if (oldPosition < newPosition) {
            // 아래로 이동: oldPosition+1 ~ newPosition 사이 카드들을 -1
            for (KanbanCard c : cards) {
                if (c.getPosition() > oldPosition && c.getPosition() <= newPosition) {
                    c.setPosition(c.getPosition() - 1);
                }
            }
        } else if (oldPosition > newPosition) {
            // 위로 이동: newPosition ~ oldPosition-1 사이 카드들을 +1
            for (KanbanCard c : cards) {
                if (c.getPosition() >= newPosition && c.getPosition() < oldPosition) {
                    c.setPosition(c.getPosition() + 1);
                }
            }
        }

        cardRepository.saveAll(cards);
    }

    private void reorderAfterRemoval(Long boardId, KanbanCard.CardStatus status, int removedPosition) {
        List<KanbanCard> cards = cardRepository.findCardsAfterPosition(boardId, status, removedPosition);
        for (KanbanCard c : cards) {
            c.setPosition(c.getPosition() - 1);
        }
        cardRepository.saveAll(cards);
    }

    private void insertCardIntoColumn(Long boardId, KanbanCard.CardStatus status, int insertPosition) {
        List<KanbanCard> cards = cardRepository.findCardsAfterPosition(boardId, status, insertPosition);
        for (KanbanCard c : cards) {
            c.setPosition(c.getPosition() + 1);
        }
        cardRepository.saveAll(cards);
    }

    // 체크리스트 아이템 추가
    @Transactional
    public KanbanCardResponse addChecklistItem(Long boardId, Long cardId, ChecklistItemRequest request, Long currentUserId) {
        KanbanCard card = cardRepository.findByIdAndBoard_Id(cardId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(card.getBoard().getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        List<KanbanChecklistItem> existingItems = checklistItemRepository.findByCard_IdOrderByPositionAsc(cardId);
        int newPosition = existingItems.isEmpty() ? 0 : existingItems.get(existingItems.size() - 1).getPosition() + 1;

        KanbanChecklistItem item = KanbanChecklistItem.builder()
                .card(card)
                .text(request.getText())
                .completed(false)
                .position(newPosition)
                .build();

        checklistItemRepository.save(item);

        log.info("체크리스트 아이템 추가 완료 - cardId: {}, itemId: {}", cardId, item.getId());

        return KanbanCardResponse.from(cardRepository.findById(cardId).orElseThrow());
    }

    // 체크리스트 아이템 토글
    @Transactional
    public KanbanCardResponse toggleChecklistItem(Long boardId, Long cardId, Long itemId, Long currentUserId) {
        KanbanCard card = cardRepository.findByIdAndBoard_Id(cardId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(card.getBoard().getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        KanbanChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("체크리스트 아이템을 찾을 수 없습니다: " + itemId));

        item.setCompleted(!item.getCompleted());
        checklistItemRepository.save(item);

        log.info("체크리스트 아이템 토글 완료 - itemId: {}, completed: {}", itemId, item.getCompleted());

        return KanbanCardResponse.from(cardRepository.findById(cardId).orElseThrow());
    }

    // 체크리스트 아이템 삭제
    @Transactional
    public KanbanCardResponse deleteChecklistItem(Long boardId, Long cardId, Long itemId, Long currentUserId) {
        KanbanCard card = cardRepository.findByIdAndBoard_Id(cardId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        if (!teamMemberRepository.existsByTeam_IdAndUser_Id(card.getBoard().getTeam().getId(), currentUserId)) {
            throw new IllegalStateException("접근 권한이 없습니다");
        }

        checklistItemRepository.deleteById(itemId);

        log.info("체크리스트 아이템 삭제 완료 - itemId: {}", itemId);

        return KanbanCardResponse.from(cardRepository.findById(cardId).orElseThrow());
    }
}