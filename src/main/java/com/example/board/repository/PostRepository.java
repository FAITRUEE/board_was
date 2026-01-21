package com.example.board.repository;

import com.example.board.entity.Category;
import com.example.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAttachments(@Param("id") Long id);

    // 카테고리별 조회 추가
    Page<Post> findByCategory(Category category, Pageable pageable);

    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

    // 특정 태그를 가진 게시글 조회
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id = :tagId")
    Page<Post> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    // 여러 태그를 가진 게시글 조회 (OR 조건)
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id IN :tagIds")
    Page<Post> findByTagIdIn(@Param("tagIds") List<Long> tagIds, Pageable pageable);

    // 태그 이름으로 게시글 조회
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.name = :tagName")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);
}
