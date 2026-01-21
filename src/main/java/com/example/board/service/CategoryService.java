package com.example.board.service;

import com.example.board.dto.request.CreateCategoryRequest;
import com.example.board.dto.request.UpdateCategoryRequest;
import com.example.board.dto.response.CategoryResponse;
import com.example.board.entity.Category;
import com.example.board.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 전체 카테고리 조회
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 카테고리 상세 조회
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        return CategoryResponse.fromEntity(category);
    }

    // 카테고리 생성
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .color(request.getColor())
                .icon(request.getIcon())
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }

    // 카테고리 수정
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        category.setName(request.getName());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        category.setDescription(request.getDescription());

        return CategoryResponse.fromEntity(category);
    }

    // 카테고리 삭제
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        categoryRepository.delete(category);
    }
}