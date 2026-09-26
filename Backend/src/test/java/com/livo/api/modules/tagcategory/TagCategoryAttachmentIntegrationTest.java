package com.livo.api.modules.tagcategory;

import com.livo.api.modules.attachment.entity.AttachmentEntity;
import com.livo.api.modules.attachment.entity.enums.AttachmentEntityType;
import com.livo.api.modules.attachment.repository.AttachmentRepository;
import com.livo.api.modules.category.entity.CategoryEntity;
import com.livo.api.modules.category.entity.enums.CategoryDomainType;
import com.livo.api.modules.category.repository.CategoryRepository;
import com.livo.api.modules.tag.entity.TagEntity;
import com.livo.api.modules.tag.repository.TagRepository;
import com.livo.api.modules.user.entity.UserEntity;
import com.livo.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TagCategoryAttachmentIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firebaseUid("test_part2_fb_" + UUID.randomUUID())
                .email("test.part2." + UUID.randomUUID() + "@example.com")
                .fullName("Part 2 Test User")
                .timezone("Asia/Kolkata")
                .language("en")
                .currency("INR")
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.findById(testUser.getId()).ifPresent(u -> {
                attachmentRepository.deleteAll(attachmentRepository.findAllByUserIdAndEntityTypeAndEntityIdAndDeletedAtIsNull(u.getId(), AttachmentEntityType.TASK, UUID.randomUUID()));
                categoryRepository.deleteAll(categoryRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(u.getId()));
                tagRepository.deleteAll(tagRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(u.getId()));
                userRepository.delete(u);
            });
        }
    }

    @Test
    @DisplayName("Test 1: TagEntity CRUD, soft delete, and name search")
    void testTagCrud() {
        TagEntity urgentTag = TagEntity.builder()
                .name("Urgent Work")
                .colorHex("#EF4444")
                .build();
        urgentTag.setUserId(testUser.getId());
        urgentTag = tagRepository.saveAndFlush(urgentTag);

        // Fetch by ID + User
        Optional<TagEntity> found = tagRepository.findByIdAndUserIdAndDeletedAtIsNull(urgentTag.getId(), testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Urgent Work");
        assertThat(found.get().getColorHex()).isEqualTo("#EF4444");

        // Case-insensitive lookup
        Optional<TagEntity> byName = tagRepository.findByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(testUser.getId(), "urgent work");
        assertThat(byName).isPresent();
        assertThat(byName.get().getId()).isEqualTo(urgentTag.getId());

        // Soft delete
        urgentTag.markDeleted();
        tagRepository.saveAndFlush(urgentTag);

        assertThat(tagRepository.findByIdAndUserIdAndDeletedAtIsNull(urgentTag.getId(), testUser.getId())).isEmpty();
        assertThat(tagRepository.findByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(testUser.getId(), "urgent work")).isEmpty();
    }

    @Test
    @DisplayName("Test 2: CategoryEntity domain type filtering and ordering")
    void testCategoryDomainFiltering() {
        CategoryEntity workTaskCat = CategoryEntity.builder()
                .name("Work Tasks")
                .iconKey("briefcase")
                .colorHex("#3B82F6")
                .domainType(CategoryDomainType.TASK)
                .build();
        workTaskCat.setUserId(testUser.getId());

        CategoryEntity foodExpenseCat = CategoryEntity.builder()
                .name("Dining Out")
                .iconKey("utensils")
                .colorHex("#F59E0B")
                .domainType(CategoryDomainType.EXPENSE)
                .build();
        foodExpenseCat.setUserId(testUser.getId());

        categoryRepository.saveAndFlush(workTaskCat);
        categoryRepository.saveAndFlush(foodExpenseCat);

        // Filter by TASK domain
        List<CategoryEntity> taskCats = categoryRepository.findAllByUserIdAndDomainTypeAndDeletedAtIsNullOrderByNameAsc(testUser.getId(), CategoryDomainType.TASK);
        assertThat(taskCats).hasSize(1);
        assertThat(taskCats.get(0).getName()).isEqualTo("Work Tasks");

        // Filter by EXPENSE domain
        List<CategoryEntity> expenseCats = categoryRepository.findAllByUserIdAndDomainTypeAndDeletedAtIsNullOrderByNameAsc(testUser.getId(), CategoryDomainType.EXPENSE);
        assertThat(expenseCats).hasSize(1);
        assertThat(expenseCats.get(0).getName()).isEqualTo("Dining Out");
    }

    @Test
    @DisplayName("Test 3: AttachmentEntity polymorphic metadata persistence")
    void testAttachmentPersistence() {
        UUID linkedTaskId = UUID.randomUUID();

        AttachmentEntity attachment = AttachmentEntity.builder()
                .entityType(AttachmentEntityType.TASK)
                .entityId(linkedTaskId)
                .storageKey("livo_attachments/sample_key_123")
                .fileUrl("https://res.cloudinary.com/demo/image/upload/sample.pdf")
                .fileName("project_specs.pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(1048576L) // 1 MB
                .build();
        attachment.setUserId(testUser.getId());

        attachment = attachmentRepository.saveAndFlush(attachment);

        // Fetch by entity type + entity ID
        List<AttachmentEntity> attachments = attachmentRepository.findAllByUserIdAndEntityTypeAndEntityIdAndDeletedAtIsNull(
                testUser.getId(), AttachmentEntityType.TASK, linkedTaskId
        );
        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getFileName()).isEqualTo("project_specs.pdf");
        assertThat(attachments.get(0).getFileSizeBytes()).isEqualTo(1048576L);
        assertThat(attachments.get(0).getMimeType()).isEqualTo("application/pdf");

        // Fetch by storage key
        Optional<AttachmentEntity> byKey = attachmentRepository.findByStorageKeyAndDeletedAtIsNull("livo_attachments/sample_key_123");
        assertThat(byKey).isPresent();
        assertThat(byKey.get().getId()).isEqualTo(attachment.getId());
    }
}
