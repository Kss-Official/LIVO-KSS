package com.livo.api.modules.tag.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.tag.dto.CreateTagRequest;
import com.livo.api.modules.tag.dto.TagResponse;
import com.livo.api.modules.tag.service.TagService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags(@CurrentUser UUID userId) {
        List<TagResponse> tags = tagService.getAllTags(userId);
        return ResponseEntity.ok(ApiResponse.success(tags, "Tags retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateTagRequest request
    ) {
        TagResponse tag = tagService.createTag(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tag, "Tag created successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        tagService.deleteTag(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tag deleted successfully"));
    }
}
