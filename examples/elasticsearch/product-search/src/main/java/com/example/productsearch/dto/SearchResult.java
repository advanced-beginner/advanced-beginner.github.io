package com.example.productsearch.dto;

import com.example.productsearch.domain.Product;

import java.util.List;
import java.util.Map;

public class SearchResult {

    private List<ProductResponse> products;
    private long total;
    private Map<String, Map<String, Long>> facets;

    public SearchResult() {}

    public SearchResult(List<ProductResponse> products, long total, Map<String, Map<String, Long>> facets) {
        this.products = products;
        this.total = total;
        this.facets = facets;
    }

    // Getters
    public List<ProductResponse> getProducts() { return products; }
    public long getTotal() { return total; }
    public Map<String, Map<String, Long>> getFacets() { return facets; }

    // Setters
    public void setProducts(List<ProductResponse> products) { this.products = products; }
    public void setTotal(long total) { this.total = total; }
    public void setFacets(Map<String, Map<String, Long>> facets) { this.facets = facets; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SearchResult result = new SearchResult();

        public Builder products(List<ProductResponse> products) { result.setProducts(products); return this; }
        public Builder total(long total) { result.setTotal(total); return this; }
        public Builder facets(Map<String, Map<String, Long>> facets) { result.setFacets(facets); return this; }
        public SearchResult build() { return result; }
    }
}
