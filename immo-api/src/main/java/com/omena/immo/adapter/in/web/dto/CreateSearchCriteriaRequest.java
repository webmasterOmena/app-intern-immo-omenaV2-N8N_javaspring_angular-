package com.omena.immo.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateSearchCriteriaRequest(
        @NotBlank String name,
        @NotBlank String city,
        String postalCode,
        @Min(0) int radiusKm,
        @Positive BigDecimal maxPrice,
        @Positive BigDecimal minSurface,
        String propertyType,
        Boolean active
) {
}
