package com.example.productsearch.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;
import java.util.List;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/settings.json")
public class Product {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Integer)
    private Integer price;

    @Field(type = FieldType.Integer)
    private Integer discountPrice;

    @Field(type = FieldType.Float)
    private Float rating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Boolean)
    private Boolean inStock;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date)
    private LocalDate createdAt;

    public Product() {}

    public Product(String id, String name, String description, String category, String brand,
                   Integer price, Integer discountPrice, Float rating, Integer reviewCount,
                   Boolean inStock, List<String> tags, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.discountPrice = discountPrice;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.inStock = inStock;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
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
