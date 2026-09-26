package com.livo.api.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeRecommendationDto {
    private UUID id;
    private String type;
    private String title;
    private String reason;
    private String actionType;
    private UUID relatedEntityId;
}
