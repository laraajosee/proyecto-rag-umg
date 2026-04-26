package com.rag.controller;

import com.rag.evaluation.MetricsService;
import com.rag.model.QueryRequest;
import com.rag.model.QueryResponse;
import com.rag.service.RAGService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QueryController {

    private final RAGService ragService;
    private final MetricsService metricsService;

    public QueryController(RAGService ragService, MetricsService metricsService) {
        this.ragService = ragService;
        this.metricsService = metricsService;
    }

    /** POST /api/query — Hace una consulta RAG y registra métricas automáticamente */
    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
        try {
            QueryResponse response = ragService.processQuery(request);

            int retrievedDocs = response.getSources() != null ? response.getSources().size() : 0;
            int relevantDocs  = response.isHasEvidence() ? retrievedDocs : 0;
            metricsService.recordMetrics(request.getQuery(), retrievedDocs, relevantDocs, response.getLatencyMs());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** GET /api/query/metrics — Devuelve promedios de precision, recall y latencia */
    @GetMapping("/query/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(metricsService.getAverageMetrics());
    }
}