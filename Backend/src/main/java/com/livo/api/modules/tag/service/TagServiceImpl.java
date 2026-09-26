package com.livo.api.modules.tag.service;

import com.livo.api.common.exception.ConflictException;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.modules.tag.dto.CreateTagRequest;
import com.livo.api.modules.tag.dto.TagResponse;
import com.livo.api.modules.tag.entity.TagEntity;
import com.livo.api.modules.tag.repository.TagRepository;
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
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags(UUID userId) {
        return tagRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(userId).stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagResponse createTag(UUID userId, CreateTagRequest request) {
        String name = request.getName().trim();
        if (tagRepository.existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(userId, name)) {
            throw new ConflictException("Tag with name '" + name + "' already exists");
        }

        TagEntity tag = TagEntity.builder()
                .name(name)
                .colorHex(request.getColorHex() != null ? request.getColorHex().toUpperCase() : "#6B7280")
                .build();
        tag.setUserId(userId);
        tag.setVersion(1L);

        return TagResponse.fromEntity(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public void deleteTag(UUID userId, UUID tagId) {
        TagEntity tag = tagRepository.findByIdAndUserIdAndDeletedAtIsNull(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));

        tag.markDeleted();
        tagRepository.save(tag);
    }
}
