package com.livo.api.modules.ocr.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.ocr.dto.OcrScanResult;
import com.livo.api.modules.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR & Document Vision", description = "Endpoints for scanning receipts, bills, and document PDFs into structured data")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Scan Receipt, Invoice, or Document", description = "Extracts structured text, merchant, total amount, taxes, date, and line items from an uploaded image or PDF")
    public ResponseEntity<ApiResponse<OcrScanResult>> scanDocument(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "Receipt image (PNG/JPEG/WEBP) or PDF invoice", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional context hint (e.g. 'Coffee with client', 'Flight booking')", required = false)
            @RequestParam(value = "hint", required = false) String hint
    ) {
        OcrScanResult result = ocrService.scanFile(file, hint);
        return ResponseEntity.ok(ApiResponse.success(result, "Document scanned and extracted successfully"));
    }
}
