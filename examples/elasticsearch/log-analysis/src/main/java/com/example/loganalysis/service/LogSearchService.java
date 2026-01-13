package com.example.loganalysis.service;

import com.example.loganalysis.domain.LogEntry;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LogSearchService {

    private final ElasticsearchOperations operations;

    public LogSearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public List<LogEntry> searchErrors(String query, LocalDateTime from, LocalDateTime to,
                                       int page, int size) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 에러 레벨만
        boolQuery.filter(Query.of(q -> q
            .terms(t -> t
                .field("level")
                .terms(v -> v.value(List.of(
                    co.elastic.clients.elasticsearch._types.FieldValue.of("ERROR"),
                    co.elastic.clients.elasticsearch._types.FieldValue.of("WARN")
                )))
            )
        ));

        // 시간 범위
        boolQuery.filter(Query.of(q -> q
            .range(r -> r
                .field("@timestamp")
                .gte(JsonData.of(from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .lt(JsonData.of(to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            )
        ));

        // 메시지 검색
        if (StringUtils.hasText(query)) {
            boolQuery.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(query)
                    .fields("message", "exception", "stackTrace")
                )
            ));
        }

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withSort(Sort.by("@timestamp").descending())
            .withPageable(PageRequest.of(page, size))
            .build();

        SearchHits<LogEntry> hits = operations.search(
            nativeQuery,
            LogEntry.class,
            IndexCoordinates.of("logs-*")
        );

        return hits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }

    public List<LogEntry> traceRequest(String requestId) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .term(t -> t.field("requestId").value(requestId))
            ))
            .withSort(Sort.by("@timestamp").ascending())
            .withPageable(PageRequest.of(0, 1000))
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query,
            LogEntry.class,
            IndexCoordinates.of("logs-*")
        );

        return hits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }

    public List<LogEntry> getUserLogs(String userId, LocalDateTime from, LocalDateTime to) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder()
            .filter(Query.of(q -> q.term(t -> t.field("userId").value(userId))))
            .filter(Query.of(q -> q
                .range(r -> r
                    .field("@timestamp")
                    .gte(JsonData.of(from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .lt(JsonData.of(to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                )
            ));

        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withSort(Sort.by("@timestamp").descending())
            .withPageable(PageRequest.of(0, 100))
            .build();

        return operations.search(query, LogEntry.class, IndexCoordinates.of("logs-*"))
            .getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }
}
