package com.omena.immo.adapter.out.persistence;

import com.omena.immo.application.port.out.SearchCriteriaRepository;
import com.omena.immo.domain.search.SearchCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchCriteriaPersistenceAdapter implements SearchCriteriaRepository {

    private final SpringDataSearchCriteriaRepository repository;

    public SearchCriteriaPersistenceAdapter(SpringDataSearchCriteriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SearchCriteria> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<SearchCriteria> findActive() {
        return repository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public SearchCriteria save(SearchCriteria criteria) {
        SearchCriteriaJpaEntity saved = repository.save(toEntity(criteria));
        return toDomain(saved);
    }

    private SearchCriteria toDomain(SearchCriteriaJpaEntity entity) {
        return new SearchCriteria(
                entity.getId(),
                entity.getName(),
                entity.getCity(),
                entity.getPostalCode(),
                entity.getRadiusKm(),
                entity.getMaxPrice(),
                entity.getMinSurface(),
                entity.getPropertyType(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private SearchCriteriaJpaEntity toEntity(SearchCriteria criteria) {
        return new SearchCriteriaJpaEntity(
                criteria.id(),
                criteria.name(),
                criteria.city(),
                criteria.postalCode(),
                criteria.radiusKm(),
                criteria.maxPrice(),
                criteria.minSurface(),
                criteria.propertyType(),
                criteria.active(),
                criteria.createdAt()
        );
    }
}
