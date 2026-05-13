package com.example.board.dto.response;

import com.example.board.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Integer views;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;  // 현재 사용자가 좋아요 했는지
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentResponse> attachments;
    private CategoryResponse category;
    private Boolean isSecret;
    private Boolean isCollaborative;
    private Long teamId;
    private String teamName;
    private List<TagResponse> tags;

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(false)  // 서비스에서 설정
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isSecret(post.getIsSecret())
                .isCollaborative(post.getIsCollaborative() != null ? post.getIsCollaborative() : false)
                .teamId(post.getTeam() != null ? post.getTeam().getId() : null)
                .teamName(post.getTeam() != null ? post.getTeam().getName() : null)
                .category(CategoryResponse.fromEntity(post.getCategory()))
                .attachments(post.getAttachments() != null
                        ? post.getAttachments().stream()
                        .map(AttachmentResponse::fromEntity)
                        .collect(Collectors.toList())
                        : null)
                .tags(post.getTags() != null && !post.getTags().isEmpty()
                        ? post.getTags().stream()
                        .map(TagResponse::fromEntity)
                        .filter(tag -> tag != null)  // null 필터링
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    public static PostResponse fromEntity(Post post, boolean isLiked) {
        PostResponse response = fromEntity(post);
        response.setIsLiked(isLiked);
        return response;
    }

    // ✅ 비밀게시글용: 내용 숨김 처리
    public static PostResponse secretPostSummary(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content("🔒 비밀글입니다.")
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(false)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .attachments(new ArrayList<>())
                .isSecret(true)
                .category(CategoryResponse.fromEntity(post.getCategory()))
                .tags(new ArrayList<>())  // ✅ 비밀글은 태그 안 보여줌
                .build();
    }
}