package com.livo.api.modules.universaladd.service;

import com.livo.api.modules.universaladd.dto.UniversalAddConfirmRequest;
import com.livo.api.modules.universaladd.dto.UniversalAddConfirmResponse;
import com.livo.api.modules.universaladd.dto.UniversalAddRequest;
import com.livo.api.modules.universaladd.dto.UniversalAddResponse;

import java.util.UUID;

public interface UniversalAddService {

    /**
     * Parses unstructured natural language input into a structured entity draft
     * using zero-latency deterministic NLP matching (with optional AI fallback).
     */
    UniversalAddResponse parseInput(UUID userId, UniversalAddRequest request);

    /**
     * Confirms and persists the parsed draft into the respective domain module
     * with multi-tenant isolation.
     */
    UniversalAddConfirmResponse confirmAndCreate(UUID userId, UniversalAddConfirmRequest request);

    /**
     * Scans an uploaded receipt, invoice, or document (Image or PDF) via OCR
     * and produces a structured 1-tap confirmable draft.
     */
    UniversalAddResponse scanDocument(UUID userId, org.springframework.web.multipart.MultipartFile file, String hint);
}
