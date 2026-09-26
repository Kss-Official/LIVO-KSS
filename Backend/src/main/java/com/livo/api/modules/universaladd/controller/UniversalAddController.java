package com.livo.api.modules.universaladd.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.universaladd.dto.UniversalAddConfirmRequest;
import com.livo.api.modules.universaladd.dto.UniversalAddConfirmResponse;
import com.livo.api.modules.universaladd.dto.UniversalAddRequest;
import com.livo.api.modules.universaladd.dto.UniversalAddResponse;
import com.livo.api.modules.universaladd.service.UniversalAddService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/universal-add")
@RequiredArgsConstructor
@Tag(name = "Universal Add", description = "Zero-latency deterministic NLP capture engine with 1-tap confirmation across all 7 modules")
public class UniversalAddController {

    private final UniversalAddService universalAddService;

    @PostMapping("/parse")
    @Operation(summary = "Parse Natural Language Input", description = "Converts unstructured text or voice transcripts into structured entity drafts across 7 modules in <2ms")
    public ResponseEntity<ApiResponse<UniversalAddResponse>> parseInput(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UniversalAddRequest request
    ) {
        UniversalAddResponse response = universalAddService.parseInput(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Parse Natural Language Input (Alias)", description = "Convenience root alias for /parse")
    public ResponseEntity<ApiResponse<UniversalAddResponse>> parseInputAlias(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UniversalAddRequest request
    ) {
        UniversalAddResponse response = universalAddService.parseInput(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/confirm")
    @Operation(summary = "1-Tap Confirm & Persist Draft", description = "Persists confirmed or edited entity draft into the respective domain module with strict tenant isolation")
    public ResponseEntity<ApiResponse<UniversalAddConfirmResponse>> confirmAndCreate(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UniversalAddConfirmRequest request
    ) {
        UniversalAddConfirmResponse response = universalAddService.confirmAndCreate(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/scan", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Scan Document or Receipt (OCR)", description = "Upload a receipt photo, invoice PDF, or ticket to automatically generate a 1-tap confirmable draft")
    public ResponseEntity<ApiResponse<UniversalAddResponse>> scanDocument(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "hint", required = false) String hint
    ) {
        UniversalAddResponse response = universalAddService.scanDocument(currentUser.getId(), file, hint);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
