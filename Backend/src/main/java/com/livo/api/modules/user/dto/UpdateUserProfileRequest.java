package com.livo.api.modules.user.dto;

import com.livo.api.modules.user.entity.enums.Theme;
import com.livo.api.modules.user.entity.enums.WeekStart;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters")
    private String fullName;

    @Size(max = 300, message = "Bio cannot exceed 300 characters")
    private String bio;

    private String avatarUrl;

    @Size(max = 50, message = "Timezone cannot exceed 50 characters")
    private String timezone;

    @Size(max = 10, message = "Language code cannot exceed 10 characters")
    private String language;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g. INR, USD, EUR)")
    private String currency;

    @Size(max = 20, message = "Date format cannot exceed 20 characters")
    private String dateFormat;

    private WeekStart weekStart;

    private Theme theme;
}
