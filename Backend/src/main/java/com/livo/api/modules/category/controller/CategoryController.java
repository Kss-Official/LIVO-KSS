package com.livo.api.modules.category.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.category.dto.CategoryResponse;
import com.livo.api.modules.category.dto.CreateCategoryRequest;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
import com.livo.api.modules.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @CurrentUser UUID userId,
            @RequestParam(required = false) CategoryDomainType domainType
    ) {
        List<CategoryResponse> list = categoryService.getCategories(userId, domainType);
        return ResponseEntity.ok(ApiResponse.success(list, "Categories retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
