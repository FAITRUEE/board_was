package com.example.board.dto.response;

import com.example.board.entity.Post;
import com.example.board.service.PostLikeService;
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
public class PostListResponse {
    private List<PostResponse> posts;
    private Integer total;
    private Integer page;
    private Integer size;
    private Integer totalPages;

    public static PostListResponse of(List<Post> posts, int page, int size, long total, Long userId, PostLikeService postLikeService) {
        return PostListResponse.builder()
                .posts(posts.stream()
                        .map(post -> {
                            boolean isLiked = postLikeService.isLikedByUser(post.getId(), userId);
                            return PostResponse.fromEntity(post, isLiked);
                        })
                        .collect(Collectors.toList()))
                .total((int) total)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }
}