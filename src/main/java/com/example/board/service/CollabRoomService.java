package com.example.board.service;

import com.example.board.dto.collab.CollabRoomCreateRequest;
import com.example.board.dto.collab.CollabRoomContentRequest;
import com.example.board.dto.collab.CollabRoomPublishRequest;
import com.example.board.dto.collab.CollabRoomResponse;
import com.example.board.entity.*;
import com.example.board.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollabRoomService {

    private final CollabRoomRepository collabRoomRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;

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

        return savedPost.getId();
    }

    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        CollabRoom room = findRoomWithMemberCheck(roomId, userId);
        collabRoomRepository.delete(room);
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
