package com.livo.api.modules.ocr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.common.exception.BadRequestException;
import com.livo.api.modules.ai.client.LivoAiClient;
import com.livo.api.modules.ocr.dto.OcrLineItem;
import com.livo.api.modules.ocr.dto.OcrScanResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

    private final LivoAiClient livoAiClient;
    private final ObjectMapper objectMapper;

    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private static final String OCR_PROMPT = """
            You are an expert OCR, receipt, invoice, and document analysis engine for LIVO Life OS.
            Analyze the document or image and extract all structured data.
            Output ONLY valid JSON with exactly the following fields:
            {
              "rawText": "Full text transcribed from the document",
              "documentType": "RECEIPT", "INVOICE", "TICKET", "TASK_LIST", or "DOCUMENT",
              "title": "Short descriptive title (e.g. Starbucks Bengaluru, Indigo Flight 6E-204, Amazon Invoice)",
              "merchant": "Store, merchant, airline, or issuing entity name",
              "totalAmount": 1250.00,
              "taxAmount": 62.50,
              "currency": "INR" (or detected 3-letter currency code),
              "date": "YYYY-MM-DD" (or null if not found),
              "time": "HH:mm" (or null if not found),
              "category": "FOOD", "SHOPPING", "TRAVEL", "UTILITIES", "HEALTH", "ENTERTAINMENT", or "OTHER",
              "lineItems": [
                { "name": "Item Name", "quantity": 1, "price": 100.00 }
              ],
              "confidence": 0.95
            }
            """;

    @Override
    public OcrScanResult scanFile(MultipartFile file, String hint) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds 15MB limit: " + file.getSize() + " bytes");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file: " + e.getMessage());
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String detectedMime = detectMimeType(bytes);

        if (detectedMime == null || !ALLOWED_MIME_TYPES.contains(detectedMime)) {
            throw new BadRequestException("Unsupported file type. Supported formats are: JPEG, PNG, WEBP, and PDF.");
        }

        return scanBytes(bytes, originalFilename, detectedMime, hint);
    }

    @Override
    public OcrScanResult scanBytes(byte[] fileBytes, String originalFilename, String mimeType, String hint) {
        log.info("Processing OCR scan for file '{}', mimeType='{}', size={} bytes", originalFilename, mimeType, fileBytes.length);

        if ("application/pdf".equalsIgnoreCase(mimeType)) {
            return processPdf(fileBytes, originalFilename, hint);
        } else {
            return processImage(fileBytes, originalFilename, mimeType, hint);
        }
    }

    private OcrScanResult processPdf(byte[] fileBytes, String originalFilename, String hint) {
        String pdfText = extractTextWithPdfBox(fileBytes);

        if (pdfText != null && pdfText.trim().length() > 20) {
            log.info("Extracted {} characters from PDF via Apache PDFBox", pdfText.trim().length());
            String aiResponse = livoAiClient.analyzeTextDocument(pdfText, OCR_PROMPT + (hint != null ? "\nHint: " + hint : ""));
            OcrScanResult result = parseAiJsonResponse(aiResponse, pdfText, "PDFBOX_TEXT", originalFilename);
            if (result != null) {
                return result;
            }

            // Fallback: Deterministic regex parsing of PDF text
            return parseTextWithHeuristics(pdfText, "PDFBOX_TEXT", originalFilename);
        }

        // If PDF contains no text (scanned image inside PDF), route to Gemini Multimodal Vision
        log.info("PDF has no extractable text layer. Routing to Gemini Multimodal Vision...");
        String visionResponse = livoAiClient.analyzeDocumentMultimodal(fileBytes, "application/pdf", OCR_PROMPT);
        OcrScanResult result = parseAiJsonResponse(visionResponse, "", "GEMINI_VISION", originalFilename);
        if (result != null) {
            return result;
        }

        return fallbackEmptyResult(originalFilename, "PDFBOX_TEXT");
    }

    private OcrScanResult processImage(byte[] fileBytes, String originalFilename, String mimeType, String hint) {
        log.info("Routing image to Gemini Multimodal Vision for OCR extraction...");
        String prompt = OCR_PROMPT + (hint != null ? "\nHint: " + hint : "");
        String visionResponse = livoAiClient.analyzeDocumentMultimodal(fileBytes, mimeType, prompt);

        OcrScanResult result = parseAiJsonResponse(visionResponse, "", "GEMINI_VISION", originalFilename);
        if (result != null) {
            return result;
        }

        log.warn("Gemini vision analysis unavailable. Returning heuristic fallback for image '{}'", originalFilename);
        return fallbackEmptyResult(originalFilename, "FALLBACK_HEURISTIC");
    }

    private String extractTextWithPdfBox(byte[] fileBytes) {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.warn("Apache PDFBox failed to extract text from PDF: {}", e.getMessage());
            return null;
        }
    }

    private OcrScanResult parseAiJsonResponse(String jsonString, String defaultRawText, String engine, String fallbackTitle) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }

        try {
            String clean = jsonString.trim();
            if (clean.startsWith("```json")) {
                clean = clean.substring(7);
            } else if (clean.startsWith("```")) {
                clean = clean.substring(3);
            }
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length() - 3);
            }
            clean = clean.trim();

            int firstBrace = clean.indexOf('{');
            int lastBrace = clean.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                clean = clean.substring(firstBrace, lastBrace + 1);
            } else {
                log.debug("No JSON structure detected in AI response. Falling back to heuristic text extractor.");
                return null;
            }

            JsonNode root = objectMapper.readTree(clean);

            String rawText = root.path("rawText").asText(defaultRawText);
            String documentType = root.path("documentType").asText("RECEIPT");
            String title = root.path("title").asText(fallbackTitle != null ? fallbackTitle : "Scanned Document");
            String merchant = root.path("merchant").asText(null);

            BigDecimal totalAmount = null;
            if (root.hasNonNull("totalAmount")) {
                totalAmount = new BigDecimal(root.path("totalAmount").asText());
            }

            BigDecimal taxAmount = null;
            if (root.hasNonNull("taxAmount")) {
                taxAmount = new BigDecimal(root.path("taxAmount").asText());
            }

            String currency = root.path("currency").asText("INR");
            String category = root.path("category").asText("FOOD");
            double confidence = root.path("confidence").asDouble(0.90);

            LocalDate date = null;
            if (root.hasNonNull("date")) {
                try {
                    date = LocalDate.parse(root.path("date").asText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception ignored) {
                }
            }

            LocalTime time = null;
            if (root.hasNonNull("time")) {
                try {
                    time = LocalTime.parse(root.path("time").asText().trim());
                } catch (Exception ignored) {
                }
            }

            List<OcrLineItem> lineItems = new ArrayList<>();
            JsonNode itemsNode = root.path("lineItems");
            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    String name = item.path("name").asText(null);
                    if (name != null && !name.isBlank()) {
                        Integer qty = item.hasNonNull("quantity") ? item.path("quantity").asInt(1) : 1;
                        BigDecimal price = item.hasNonNull("price") ? new BigDecimal(item.path("price").asText()) : null;
                        lineItems.add(new OcrLineItem(name, qty, price));
                    }
                }
            }

            return OcrScanResult.builder()
                    .rawText(rawText)
                    .documentType(documentType)
                    .title(title)
                    .merchant(merchant)
                    .totalAmount(totalAmount)
                    .taxAmount(taxAmount)
                    .currency(currency)
                    .date(date != null ? date : LocalDate.now())
                    .time(time)
                    .category(category)
                    .lineItems(lineItems)
                    .confidenceScore(confidence)
                    .engine(engine)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse OCR AI JSON response: {}", e.getMessage());
            return null;
        }
    }

    private OcrScanResult parseTextWithHeuristics(String text, String engine, String filename) {
        BigDecimal amount = null;
        Pattern amountPattern = Pattern.compile("(?i)(?:total|amount|rs\\.?|inr|\\$|€|₹)\\s*:?\\s*([\\d,]+\\.?\\d{0,2})");
        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            try {
                String cleanAmount = amountMatcher.group(1).replace(",", "");
                amount = new BigDecimal(cleanAmount);
            } catch (Exception ignored) {
            }
        }

        LocalDate date = null;
        Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}|\\d{1,2}/\\d{1,2}/\\d{2,4})");
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            try {
                String rawDate = dateMatcher.group(1);
                if (rawDate.contains("-")) {
                    date = LocalDate.parse(rawDate);
                }
            } catch (Exception ignored) {
            }
        }

        String[] lines = text.split("\\r?\\n");
        String merchant = lines.length > 0 && !lines[0].isBlank() ? lines[0].trim() : "Scanned Merchant";

        return OcrScanResult.builder()
                .rawText(text)
                .documentType("RECEIPT")
                .title(merchant)
                .merchant(merchant)
                .totalAmount(amount != null ? amount : new BigDecimal("0.00"))
                .currency("INR")
                .date(date != null ? date : LocalDate.now())
                .category("FOOD")
                .confidenceScore(0.75)
                .engine(engine)
                .build();
    }

    private OcrScanResult fallbackEmptyResult(String filename, String engine) {
        return OcrScanResult.builder()
                .rawText("Scanned document: " + filename)
                .documentType("DOCUMENT")
                .title("Scanned " + filename)
                .totalAmount(BigDecimal.ZERO)
                .currency("INR")
                .date(LocalDate.now())
                .category("OTHER")
                .confidenceScore(0.50)
                .engine(engine)
                .build();
    }

    private String detectMimeType(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return null;
        }

        // PDF: %PDF
        if (bytes.length >= 4 && bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) {
            return "application/pdf";
        }

        // JPEG: FF D8 FF
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // PNG: 89 50 4E 47
        if (bytes.length >= 4 && (bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50
                && (bytes[2] & 0xFF) == 0x4E && (bytes[3] & 0xFF) == 0x47) {
            return "image/png";
        }

        // WEBP: RIFF....WEBP
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }

        return null;
    }
}
