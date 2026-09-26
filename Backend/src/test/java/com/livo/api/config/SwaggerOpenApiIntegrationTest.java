package com.livo.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerOpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs should return OpenAPI 3 JSON with LIVO metadata and security scheme")
    void getApiDocs_returnsOpenApiJsonWithAllDomainsAndSecurityScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("LIVO — Personal Life Operating System API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/auth/sync")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/tasks")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/plan/daily")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/goals")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/habits")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/finance/transactions")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/learning/items")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/trips")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/health/entries")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/home")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/insights")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/search")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/universal-add/parse")));
    }

    @Test
    @DisplayName("GET /v3/api-docs/swagger-config should return 200 OK with Swagger configuration")
    void getSwaggerConfig_returnsSwaggerConfigJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }

    @Test
    @DisplayName("GET /swagger-ui.html should redirect to /swagger-ui/index.html")
    void getSwaggerUi_redirectsToPortal() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    @DisplayName("GET /swagger-ui/index.html should return HTML portal for interactive API exploration")
    void getSwaggerUiIndex_returnsHtml() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
