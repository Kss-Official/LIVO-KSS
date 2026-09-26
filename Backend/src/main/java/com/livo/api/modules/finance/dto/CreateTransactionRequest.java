package com.livo.api.modules.finance.dto;

import com.livo.api.modules.finance.entity.enums.PaymentMethod;
import com.livo.api.modules.finance.entity.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    private UUID goalId;
    private UUID tripId;

    @Builder.Default
    private TransactionType type = TransactionType.EXPENSE;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Builder.Default
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g. INR, USD)")
    private String currency = "INR";

    @NotBlank(message = "Transaction title cannot be blank")
    @Size(max = 150, message = "Transaction title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Category cannot be blank")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.UPI;

    private LocalDate transactionDate;
    private LocalTime transactionTime;
}
