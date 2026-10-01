package com.omena.immo.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_criteria")
class SearchCriteriaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "radius_km", nullable = false)
    private int radiusKm;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "min_surface", precision = 10, scale = 2)
    private BigDecimal minSurface;

    @Column(name = "property_type", length = 40)
    private String propertyType;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SearchCriteriaJpaEntity() {
    }

    SearchCriteriaJpaEntity(
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
        this.id = id;
        this.name = name;
        this.city = city;
        this.postalCode = postalCode;
        this.radiusKm = radiusKm;
        this.maxPrice = maxPrice;
        this.minSurface = minSurface;
        this.propertyType = propertyType;
        this.active = active;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    String getCity() { return city; }
    String getPostalCode() { return postalCode; }
    int getRadiusKm() { return radiusKm; }
    BigDecimal getMaxPrice() { return maxPrice; }
    BigDecimal getMinSurface() { return minSurface; }
    String getPropertyType() { return propertyType; }
    boolean isActive() { return active; }
    Instant getCreatedAt() { return createdAt; }
}
