package com.livo.api.modules.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOnboardingRequest {

    @Min(value = 1, message = "Onboarding step must be at least 1")
    @Max(value = 10, message = "Onboarding step cannot exceed 10")
    private Short onboardingStepReached;

    private Boolean isOnboarded;

    private String termsAcceptedVersion;

    private Boolean acceptTerms;
}
