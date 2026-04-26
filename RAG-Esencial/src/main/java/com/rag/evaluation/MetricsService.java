package com.rag.evaluation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MetricsService {

    private final JdbcTemplate jdbcTemplate;

    public MetricsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordMetrics(String query, int retrievedDocs, int relevantDocs, long latencyMs) {
        double precision = retrievedDocs > 0 ? (double) relevantDocs / retrievedDocs : 0.0;
        double recall = relevantDocs > 0 ? 1.0 : 0.0;

        String sql = """
            INSERT INTO evaluation_metrics (query, retrieved_docs, relevant_docs, precision, recall, latency_ms)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql, query, retrievedDocs, relevantDocs, precision, recall, latencyMs);
        System.out.println("Metrics recorded - Precision: " + precision +
                           ", Recall: " + recall +
                           ", Latency: " + latencyMs + "ms");
    }

    public Map<String, Object> getAverageMetrics() {
        String sql = """
            SELECT
                AVG(precision)   as avg_precision,
                AVG(recall)      as avg_recall,
                AVG(latency_ms)  as avg_latency,
                COUNT(*)         as total_queries
            FROM evaluation_metrics
            """;
        return jdbcTemplate.queryForMap(sql);
    }
}
