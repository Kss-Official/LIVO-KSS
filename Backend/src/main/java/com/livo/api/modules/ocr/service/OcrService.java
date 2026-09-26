package com.livo.api.modules.ocr.service;

import com.livo.api.modules.ocr.dto.OcrScanResult;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

    /**
     * Scans an uploaded receipt, invoice, bill, ticket, or document (Image or PDF)
     * using hybrid OCR (PDFBox for digital text, Gemini Vision for images/scans)
     * and returns structured extraction metadata.
     */
    OcrScanResult scanFile(MultipartFile file, String hint);

    /**
     * Scans raw file bytes with explicit MIME type.
     */
    OcrScanResult scanBytes(byte[] fileBytes, String originalFilename, String mimeType, String hint);
}
