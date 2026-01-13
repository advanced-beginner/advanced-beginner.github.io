package com.example.loganalysis.service;

import com.example.loganalysis.domain.LogEntry;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogAnalyticsService {

    private final ElasticsearchOperations operations;

    public LogAnalyticsService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public Map<String, Long> getErrorCountByHour(LocalDateTime from, LocalDateTime to) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .bool(b -> b
                    .filter(Query.of(f -> f.term(t -> t.field("level").value("ERROR"))))
                    .filter(Query.of(f -> f
                        .range(r -> r
                            .field("@timestamp")
                            .gte(JsonData.of(from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                            .lt(JsonData.of(to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                        )
                    ))
                )
            ))
            .withAggregation("errors_by_hour", Aggregation.of(a -> a
                .dateHistogram(dh -> dh
                    .field("@timestamp")
                    .calendarInterval(CalendarInterval.Hour)
                    .format("yyyy-MM-dd HH:00")
                )
            ))
            .withMaxResults(0)
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query, LogEntry.class, IndexCoordinates.of("logs-*"));

        Map<String, Long> result = new HashMap<>();
        if (hits.getAggregations() != null) {
            ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();
            var histogramAgg = aggs.get("errors_by_hour");
            if (histogramAgg != null) {
                for (DateHistogramBucket bucket : histogramAgg.aggregation().getAggregate().dateHistogram().buckets().array()) {
                    result.put(bucket.keyAsString(), bucket.docCount());
                }
            }
        }
        return result;
    }

    public ErrorRateResult getErrorRate(LocalDateTime from, LocalDateTime to) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .range(r -> r
                    .field("@timestamp")
                    .gte(JsonData.of(from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .lt(JsonData.of(to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                )
            ))
            .withAggregation("total", Aggregation.of(a -> a.valueCount(v -> v.field("level"))))
            .withAggregation("errors", Aggregation.of(a -> a
                .filter(f -> f.term(t -> t.field("level").value("ERROR")))
            ))
            .withMaxResults(0)
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query, LogEntry.class, IndexCoordinates.of("logs-*"));

        long total = hits.getTotalHits();
        long errors = 0;

        if (hits.getAggregations() != null) {
            ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();
            var errorsAgg = aggs.get("errors");
            if (errorsAgg != null) {
                errors = errorsAgg.aggregation().getAggregate().filter().docCount();
            }
        }

        double errorRate = total > 0 ? (double) errors / total * 100 : 0;
        return new ErrorRateResult(total, errors, errorRate);
    }

    public List<SlowRequest> getSlowRequests(int topN, long thresholdMs) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .range(r -> r.field("durationMs").gte(JsonData.of(thresholdMs)))
            ))
            .withSort(Sort.by("durationMs").descending())
            .withPageable(PageRequest.of(0, topN))
            .build();

        return operations.search(query, LogEntry.class, IndexCoordinates.of("logs-*"))
            .getSearchHits().stream()
            .map(hit -> new SlowRequest(
                hit.getContent().getHttpPath(),
                hit.getContent().getDurationMs(),
                hit.getContent().getTimestamp()
            ))
            .toList();
    }

    public record ErrorRateResult(long total, long errors, double errorRate) {}

    public record SlowRequest(String path, Long durationMs, java.time.Instant timestamp) {}
}
