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

    // 카테고리별 조회
    Page<Post> findByCategory(Category category, Pageable pageable);
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

    // 태그별 조회
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id = :tagId")
    Page<Post> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id IN :tagIds")
    Page<Post> findByTagIdIn(@Param("tagIds") List<Long> tagIds, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.name = :tagName")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    // 카테고리 + 태그 필터
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.category.id = :categoryId AND t.name = :tagName")
    Page<Post> findByCategoryIdAndTagName(@Param("categoryId") Long categoryId, @Param("tagName") String tagName, Pageable pageable);

    // ✅ 검색 쿼리 추가 (제목 + 내용)
    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ✅ 검색 + 카테고리 필터
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeywordAndCategoryId(@Param("keyword") String keyword,
                                            @Param("categoryId") Long categoryId,
                                            Pageable pageable);

    // ✅ 검색 + 태그 필터
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.name = :tagName AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeywordAndTagName(@Param("keyword") String keyword,
                                         @Param("tagName") String tagName,
                                         Pageable pageable);

    // ✅ 검색 + 카테고리 + 태그 필터
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.category.id = :categoryId AND t.name = :tagName AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeywordAndCategoryIdAndTagName(@Param("keyword") String keyword,
                                                      @Param("categoryId") Long categoryId,
                                                      @Param("tagName") String tagName,
                                                      Pageable pageable);

    // ✅ 제목으로만 검색
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // ✅ 내용으로만 검색
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    // ✅ 작성자 이름으로 검색
    @Query("SELECT p FROM Post p JOIN p.author a WHERE LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByAuthor(@Param("keyword") String keyword, Pageable pageable);
}