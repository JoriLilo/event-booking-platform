package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.CategoryRequest;
import com.example.EventBookingPlatform.dto.CategoryResponse;
import com.example.EventBookingPlatform.entity.Category;
import com.example.EventBookingPlatform.exception.CategoryNotFoundException;
import com.example.EventBookingPlatform.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryRequest categoryRequest;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest();
        categoryRequest.setCategoryName("Technology");

        category = new Category();
        category.setId(1L);
        category.setCategoryName("Technology");
    }

    @Test
    void testCreateCategorySuccess() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(categoryRequest);

        assertNotNull(response);
        assertEquals("Technology", response.getCategoryName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategoryWithEmptyName() {
        categoryRequest.setCategoryName("");

        assertThrows(IllegalArgumentException.class, () ->
                categoryService.createCategory(categoryRequest));
    }

    @Test
    void testCreateCategoryWithNullName() {
        categoryRequest.setCategoryName(null);

        assertThrows(IllegalArgumentException.class, () ->
                categoryService.createCategory(categoryRequest));
    }

    @Test
    void testUpdateCategorySuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setCategoryName("Updated Technology");
        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);

        assertNotNull(response);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testUpdateCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () ->
                categoryService.updateCategory(1L, categoryRequest));
    }

    @Test
    void testGetCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertNotNull(response);
        assertEquals("Technology", response.getCategoryName());
    }

    @Test
    void testGetCategoryByIdNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () ->
                categoryService.getCategoryById(1L));
    }

    @Test
    void testGetAllCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(category);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void testDeleteCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () ->
                categoryService.deleteCategory(1L));
    }
}