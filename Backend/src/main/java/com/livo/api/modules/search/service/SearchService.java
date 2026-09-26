package com.livo.api.modules.search.service;

import com.livo.api.modules.search.dto.SearchDomain;
import com.livo.api.modules.search.dto.SearchResponse;

import java.util.UUID;

public interface SearchService {

    /**
     * Executes cross-module search with relevance ranking, snippet extraction,
     * and strict multi-tenant tenant isolation.
     *
     * @param userId Current user UUID
     * @param query Search query text
     * @param domain Domain filter (optional, default ALL)
     * @param limit Max items to return (optional, default 20)
     * @return SearchResponse with ranked results
     */
    SearchResponse search(UUID userId, String query, SearchDomain domain, Integer limit);
}
