package com.example.productsearch.controller;

import com.example.productsearch.dto.SearchRequest;
import com.example.productsearch.dto.SearchResult;
import com.example.productsearch.service.ProductSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductSearchService searchService;

    public ProductController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "true") boolean inStockOnly,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SearchRequest request = SearchRequest.builder()
            .keyword(keyword)
            .category(category)
            .brands(brands)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .inStockOnly(inStockOnly)
            .sortBy(sortBy)
            .page(page)
            .size(size)
            .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String q) {
        return ResponseEntity.ok(searchService.autocomplete(q));
    }
}
