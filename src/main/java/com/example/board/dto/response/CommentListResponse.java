package com.example.board.dto.response;

import com.example.board.entity.Comment;
import jakarta.validation.constraints.NotBlank;
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
public class CommentListResponse {
    private List<CommentResponse> comments;
    private Integer total;

    public static CommentListResponse of(List<Comment> comments) {
        return CommentListResponse.builder()
                .comments(comments.stream()
                        .map(CommentResponse::fromEntity)
                        .collect(Collectors.toList()))
                .total(comments.size())
                .build();
    }
}