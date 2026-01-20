package com.example.board.dto.response;

import com.example.board.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private List<AttachmentResponse> attachments;  // ✅ 추가

    // ✅ 비밀게시글 여부 추가
    private Boolean isSecret;

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
                .isSecret(post.getIsSecret())  // ✅ 추가
                .attachments(post.getAttachments() != null
                        ? post.getAttachments().stream()
                        .map(AttachmentResponse::fromEntity)
                        .collect(Collectors.toList())
                        : null)  // ✅ 추가
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
                .content("🔒 비밀글입니다.")  // 내용 숨김
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(false)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isSecret(true)
                .attachments(null)  // 첨부파일도 숨김
                .build();
    }
}