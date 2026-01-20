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
                .build();
    }

    public static PostResponse fromEntity(Post post, boolean isLiked) {
        PostResponse response = fromEntity(post);
        response.setIsLiked(isLiked);
        return response;
    }
}
