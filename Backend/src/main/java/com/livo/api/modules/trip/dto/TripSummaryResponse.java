package com.livo.api.modules.trip.dto;

import com.livo.api.modules.finance.dto.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSummaryResponse {

    private TripResponse trip;
    private List<ItineraryItemResponse> itinerary;
    private BigDecimal totalExpenses;
    private long expenseCount;
    private List<TransactionResponse> recentTransactions;
    private long linkedTaskCount;
    private long linkedEventCount;
    private long tripDurationDays;
    private Long daysUntilTrip;
}
