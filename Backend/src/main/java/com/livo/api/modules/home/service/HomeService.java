package com.livo.api.modules.home.service;

import com.livo.api.modules.home.dto.DailyQuoteDto;
import com.livo.api.modules.home.dto.HomeDashboardResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface HomeService {

    HomeDashboardResponse getHomeDashboard(UUID userId, LocalDate date);

    DailyQuoteDto getDailyQuote(LocalDate date);
}
