package com.example.productsearch.dto;

import java.util.List;

public class SearchRequest {

    private String keyword;
    private String category;
    private List<String> brands;
    private Integer minPrice;
    private Integer maxPrice;
    private boolean inStockOnly = true;
    private String sortBy;
    private int page = 0;
    private int size = 10;

    public SearchRequest() {}

    // Getters
    public String getKeyword() { return keyword; }
    public String getCategory() { return category; }
    public List<String> getBrands() { return brands; }
    public Integer getMinPrice() { return minPrice; }
    public Integer getMaxPrice() { return maxPrice; }
    public boolean isInStockOnly() { return inStockOnly; }
    public String getSortBy() { return sortBy; }
    public int getPage() { return page; }
    public int getSize() { return size; }

    // Setters
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public void setCategory(String category) { this.category = category; }
    public void setBrands(List<String> brands) { this.brands = brands; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }
    public void setInStockOnly(boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public void setPage(int page) { this.page = page; }
    public void setSize(int size) { this.size = size; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SearchRequest request = new SearchRequest();

        public Builder keyword(String keyword) { request.setKeyword(keyword); return this; }
        public Builder category(String category) { request.setCategory(category); return this; }
        public Builder brands(List<String> brands) { request.setBrands(brands); return this; }
        public Builder minPrice(Integer minPrice) { request.setMinPrice(minPrice); return this; }
        public Builder maxPrice(Integer maxPrice) { request.setMaxPrice(maxPrice); return this; }
        public Builder inStockOnly(boolean inStockOnly) { request.setInStockOnly(inStockOnly); return this; }
        public Builder sortBy(String sortBy) { request.setSortBy(sortBy); return this; }
        public Builder page(int page) { request.setPage(page); return this; }
        public Builder size(int size) { request.setSize(size); return this; }
        public SearchRequest build() { return request; }
    }
}
