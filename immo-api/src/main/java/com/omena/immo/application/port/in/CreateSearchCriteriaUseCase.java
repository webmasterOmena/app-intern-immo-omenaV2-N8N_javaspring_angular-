package com.omena.immo.application.port.in;

import com.omena.immo.domain.search.SearchCriteria;

import java.math.BigDecimal;

public interface CreateSearchCriteriaUseCase {

    SearchCriteria create(Command command);

    record Command(
            String name,
            String city,
            String postalCode,
            int radiusKm,
            BigDecimal maxPrice,
            BigDecimal minSurface,
            String propertyType,
            boolean active
    ) {
    }
}
