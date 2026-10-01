package com.omena.immo.application.port.in;

import com.omena.immo.domain.search.SearchCriteria;

import java.util.List;

public interface ListSearchCriteriaUseCase {

    List<SearchCriteria> listAll();

    List<SearchCriteria> listActive();
}
