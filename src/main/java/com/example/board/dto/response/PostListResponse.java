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
public class PostListResponse {
    private List<PostResponse> posts;
    private Integer total;
    private Integer page;
    private Integer size;

    public static PostListResponse of(List<Post> posts, int page, int size, long total) {
        return PostListResponse.builder()
                .posts(posts.stream()
                        .map(PostResponse::fromEntity)
                        .collect(Collectors.toList()))
                .total((int) total)
                .page(page)
                .size(size)
                .build();
    }
}
