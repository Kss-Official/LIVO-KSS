package com.livo.api.modules.finance.dto;

import com.livo.api.modules.finance.entity.TransactionEntity;
import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private UUID tripId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private String title;
    private String description;
    private String category;
    private PaymentMethod paymentMethod;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static TransactionResponse fromEntity(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        return TransactionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .goalId(entity.getGoalId())
                .tripId(entity.getTripId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .paymentMethod(entity.getPaymentMethod())
                .transactionDate(entity.getTransactionDate())
                .transactionTime(entity.getTransactionTime())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
