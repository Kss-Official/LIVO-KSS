package com.livo.api.modules.tag.service;

import com.livo.api.modules.tag.dto.CreateTagRequest;
import com.livo.api.modules.tag.dto.TagResponse;

import java.util.List;
import java.util.UUID;

public interface TagService {

    List<TagResponse> getAllTags(UUID userId);

    TagResponse createTag(UUID userId, CreateTagRequest request);

    void deleteTag(UUID userId, UUID tagId);
}
