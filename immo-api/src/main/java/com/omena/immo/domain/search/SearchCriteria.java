package com.omena.immo.domain.search;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SearchCriteria(
        UUID id,
        String name,
        String city,
        String postalCode,
        int radiusKm,
        BigDecimal maxPrice,
        BigDecimal minSurface,
        String propertyType,
        boolean active,
        Instant createdAt
) {
}
