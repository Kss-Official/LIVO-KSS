package com.livo.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    static {
        org.springdoc.core.utils.SpringDocUtils.getConfig()
                .addAnnotationsToIgnore(com.livo.api.common.security.CurrentUser.class)
                .addRequestWrapperToIgnore(com.livo.api.common.security.UserPrincipal.class);
    }

    @Bean
    public OpenAPI livoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LIVO — Personal Life Operating System API")
                        .description("High-performance production REST API for LIVO Personal Life OS. " +
                                "Features local-first deterministic engines (sweep-line schedule fitting, priority math, day overload evaluator), " +
                                "8-domain life management (tasks, plan, goals, habits, finance, learning, travel, health), " +
                                "asynchronous event bus, AI orchestrations (Gemini Flash & Groq Whisper), " +
                                "universal add quick capture, and delta offline synchronization.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LIVO Engineering Team")
                                .email("dev@livo.app")
                                .url("https://livo.app"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://livo.app/terms")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Bearer token returned from `/api/v1/auth/sync` or `/api/v1/auth/login`.")))
                .tags(List.of(
                        new Tag().name("Auth").description("Authentication, user sync, and token refresh"),
                        new Tag().name("User").description("User profile, preferences, life areas, and FCM device tokens"),
                        new Tag().name("Tasks").description("Core task management, subtasks, priorities, and AI subtask breakdown"),
                        new Tag().name("Plan").description("Daily timeline, schedule blocks, sweep-line conflict detection, and workload rebalance"),
                        new Tag().name("Goals").description("Goals, milestones, progress tracking, and AI milestone breakdown"),
                        new Tag().name("Habits").description("Habits, check-ins, streak recalculations, and consistency heatmap"),
                        new Tag().name("Finance").description("Income, expenses, payment methods, monthly budgets, and 80% threshold alerts"),
                        new Tag().name("Learning").description("Courses, books, skills, and study session logs"),
                        new Tag().name("Travel").description("Trips, itineraries, and day-by-day travel plans"),
                        new Tag().name("Health").description("Workouts, nutrition, sleep logs, and dynamic metrics"),
                        new Tag().name("Routines").description("Committed daily routines (morning, lunch, bedtime)"),
                        new Tag().name("Home").description("Unified daily dashboard feed (<25ms single roundtrip)"),
                        new Tag().name("Insights").description("Life analytics, peak productivity hours, and category distributions"),
                        new Tag().name("Attachments").description("Cloudinary CDN media uploads and metadata"),
                        new Tag().name("Notifications").description("System alerts, overdue reminders, and FCM push notifications"),
                        new Tag().name("Sync").description("Delta synchronization protocol and offline mutation queue"),
                        new Tag().name("Universal Add").description("Zero-latency NLP quick capture and 1-tap entity creation"),
                        new Tag().name("OCR & Document Vision").description("Receipt, invoice, and PDF document OCR extraction into structured drafts"),
                        new Tag().name("AI Chat").description("Conversational AI, SSE streaming chat, proposal confirm/revert, and Groq Whisper audio STT"),
                        new Tag().name("Search").description("Global trigram search engine across all 8 life areas")
                ));
    }
}
