package com.omena.immo.adapter.in.web;

import com.omena.immo.adapter.in.web.dto.CreateSearchCriteriaRequest;
import com.omena.immo.application.port.in.CreateSearchCriteriaUseCase;
import com.omena.immo.application.port.in.ListSearchCriteriaUseCase;
import com.omena.immo.domain.search.SearchCriteria;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search-criteria")
public class SearchCriteriaController {

    private final ListSearchCriteriaUseCase listUseCase;
    private final CreateSearchCriteriaUseCase createUseCase;

    public SearchCriteriaController(
            ListSearchCriteriaUseCase listUseCase,
            CreateSearchCriteriaUseCase createUseCase
    ) {
        this.listUseCase = listUseCase;
        this.createUseCase = createUseCase;
    }

    @GetMapping
    public List<SearchCriteria> listAll() {
        return listUseCase.listAll();
    }

    @GetMapping("/active")
    public List<SearchCriteria> listActive() {
        return listUseCase.listActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SearchCriteria create(@Valid @RequestBody CreateSearchCriteriaRequest request) {
        boolean active = request.active() == null || request.active();

        return createUseCase.create(new CreateSearchCriteriaUseCase.Command(
                request.name(),
                request.city(),
                request.postalCode(),
                request.radiusKm(),
                request.maxPrice(),
                request.minSurface(),
                request.propertyType(),
                active
        ));
    }
}
