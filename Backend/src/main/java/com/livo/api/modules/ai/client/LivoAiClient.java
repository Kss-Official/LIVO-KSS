package com.livo.api.modules.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LivoAiClient {

    private final ObjectMapper objectMapper;

    @Value("${livo.ai.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${livo.ai.gemini-model:gemini-3.6-flash}")
    private String geminiModel;

    @Value("${livo.ai.groq-api-key:}")
    private String groqApiKey;

    private static final String GEMINI_API_HOST = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public String generateChatResponse(String systemContext, String userPrompt, List<Map<String, String>> history) {
        String effectiveGeminiKey = getSanitizedKey(geminiApiKey);
        if (effectiveGeminiKey != null && !effectiveGeminiKey.isBlank()) {
            try {
                return callGeminiApi(systemContext, userPrompt, history, effectiveGeminiKey);
            } catch (Exception e) {
                log.warn("Gemini API call failed: {}, attempting Groq fallback...", e.getMessage());
            }
        } else {
            log.debug("Gemini API key not configured. Checking Groq fallback.");
        }

        String effectiveGroqKey = getSanitizedKey(groqApiKey);
        if (effectiveGroqKey != null && !effectiveGroqKey.isBlank()) {
            try {
                return callGroqApi(systemContext, userPrompt, history, effectiveGroqKey);
            } catch (Exception e) {
                log.warn("Groq API call failed: {}, falling back to local heuristic response generator", e.getMessage());
            }
        } else {
            log.debug("Groq API key not configured. Using local deterministic AI reasoning generator.");
        }

        return generateLocalResponse(userPrompt, systemContext);
    }

    private String getSanitizedKey(String rawKey) {
        if (rawKey == null) return null;
        String key = rawKey.trim();
        int hashIdx = key.indexOf('#');
        if (hashIdx >= 0) {
            key = key.substring(0, hashIdx).trim();
        }
        return key;
    }

    private String callGroqApi(String systemContext, String userPrompt, List<Map<String, String>> history, String effectiveKey) {
        RestClient restClient = RestClient.builder().build();

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemContext != null && !systemContext.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemContext));
        }

        if (history != null) {
            for (Map<String, String> msg : history) {
                String role = "user".equalsIgnoreCase(msg.get("role")) ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", msg.getOrDefault("content", "")));
            }
        }

        messages.add(Map.of("role", "user", "content", userPrompt));

        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-20b",
                "messages", messages,
                "temperature", 0.7
        );

        String responseString = restClient.post()
                .uri(GROQ_API_URL)
                .header("Authorization", "Bearer " + effectiveKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseString);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}", e.getMessage());
        }

        return generateLocalResponse(userPrompt, systemContext);
    }

    private String callGeminiApi(String systemContext, String userPrompt, List<Map<String, String>> history, String effectiveKey) {
        RestClient restClient = RestClient.builder().build();

        Map<String, Object> requestBody = new HashMap<>();

        if (systemContext != null && !systemContext.isBlank()) {
            Map<String, Object> sysInstruction = Map.of(
                    "parts", List.of(Map.of("text", systemContext))
            );
            requestBody.put("system_instruction", sysInstruction);
        }

        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            for (Map<String, String> msg : history) {
                String role = "user".equalsIgnoreCase(msg.get("role")) ? "user" : "model";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", msg.getOrDefault("content", "")))
                ));
            }
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userPrompt))
        ));

        requestBody.put("contents", contents);

        String modelName = (geminiModel != null && !geminiModel.isBlank()) ? geminiModel.trim() : "gemini-3.6-flash";
        String url = GEMINI_API_HOST + modelName + ":generateContent";

        String responseString = restClient.post()
                .uri(url)
                .header("x-goog-api-key", effectiveKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseString);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
        }

        return generateLocalResponse(userPrompt, systemContext);
    }

    public String generateLocalResponse(String userPrompt, String systemContext) {
        String lower = userPrompt.toLowerCase();

        if (lower.contains("plan") || lower.contains("focus") || lower.contains("today") || lower.contains("schedule")) {
            return "### 🎯 Your Focus Plan For Today\n\n" +
                    "Based on your current commitments and energy rhythms:\n\n" +
                    "1. **Primary Objective**: Tackle your top priority tasks during your high-focus morning window.\n" +
                    "2. **Habit Continuity**: Check in on your active daily habits before mid-day to maintain your streak.\n" +
                    "3. **Buffer Management**: Leave a 30-minute buffer before late afternoon meetings to avoid workload fatigue.\n\n" +
                    "*Need help shifting or rescheduling overloaded items? Ask me to rebalance your day!*";
        }

        if (lower.contains("goal") || lower.contains("milestone") || lower.contains("progress")) {
            return "### 🚀 Goal Tracking & Strategy\n\n" +
                    "You have active long-term goals in your Life OS. To accelerate progress:\n\n" +
                    "- Break down target milestones into actionable 45-minute daily tasks.\n" +
                    "- Align at least one task today directly towards your primary quarterly goal.\n\n" +
                    "Keep up the momentum!";
        }

        if (lower.contains("budget") || lower.contains("expense") || lower.contains("spend") || lower.contains("money")) {
            return "### 💳 Financial Awareness\n\n" +
                    "Your monthly budgets and recent transactions are tracked in your Life OS.\n\n" +
                    "- Log all cash and UPI expenses as soon as they occur.\n" +
                    "- Review categories approaching threshold limits to ensure your savings rate remains on track.";
        }

        return "### 💡 LIVO Assistant\n\n" +
                "I am your personal AI Life OS co-pilot. I analyze your structured tasks, habits, calendar, and goals to help you stay organized and productive.\n\n" +
                "- Ask **'What should I focus on today?'** for prioritized scheduling.\n" +
                "- Ask **'Analyze my workload'** to inspect overload and conflicts.\n" +
                "- Ask **'Suggest actions'** to generate automated proposals.";
    }

    /**
     * Analyzes image or PDF bytes directly using Gemini Multimodal Vision.
     */
    public String analyzeDocumentMultimodal(byte[] fileBytes, String mimeType, String prompt) {
        String effectiveGeminiKey = getSanitizedKey(geminiApiKey);
        if (effectiveGeminiKey != null && !effectiveGeminiKey.isBlank() && fileBytes != null && fileBytes.length > 0) {
            try {
                return callGeminiMultimodal(fileBytes, mimeType, prompt, effectiveGeminiKey);
            } catch (Exception e) {
                log.warn("Gemini multimodal analysis failed: {}, falling back...", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Analyzes text extracted from a document (e.g. from PDFBox) using Gemini or Groq fallback.
     */
    public String analyzeTextDocument(String rawText, String prompt) {
        String systemContext = "You are an expert document, receipt, and invoice analysis engine for LIVO Life OS. Extract structured information in JSON format.";
        String userPrompt = prompt + "\n\n--- DOCUMENT TEXT ---\n" + rawText;
        return generateChatResponse(systemContext, userPrompt, null);
    }

    private String callGeminiMultimodal(byte[] fileBytes, String mimeType, String prompt, String effectiveKey) {
        RestClient restClient = RestClient.builder().build();

        String base64Data = Base64.getEncoder().encodeToString(fileBytes);
        Map<String, Object> inlineData = Map.of(
                "mime_type", mimeType,
                "data", base64Data
        );

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> mediaPart = Map.of("inline_data", inlineData);

        Map<String, Object> content = Map.of("parts", List.of(textPart, mediaPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        String modelName = (geminiModel != null && !geminiModel.isBlank()) ? geminiModel.trim() : "gemini-3.6-flash";
        String url = GEMINI_API_HOST + modelName + ":generateContent";

        String responseString = restClient.post()
                .uri(url)
                .header("x-goog-api-key", effectiveKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseString);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini multimodal response: {}", e.getMessage());
        }

        return null;
    }
}

