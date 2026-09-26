package com.livo.api.modules.category.service;

import com.livo.api.common.exception.ConflictException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.category.dto.CategoryResponse;
import com.livo.api.modules.category.dto.CreateCategoryRequest;
import com.livo.api.modules.category.entity.CategoryEntity;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
import com.livo.api.modules.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(UUID userId, CategoryDomainType domainType) {
        List<CategoryEntity> list = (domainType != null)
                ? categoryRepository.findAllByUserIdAndDomainTypeAndDeletedAtIsNullOrderByNameAsc(userId, domainType)
                : categoryRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);

        return list.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(UUID userId, CreateCategoryRequest request) {
        String name = request.getName().trim();
        CategoryDomainType domainType = request.getDomainType();

        if (categoryRepository.existsByUserIdAndDomainTypeAndNameIgnoreCaseAndDeletedAtIsNull(userId, domainType, name)) {
            throw new ConflictException("Category with name '" + name + "' already exists for domain " + domainType);
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(name)
                .iconKey(request.getIconKey() != null ? request.getIconKey().trim() : "folder")
                .colorHex(request.getColorHex() != null ? request.getColorHex().toUpperCase() : "#3B82F6")
                .domainType(domainType)
                .build();
        category.setUserId(userId);
        category.setVersion(1L);

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID userId, UUID categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.markDeleted();
        categoryRepository.save(category);
    }
}
