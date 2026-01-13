package com.example.productsearch.service;

import com.example.productsearch.domain.Product;
import com.example.productsearch.dto.ProductResponse;
import com.example.productsearch.dto.SearchRequest;
import com.example.productsearch.dto.SearchResult;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;

    public ProductSearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public SearchResult search(SearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 검색어
        if (StringUtils.hasText(request.getKeyword())) {
            boolQuery.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(request.getKeyword())
                    .fields("name^3", "description")
                    .type(TextQueryType.BestFields)
                )
            ));
        }

        // 카테고리 필터
        if (StringUtils.hasText(request.getCategory())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("category").value(request.getCategory()))
            ));
        }

        // 브랜드 필터
        if (request.getBrands() != null && !request.getBrands().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .terms(t -> t
                    .field("brand")
                    .terms(v -> v.value(
                        request.getBrands().stream()
                            .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                            .toList()
                    ))
                )
            ));
        }

        // 가격 범위 필터
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> {
                    r.field("price");
                    if (request.getMinPrice() != null)
                        r.gte(JsonData.of(request.getMinPrice()));
                    if (request.getMaxPrice() != null)
                        r.lte(JsonData.of(request.getMaxPrice()));
                    return r;
                })
            ));
        }

        // 재고 필터
        if (request.isInStockOnly()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("inStock").value(true))
            ));
        }

        // 쿼리 빌드
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withPageable(PageRequest.of(request.getPage(), request.getSize()))
            .withSort(buildSort(request.getSortBy()))
            .withHighlightQuery(buildHighlight())
            .withAggregation("categories", Aggregation.of(a -> a
                .terms(t -> t.field("category").size(10))
            ))
            .withAggregation("brands", Aggregation.of(a -> a
                .terms(t -> t.field("brand").size(20))
            ))
            .build();

        SearchHits<Product> hits = operations.search(query, Product.class);

        return SearchResult.builder()
            .products(hits.getSearchHits().stream()
                .map(this::toProductResponse)
                .toList())
            .total(hits.getTotalHits())
            .facets(extractFacets(hits))
            .build();
    }

    public List<String> autocomplete(String prefix) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .matchPhrasePrefix(m -> m
                    .field("name")
                    .query(prefix)
                )
            ))
            .withPageable(PageRequest.of(0, 5))
            .build();

        SearchHits<Product> hits = operations.search(query, Product.class);

        return hits.getSearchHits().stream()
            .map(h -> h.getContent().getName())
            .distinct()
            .toList();
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(
                Sort.Order.desc("_score"),
                Sort.Order.desc("reviewCount")
            );
        }
        return switch (sortBy) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("rating").descending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("_score").descending();
        };
    }

    private HighlightQuery buildHighlight() {
        return new HighlightQuery(
            new Highlight(List.of(
                new HighlightField("name"),
                new HighlightField("description")
            )),
            Product.class
        );
    }

    private ProductResponse toProductResponse(SearchHit<Product> hit) {
        ProductResponse response = new ProductResponse(hit.getContent());
        if (hit.getHighlightFields().containsKey("name")) {
            response.setHighlightedName(
                String.join("", hit.getHighlightField("name"))
            );
        }
        return response;
    }

    private Map<String, Map<String, Long>> extractFacets(SearchHits<Product> hits) {
        Map<String, Map<String, Long>> facets = new HashMap<>();

        if (hits.getAggregations() != null) {
            ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();

            var categoriesAgg = aggs.get("categories");
            if (categoriesAgg != null) {
                Map<String, Long> categories = new HashMap<>();
                for (StringTermsBucket bucket : categoriesAgg.aggregation().getAggregate().sterms().buckets().array()) {
                    categories.put(bucket.key().stringValue(), bucket.docCount());
                }
                facets.put("categories", categories);
            }

            var brandsAgg = aggs.get("brands");
            if (brandsAgg != null) {
                Map<String, Long> brands = new HashMap<>();
                for (StringTermsBucket bucket : brandsAgg.aggregation().getAggregate().sterms().buckets().array()) {
                    brands.put(bucket.key().stringValue(), bucket.docCount());
                }
                facets.put("brands", brands);
            }
        }

        return facets;
    }
}
