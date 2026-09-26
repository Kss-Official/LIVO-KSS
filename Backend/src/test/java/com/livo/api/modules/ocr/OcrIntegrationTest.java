package com.livo.api.modules.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.modules.auth.dto.UserSyncRequest;
import com.livo.api.modules.auth.service.AuthService;
import com.livo.api.modules.universaladd.dto.UniversalAddConfirmRequest;
import com.livo.api.modules.universaladd.dto.UniversalAddDomain;
import com.livo.api.modules.universaladd.dto.UniversalAddResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OcrIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private UUID userId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        String uid = "ocr_test_uid_" + UUID.randomUUID();
        String email = "ocr_test_" + UUID.randomUUID() + "@livo.test";

        var authResponse = authService.syncUser(UserSyncRequest.builder()
                .firebaseUid(uid)
                .email(email)
                .fullName("OCR Test User")
                .build());

        this.userId = authResponse.getUser().getId();
        this.jwtToken = authResponse.getAccessToken();
    }

    private byte[] createSamplePdf(String merchant, String totalAmountStr, String dateStr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("STORE: " + merchant);
                cs.newLineAtOffset(0, -20);
                cs.showText("Date: " + dateStr);
                cs.newLineAtOffset(0, -20);
                cs.showText("Total: " + totalAmountStr);
                cs.endText();
            }
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Should scan PDF invoice via /api/v1/ocr/scan and extract structured metadata")
    void testDirectOcrScanPdf() throws Exception {
        byte[] pdfBytes = createSamplePdf("Starbucks Coffee Koramangala", "Rs 450.00", "2026-09-24");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                pdfBytes
        );

        mockMvc.perform(multipart("/api/v1/ocr/scan")
                        .file(file)
                        .param("hint", "Coffee meeting")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchant").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(450.00))
                .andExpect(jsonPath("$.data.engine").value("PDFBOX_TEXT"));
    }

    @Test
    @DisplayName("Should scan PDF via /api/v1/universal-add/scan and confirm 1-tap expense creation")
    void testUniversalAddScanAndConfirm() throws Exception {
        byte[] pdfBytes = createSamplePdf("Apple Store Indiranagar", "Rs 12900.00", "2026-09-24");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "apple_receipt.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult scanResult = mockMvc.perform(multipart("/api/v1/universal-add/scan")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.detectedDomain").value("EXPENSE"))
                .andExpect(jsonPath("$.data.parsedDraft.amount").value(12900.00))
                .andReturn();

        String scanJson = scanResult.getResponse().getContentAsString();
        UniversalAddResponse response = objectMapper.readValue(
                objectMapper.readTree(scanJson).path("data").toString(),
                UniversalAddResponse.class
        );

        assertThat(response.getParsedDraft()).isNotNull();
        assertThat(response.getParsedDraft().getAmount()).isEqualByComparingTo(new BigDecimal("12900.00"));

        // Confirm the OCR parsed draft in 1 tap
        UniversalAddConfirmRequest confirmReq = UniversalAddConfirmRequest.builder()
                .draft(response.getParsedDraft())
                .build();

        mockMvc.perform(post("/api/v1/universal-add/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq))
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.domain").value("EXPENSE"))
                .andExpect(jsonPath("$.data.entityId").isNotEmpty());
    }

    @Test
    @DisplayName("Should reject unsupported file format with 400 Bad Request")
    void testRejectUnsupportedFormat() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                new byte[]{0x4D, 0x5A, 0x00, 0x00}
        );

        mockMvc.perform(multipart("/api/v1/ocr/scan")
                        .file(invalidFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported file type")));
    }

    @Test
    @DisplayName("Should reject empty file with 400 Bad Request")
    void testRejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/ocr/scan")
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot be empty")));
    }
}
