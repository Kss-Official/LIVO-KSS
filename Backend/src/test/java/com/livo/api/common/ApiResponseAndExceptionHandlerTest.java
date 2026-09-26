package com.livo.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livo.api.common.exception.AiServiceUnavailableException;
import com.livo.api.common.exception.BadRequestException;
import com.livo.api.common.exception.ConflictException;
import com.livo.api.common.exception.ForbiddenException;
import com.livo.api.common.exception.GlobalExceptionHandler;
import com.livo.api.common.exception.ResourceNotFoundException;
import com.livo.api.common.exception.ScheduleConflictException;
import com.livo.api.common.exception.UnauthorizedException;
import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiResponseAndExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class DummyDto {
        @NotBlank(message = "Title must not be blank")
        private String title;
    }

    @RestController
    @RequestMapping("/test/api")
    static class DummyTestController {

        @GetMapping("/success")
        public ApiResponse<String> getSuccess() {
            return ApiResponse.success("LIVO System OK", "Request succeeded");
        }

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Task", "id", "12345");
        }

        @GetMapping("/bad-request")
        public void throwBadRequest() {
            throw new BadRequestException("Invalid parameter value provided");
        }

        @GetMapping("/conflict")
        public void throwConflict() {
            throw new ConflictException("Category already exists");
        }

        @GetMapping("/schedule-conflict")
        public void throwScheduleConflict() {
            throw new ScheduleConflictException("Event overlaps with deep work routine");
        }

        @GetMapping("/unauthorized")
        public void throwUnauthorized() {
            throw new UnauthorizedException("Invalid or expired Firebase ID token");
        }

        @GetMapping("/forbidden")
        public void throwForbidden() {
            throw new ForbiddenException("User not authorized for this resource");
        }

        @GetMapping("/ai-unavailable")
        public void throwAiUnavailable() {
            throw new AiServiceUnavailableException("Gemini quota exceeded, fallback active");
        }

        @PostMapping("/validate")
        public ApiResponse<String> validateBody(@Valid @RequestBody DummyDto dto) {
            return ApiResponse.success(dto.getTitle());
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DummyTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Test 1: ApiResponse factory methods and PageResponse generation")
    void testApiResponseAndPageResponse() {
        ApiResponse<String> res = ApiResponse.success("Sample Data", "Loaded successfully");
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getData()).isEqualTo("Sample Data");
        assertThat(res.getMessage()).isEqualTo("Loaded successfully");
        assertThat(res.getTimestamp()).isNotNull();

        ApiResponse<Void> err = ApiResponse.error("Error occurred", List.of("Field 'email' is invalid"));
        assertThat(err.isSuccess()).isFalse();
        assertThat(err.getErrors()).hasSize(1);

        PageResponse<String> page = PageResponse.from(new PageImpl<>(List.of("Item1", "Item2"), PageRequest.of(0, 10), 2));
        assertThat(page.getContent()).containsExactly("Item1", "Item2");
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
        assertThat(page.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("Test 2: Success response envelope via MockMvc")
    void testSuccessEndpoint() throws Exception {
        mockMvc.perform(get("/test/api/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("LIVO System OK"))
                .andExpect(jsonPath("$.message").value("Request succeeded"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Test 3: ResourceNotFoundException maps to 404 with standard envelope")
    void testNotFoundException() throws Exception {
        mockMvc.perform(get("/test/api/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Task not found with id : '12345'"));
    }

    @Test
    @DisplayName("Test 4: BadRequestException and ValidationException map to 400")
    void testBadRequestAndValidation() throws Exception {
        mockMvc.perform(get("/test/api/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid parameter value provided"));

        // Validation error on empty title
        DummyDto invalid = new DummyDto("");
        mockMvc.perform(post("/test/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("title: Title must not be blank"));
    }

    @Test
    @DisplayName("Test 5: Conflict and ScheduleConflict map to 409")
    void testConflictExceptions() throws Exception {
        mockMvc.perform(get("/test/api/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Category already exists"));

        mockMvc.perform(get("/test/api/schedule-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Event overlaps with deep work routine"));
    }

    @Test
    @DisplayName("Test 6: Security and specialized domain exceptions (401, 403, 422, 503)")
    void testSpecializedExceptions() throws Exception {
        mockMvc.perform(get("/test/api/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired Firebase ID token"));

        mockMvc.perform(get("/test/api/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not authorized for this resource"));

        mockMvc.perform(get("/test/api/ai-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Gemini quota exceeded, fallback active"));
    }
}
