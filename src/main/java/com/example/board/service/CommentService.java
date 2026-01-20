package com.example.board.service;

import com.example.board.dto.response.CommentListResponse;
import com.example.board.dto.response.CommentResponse;
import com.example.board.dto.request.CreateCommentRequest;
import com.example.board.dto.request.UpdateCommentRequest;
import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import com.example.board.entity.User;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentListResponse getComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return CommentListResponse.of(comments);
    }

    @Transactional
    public CommentResponse createComment(Long userId, Long postId, CreateCommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 게시글 댓글 수 증가
        post.incrementComments();

        return CommentResponse.fromEntity(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(request.getContent());

        return CommentResponse.fromEntity(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        Post post = comment.getPost();
        commentRepository.delete(comment);

        // 게시글 댓글 수 감소
        post.decrementComments();
    }
}