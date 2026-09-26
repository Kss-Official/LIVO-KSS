package com.livo.api.modules.ocr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Line item extracted from receipt or invoice")
public class OcrLineItem {

    @Schema(description = "Item or service name")
    private String name;

    @Schema(description = "Quantity")
    private Integer quantity;

    @Schema(description = "Item price or total line cost")
    private BigDecimal price;
}
