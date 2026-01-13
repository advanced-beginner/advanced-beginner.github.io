package com.example.productsearch.dto;

import com.example.productsearch.domain.Product;

import java.time.LocalDate;
import java.util.List;

public class ProductResponse {

    private String id;
    private String name;
    private String highlightedName;
    private String description;
    private String category;
    private String brand;
    private Integer price;
    private Integer discountPrice;
    private Float rating;
    private Integer reviewCount;
    private Boolean inStock;
    private List<String> tags;
    private LocalDate createdAt;

    public ProductResponse() {}

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.brand = product.getBrand();
        this.price = product.getPrice();
        this.discountPrice = product.getDiscountPrice();
        this.rating = product.getRating();
        this.reviewCount = product.getReviewCount();
        this.inStock = product.getInStock();
        this.tags = product.getTags();
        this.createdAt = product.getCreatedAt();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getHighlightedName() { return highlightedName; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public Integer getPrice() { return price; }
    public Integer getDiscountPrice() { return discountPrice; }
    public Float getRating() { return rating; }
    public Integer getReviewCount() { return reviewCount; }
    public Boolean getInStock() { return inStock; }
    public List<String> getTags() { return tags; }
    public LocalDate getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHighlightedName(String highlightedName) { this.highlightedName = highlightedName; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setPrice(Integer price) { this.price = price; }
    public void setDiscountPrice(Integer discountPrice) { this.discountPrice = discountPrice; }
    public void setRating(Float rating) { this.rating = rating; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
