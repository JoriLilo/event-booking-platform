package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.CategoryRequest;
import com.example.EventBookingPlatform.dto.CategoryResponse;
import com.example.EventBookingPlatform.entity.Category;
import com.example.EventBookingPlatform.exception.CategoryNotFoundException;
import com.example.EventBookingPlatform.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {

        if (categoryRequest.getCategoryName() == null || categoryRequest.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        Category category = new Category();
        category.setCategoryName(categoryRequest.getCategoryName());
        categoryRepository.save(category);

        CategoryResponse response = categoryToResponse(category);
        return response;
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest) {
        if (categoryRequest.getCategoryName() == null || categoryRequest.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        category.setCategoryName(categoryRequest.getCategoryName());
        categoryRepository.save(category);

        CategoryResponse response = categoryToResponse(category);

        return response;
    }

    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        CategoryResponse response = categoryToResponse(category);

        return response;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            CategoryResponse response = categoryToResponse(category);
            responses.add(response);
        }
        return responses;
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    private CategoryResponse categoryToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        return response;
    }
}