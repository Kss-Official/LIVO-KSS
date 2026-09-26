package com.livo.api.modules.category.repository;

import com.livo.api.modules.category.entity.CategoryEntity;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findAllByUserIdAndDomainTypeAndDeletedAtIsNullOrderByNameAsc(UUID userId, CategoryDomainType domainType);

    List<CategoryEntity> findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(UUID userId);

    Optional<CategoryEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<CategoryEntity> findByUserIdAndDomainTypeAndNameIgnoreCaseAndDeletedAtIsNull(UUID userId, CategoryDomainType domainType, String name);

    boolean existsByUserIdAndDomainTypeAndNameIgnoreCaseAndDeletedAtIsNull(UUID userId, CategoryDomainType domainType, String name);
}
