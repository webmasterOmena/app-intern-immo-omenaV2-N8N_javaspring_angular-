package com.omena.immo.application.port.out;

import com.omena.immo.domain.search.SearchCriteria;

import java.util.List;

public interface SearchCriteriaRepository {

    List<SearchCriteria> findAll();

    List<SearchCriteria> findActive();

    SearchCriteria save(SearchCriteria criteria);
}
