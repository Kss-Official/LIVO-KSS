package com.livo.api.modules.category.service;

import com.livo.api.modules.category.dto.CategoryResponse;
import com.livo.api.modules.category.dto.CreateCategoryRequest;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> getCategories(UUID userId, CategoryDomainType domainType);

    CategoryResponse createCategory(UUID userId, CreateCategoryRequest request);

    void deleteCategory(UUID userId, UUID categoryId);
}
