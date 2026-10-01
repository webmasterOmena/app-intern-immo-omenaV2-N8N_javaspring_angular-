package com.omena.immo.application.service;

import com.omena.immo.application.port.in.CreateSearchCriteriaUseCase;
import com.omena.immo.application.port.in.ListSearchCriteriaUseCase;
import com.omena.immo.application.port.out.SearchCriteriaRepository;
import com.omena.immo.domain.search.SearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SearchCriteriaService implements ListSearchCriteriaUseCase, CreateSearchCriteriaUseCase {

    private final SearchCriteriaRepository repository;

    public SearchCriteriaService(SearchCriteriaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchCriteria> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchCriteria> listActive() {
        return repository.findActive();
    }

    @Override
    public SearchCriteria create(Command command) {
        SearchCriteria criteria = new SearchCriteria(
                UUID.randomUUID(),
                command.name().trim(),
                command.city().trim(),
                normalize(command.postalCode()),
                command.radiusKm(),
                command.maxPrice(),
                command.minSurface(),
                normalize(command.propertyType()),
                command.active(),
                Instant.now()
        );

        return repository.save(criteria);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
