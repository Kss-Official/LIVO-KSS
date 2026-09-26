package com.livo.api.modules.ocr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structured result of OCR document / receipt extraction")
public class OcrScanResult {

    @Schema(description = "Full transcribed text extracted from document")
    private String rawText;

    @Schema(description = "Detected document type (RECEIPT, INVOICE, TICKET, TASK_LIST, DOCUMENT)")
    private String documentType;

    @Schema(description = "Suggested headline or title")
    private String title;

    @Schema(description = "Merchant, vendor, or issuing party")
    private String merchant;

    @Schema(description = "Total extracted monetary amount")
    private BigDecimal totalAmount;

    @Schema(description = "Extracted tax amount if identifiable")
    private BigDecimal taxAmount;

    @Builder.Default
    @Schema(description = "Currency code (e.g. INR, USD, EUR)")
    private String currency = "INR";

    @Schema(description = "Extracted document or transaction date")
    private LocalDate date;

    @Schema(description = "Extracted document or transaction time")
    private LocalTime time;

    @Schema(description = "Inferred category (e.g. FOOD, SHOPPING, TRAVEL, UTILITIES, HEALTH)")
    private String category;

    @Builder.Default
    @Schema(description = "Itemized breakdown of products or services")
    private List<OcrLineItem> lineItems = new ArrayList<>();

    @Schema(description = "OCR and semantic extraction confidence score (0.0 to 1.0)")
    private double confidenceScore;

    @Schema(description = "Engine used for extraction: PDFBOX_TEXT, GEMINI_VISION, or FALLBACK_HEURISTIC")
    private String engine;
}
