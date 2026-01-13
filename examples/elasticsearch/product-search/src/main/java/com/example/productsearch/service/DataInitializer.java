package com.example.productsearch.service;

import com.example.productsearch.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final ElasticsearchOperations operations;

    public DataInitializer(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        IndexOperations indexOps = operations.indexOps(Product.class);

        if (!indexOps.exists()) {
            log.info("인덱스 생성 및 샘플 데이터 추가 중...");
            indexOps.createWithMapping();
            insertSampleData();
            log.info("샘플 데이터 추가 완료");
        } else {
            log.info("인덱스가 이미 존재합니다");
        }
    }

    private void insertSampleData() {
        List<Product> products = List.of(
            new Product("1", "맥북 프로 14인치 M3 Pro",
                "Apple M3 Pro 칩, 18GB 통합 메모리, 512GB SSD",
                "노트북", "Apple", 2390000, 2290000, 4.8f, 1250,
                true, List.of("프리미엄", "신상품"), LocalDate.of(2024, 1, 10)),

            new Product("2", "맥북 에어 13인치 M3",
                "Apple M3 칩, 8GB 통합 메모리, 256GB SSD, 미드나이트",
                "노트북", "Apple", 1390000, null, 4.7f, 890,
                true, List.of("베스트셀러"), LocalDate.of(2024, 1, 15)),

            new Product("3", "갤럭시북4 프로 16인치",
                "인텔 코어 울트라 7, 16GB RAM, 512GB SSD",
                "노트북", "Samsung", 1890000, null, 4.5f, 456,
                true, List.of("신상품"), LocalDate.of(2024, 1, 20)),

            new Product("4", "아이패드 프로 11인치 M4",
                "Apple M4 칩, 256GB, 스페이스 블랙",
                "태블릿", "Apple", 1499000, null, 4.9f, 2100,
                true, List.of("프리미엄", "신상품"), LocalDate.of(2024, 1, 5)),

            new Product("5", "갤럭시 탭 S9 Ultra",
                "스냅드래곤 8 Gen 2, 12GB RAM, 256GB",
                "태블릿", "Samsung", 1599000, null, 4.6f, 780,
                false, List.of("대화면"), LocalDate.of(2024, 1, 8)),

            new Product("6", "LG 그램 17인치",
                "인텔 13세대 코어 i7, 16GB RAM, 512GB SSD, 초경량",
                "노트북", "LG", 1790000, 1690000, 4.4f, 320,
                true, List.of("초경량"), LocalDate.of(2024, 1, 12)),

            new Product("7", "삼성전자 갤럭시 S24 Ultra",
                "스냅드래곤 8 Gen 3, 12GB RAM, 256GB, AI 기능",
                "스마트폰", "Samsung", 1698000, null, 4.7f, 3200,
                true, List.of("신상품", "AI"), LocalDate.of(2024, 1, 25))
        );

        operations.save(products);
    }
}
