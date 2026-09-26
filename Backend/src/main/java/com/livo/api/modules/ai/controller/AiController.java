package com.livo.api.modules.ai.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.modules.ai.dto.AiAnalysisResponse;
import com.livo.api.modules.ai.dto.AiAuditLogResponse;
import com.livo.api.modules.ai.dto.AiChatRequest;
import com.livo.api.modules.ai.dto.AiChatResponse;
import com.livo.api.modules.ai.dto.AiConversationResponse;
import com.livo.api.modules.ai.dto.AiFeedbackRequest;
import com.livo.api.modules.ai.dto.AiMessageResponse;
import com.livo.api.modules.ai.dto.AiProposalResponse;
import com.livo.api.modules.ai.dto.AiRecommendationResponse;
import com.livo.api.modules.ai.entity.enums.AiProposalStatus;
import com.livo.api.modules.ai.service.AiChatService;
import com.livo.api.modules.ai.service.AiProposalService;
import com.livo.api.modules.ai.service.AiRecommendationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "Endpoints for AI conversational assistant (Gemini 3.6 Flash), proactive recommendations, controlled action proposals, and audit logs")
public class AiController {

    private final AiChatService aiChatService;
    private final AiRecommendationService aiRecommendationService;
    private final AiProposalService aiProposalService;

    @PostMapping("/chat")
    @Operation(summary = "Send chat message to LIVO AI co-pilot with real-time context and proposal cards")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AiChatRequest request
    ) {
        AiChatResponse response = aiChatService.sendMessage(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "AI response generated successfully"));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get user's AI conversation threads")
    public ResponseEntity<ApiResponse<List<AiConversationResponse>>> getConversations(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<AiConversationResponse> response = aiChatService.getConversations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/conversations")
    @Operation(summary = "Create a new AI conversation thread")
    public ResponseEntity<ApiResponse<AiConversationResponse>> createConversation(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String title = (body != null) ? body.get("title") : null;
        AiConversationResponse response = aiChatService.createConversation(currentUser.getId(), title);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Conversation thread created successfully"));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Get message history for an AI conversation thread")
    public ResponseEntity<ApiResponse<List<AiMessageResponse>>> getConversationMessages(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        List<AiMessageResponse> response = aiChatService.getConversationMessages(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete an AI conversation thread")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        aiChatService.deleteConversation(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Conversation thread deleted successfully"));
    }

    @PostMapping("/analyze")
    @Operation(summary = "Perform comprehensive workload, productivity, and life-balance analysis")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> analyze(
            @CurrentUser UserPrincipal currentUser
    ) {
        AiAnalysisResponse response = aiChatService.analyzeUserState(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Analysis completed successfully"));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get active proactive AI recommendations feed")
    public ResponseEntity<ApiResponse<List<AiRecommendationResponse>>> getRecommendations(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<AiRecommendationResponse> response = aiRecommendationService.getActiveRecommendations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/recommendations/refresh")
    @Operation(summary = "Trigger dynamic refresh of heuristic and proactive recommendations")
    public ResponseEntity<ApiResponse<List<AiRecommendationResponse>>> refreshRecommendations(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<AiRecommendationResponse> response = aiRecommendationService.refreshRecommendations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendations refreshed successfully"));
    }

    @PostMapping("/recommendations/{id}/feedback")
    @Operation(summary = "Submit feedback on an AI recommendation (ACCEPTED, DISMISSED, SNOOZED)")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> submitFeedback(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody AiFeedbackRequest request
    ) {
        AiRecommendationResponse response = aiRecommendationService.recordFeedback(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendation feedback recorded"));
    }

    @GetMapping("/proposals")
    @Operation(summary = "Get actionable AI proposals awaiting review or actioned")
    public ResponseEntity<ApiResponse<List<AiProposalResponse>>> getProposals(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) AiProposalStatus status
    ) {
        List<AiProposalResponse> response = aiProposalService.getProposals(currentUser.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/proposals/{id}/confirm")
    @Operation(summary = "Confirm and execute an AI proposed controlled action")
    public ResponseEntity<ApiResponse<AiProposalResponse>> confirmProposal(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        AiProposalResponse response = aiProposalService.confirmProposal(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "AI proposal confirmed and executed"));
    }

    @PostMapping("/proposals/{id}/revert")
    @Operation(summary = "Revert and undo a previously confirmed AI proposal action")
    public ResponseEntity<ApiResponse<AiProposalResponse>> revertProposal(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        AiProposalResponse response = aiProposalService.revertProposal(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "AI proposal action reverted"));
    }

    @PostMapping("/proposals/{id}/reject")
    @Operation(summary = "Reject an AI proposal")
    public ResponseEntity<ApiResponse<AiProposalResponse>> rejectProposal(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        AiProposalResponse response = aiProposalService.rejectProposal(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "AI proposal rejected"));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get immutable audit trail of all AI-initiated mutations")
    public ResponseEntity<ApiResponse<List<AiAuditLogResponse>>> getAuditLogs(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        List<AiAuditLogResponse> response = aiProposalService.getAuditLogs(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
