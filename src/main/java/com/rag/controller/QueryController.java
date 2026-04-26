package com.rag.controller;

import com.rag.evaluation.MetricsService;
import com.rag.model.QueryRequest;
import com.rag.model.QueryResponse;
import com.rag.service.RAGService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para manejar consultas al sistema RAG.
 * 
 * Este controlador expone endpoints HTTP para:
 * - Hacer preguntas al sistema RAG
 * - Recibir respuestas generadas con contexto de los documentos
 * - Registrar métricas de evaluación (precision, recall, latency)
 * 
 * @RestController: Indica que esta clase maneja peticiones HTTP y retorna JSON
 * @RequestMapping: Define la ruta base "/api/query" para todos los endpoints
 */
@RestController
@RequestMapping("/api/query")
public class QueryController {
    
    // Servicio que implementa la lógica RAG (Retrieval-Augmented Generation)
    // Spring lo inyecta automáticamente
    private final RAGService ragService;
    
    // Servicio para registrar métricas de evaluación
    private final MetricsService metricsService;
    
    /**
     * Constructor del controlador.
     * Spring Boot inyecta automáticamente el RAGService y MetricsService.
     * 
     * @param ragService Servicio que implementa RAG
     * @param metricsService Servicio para registrar métricas
     */
    public QueryController(RAGService ragService, MetricsService metricsService) {
        this.ragService = ragService;
        this.metricsService = metricsService;
    }
    
    /**
     * Endpoint para hacer consultas al sistema RAG.
     * 
     * Ruta completa: POST http://localhost:8080/api/query
     * 
     * Proceso:
     * 1. Recibe la pregunta del usuario
     * 2. Convierte la pregunta en un embedding (vector)
     * 3. Busca los chunks más similares en la base vectorial
     * 4. Construye un prompt con el contexto recuperado
     * 5. Envía el prompt a Ollama (Llama2) para generar la respuesta
     * 6. Retorna la respuesta junto con las fuentes utilizadas
     * 
     * @param request Objeto JSON con la pregunta y parámetros
     *                Ejemplo: {"query": "¿Qué es RAG?", "topK": 3}
     * 
     * @return ResponseEntity con la respuesta generada, fuentes, y métricas
     * 
     * Ejemplo de uso con curl:
     * curl -X POST http://localhost:8080/api/query \
     *   -H "Content-Type: application/json" \
     *   -d '{"query":"¿Qué es RAG?","topK":3}'
     * 
     * Ejemplo de uso con PowerShell:
     * $body = @{query = "¿Qué es RAG?"; topK = 3} | ConvertTo-Json
     * Invoke-RestMethod -Uri "http://localhost:8080/api/query" -Method Post -ContentType "application/json" -Body $body
     * 
     * Respuesta esperada:
     * {
     *   "answer": "RAG es una técnica que combina...",
     *   "sources": [
     *     {
     *       "content": "RAG es...",
     *       "metadata": {"source": "documento_rag.txt", "chunk_index": 0}
     *     }
     *   ],
     *   "hasEvidence": true,
     *   "latencyMs": 1234
     * }
     */
    @PostMapping
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
        // Llama al servicio RAG para procesar la consulta
        // request.getQuery() → La pregunta del usuario
        // request.getTopK() → Cuántos documentos recuperar (ej: 3, 5)
        QueryResponse response = ragService.query(request.getQuery(), request.getTopK());
        
        // Registra las métricas de la consulta automáticamente
        // - retrievedDocs: Número de documentos recuperados (sources)
        // - relevantDocs: Documentos relevantes (si hay evidencia, asumimos que todos son relevantes)
        // - latencyMs: Tiempo de respuesta ya calculado en el RAGService
        int retrievedDocs = response.getSources().size();
        int relevantDocs = response.isHasEvidence() ? retrievedDocs : 0;
        
        metricsService.recordMetrics(
            request.getQuery(),
            retrievedDocs,
            relevantDocs,
            response.getLatencyMs()
        );
        
        // Retorna código 200 (OK) con la respuesta en formato JSON
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para obtener las métricas promedio del sistema.
     * 
     * Ruta completa: GET http://localhost:8080/api/query/metrics
     * 
     * Retorna los promedios de:
     * - Precision: Proporción de documentos relevantes entre los recuperados
     * - Recall: Proporción de documentos relevantes encontrados
     * - Latency: Tiempo de respuesta en milisegundos
     * 
     * Ejemplo de uso con curl:
     * curl http://localhost:8080/api/query/metrics
     * 
     * Ejemplo de uso con PowerShell:
     * Invoke-RestMethod -Uri "http://localhost:8080/api/query/metrics"
     * 
     * Respuesta esperada:
     * {
     *   "avg_precision": 0.85,
     *   "avg_recall": 0.92,
     *   "avg_latency": 1234.5
     * }
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = metricsService.getAverageMetrics();
        return ResponseEntity.ok(metrics);
    }
}
