package com.example.board.service;

import com.example.board.dto.collab.CollabRoomCreateRequest;
import com.example.board.dto.collab.CollabRoomContentRequest;
import com.example.board.dto.collab.CollabRoomPublishRequest;
import com.example.board.dto.collab.CollabRoomResponse;
import com.example.board.dto.collab.EditHistoryResponse;
import com.example.board.entity.*;
import com.example.board.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollabRoomService {

    /** 자동 저장 시 이 간격마다 한 번씩만 버전 기록을 남긴다 */
    private static final long AUTO_SNAPSHOT_INTERVAL_MINUTES = 5;

    private final CollabRoomRepository collabRoomRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;
    private final EditHistoryRepository editHistoryRepository;

    public List<CollabRoomResponse> getMyRooms(Long userId) {
        return collabRoomRepository.findActiveRoomsByUserId(userId).stream()
                .map(CollabRoomResponse::from)
                .collect(Collectors.toList());
    }

    public CollabRoomResponse getRoom(Long roomId, Long userId) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);
        return CollabRoomResponse.from(room);
    }

    @Transactional
    public CollabRoomResponse createRoom(Long userId, CollabRoomCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Team team = teamRepository.findTeamByIdAndUserId(request.getTeamId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없거나 팀원이 아닙니다."));

        CollabRoom room = CollabRoom.builder()
                .team(team)
                .createdBy(user)
                .title(request.getTitle() != null ? request.getTitle() : "")
                .content("")
                .isPublished(false)
                .build();

        return CollabRoomResponse.from(collabRoomRepository.save(room));
    }

    @Transactional
    public CollabRoomResponse updateContent(Long roomId, Long userId, CollabRoomContentRequest request) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);

        if (request.getTitle() != null) {
            room.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            room.setContent(request.getContent());
        }

        boolean snapshotDue = editHistoryRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(roomId)
                .map(last -> last.getCreatedAt().isBefore(
                        LocalDateTime.now().minusMinutes(AUTO_SNAPSHOT_INTERVAL_MINUTES)))
                .orElse(true);
        if (snapshotDue) {
            saveSnapshot(room, userId, "자동 저장");
        }

        return CollabRoomResponse.from(room);
    }

    public List<EditHistoryResponse> getHistory(Long roomId, Long userId) {
        findRoomWithMemberCheck(roomId, userId);
        return editHistoryRepository.findTop50ByRoomIdOrderByCreatedAtDescIdDesc(roomId).stream()
                .map(EditHistoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 내용을 버전으로 직접 저장
     */
    @Transactional
    public void createSnapshot(Long roomId, Long userId, String description) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);
        saveSnapshot(room, userId, description != null && !description.isBlank() ? description : "수동 저장");
    }

    /**
     * 이전 버전으로 복원 (복원 전 현재 내용도 버전으로 남겨 되돌릴 수 있게 함)
     */
    @Transactional
    public CollabRoomResponse restoreSnapshot(Long roomId, Long historyId, Long userId) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);
        EditHistory target = editHistoryRepository.findByIdAndRoomId(historyId, roomId)
                .orElseThrow(() -> new IllegalArgumentException("버전 기록을 찾을 수 없습니다."));

        saveSnapshot(room, userId, "복원 전 자동 저장");

        room.setTitle(target.getTitle());
        room.setContent(target.getContentSnapshot());

        return CollabRoomResponse.from(room);
    }

    @Transactional
    public Long publishAsPost(Long roomId, Long userId, CollabRoomPublishRequest request) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);

        if (room.getIsPublished()) {
            throw new IllegalStateException("이미 게시글로 발행된 방입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        Post post = Post.builder()
                .title(room.getTitle() != null && !room.getTitle().isBlank() ? room.getTitle() : "제목 없음")
                .content(room.getContent() != null ? room.getContent() : "")
                .author(user)
                .category(category)
                .team(room.getTeam())
                .isCollaborative(true)
                .views(0)
                .likeCount(0)
                .commentCount(0)
                .isSecret(false)
                .build();

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> tags = tagService.getOrCreateTags(request.getTags());
            for (Tag tag : tags) {
                post.addTag(tag);
            }
        }

        Post savedPost = postRepository.save(post);

        room.setIsPublished(true);
        room.setPublishedPostId(savedPost.getId());
        saveSnapshot(room, userId, "게시글로 발행");

        return savedPost.getId();
    }

    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);
        collabRoomRepository.delete(room);
    }

    /**
     * 가장 최근 버전과 내용이 같으면 저장하지 않는다
     */
    private void saveSnapshot(CollabRoom room, Long userId, String description) {
        boolean unchanged = editHistoryRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(room.getId())
                .map(last -> Objects.equals(last.getTitle(), room.getTitle())
                        && Objects.equals(last.getContentSnapshot(), room.getContent()))
                .orElse(false);
        if (unchanged) {
            return;
        }

        editHistoryRepository.save(EditHistory.builder()
                .roomId(room.getId())
                .user(userRepository.getReferenceById(userId))
                .title(room.getTitle())
                .contentSnapshot(room.getContent())
                .changeDescription(description)
                .build());
    }

    private CollabRoom findRoomWithMemberCheck(Long roomId, Long userId) {
        CollabRoom room = collabRoomRepository.findByIdWithTeamAndMembers(roomId)
                .orElseThrow(() -> new IllegalArgumentException("공동 편집 방을 찾을 수 없습니다."));

        boolean isMember = room.getTeam().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (!isMember) {
            throw new IllegalArgumentException("팀원만 접근할 수 있습니다.");
        }

        return room;
    }
}
