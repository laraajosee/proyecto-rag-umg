package com.rag.evaluation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para registrar y calcular métricas de evaluación del sistema RAG.
 * 
 * Este servicio es crucial para el Sprint 4 del proyecto, donde debes:
 * - Realizar experimentos comparando diferentes configuraciones
 * - Medir la calidad del sistema RAG
 * - Documentar resultados con métricas objetivas
 * 
 * Métricas implementadas:
 * - Precision@k: Proporción de documentos relevantes entre los recuperados
 * - Recall@k: Proporción de documentos relevantes que fueron encontrados
 * - Latency: Tiempo de respuesta en milisegundos
 * 
 * Las métricas se guardan en la tabla evaluation_metrics de PostgreSQL.
 * 
 * @Service: Indica que esta clase es un servicio de Spring
 */
@Service
public class MetricsService {
    
    /**
     * JdbcTemplate: Clase de Spring para ejecutar queries SQL fácilmente.
     * 
     * Ventajas sobre JDBC puro:
     * - Manejo automático de conexiones
     * - Manejo automático de excepciones
     * - Menos código boilerplate
     * - Integración con transacciones de Spring
     */
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Constructor del servicio.
     * Spring inyecta automáticamente el JdbcTemplate configurado.
     * 
     * @param jdbcTemplate Template para ejecutar queries SQL
     */
    public MetricsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Registra las métricas de una consulta en la base de datos.
     * 
     * Este método:
     * 1. Calcula precision y recall
     * 2. Guarda las métricas en la tabla evaluation_metrics
     * 3. Imprime las métricas en la consola para debugging
     * 
     * @param query La pregunta del usuario
     * @param retrievedDocs Número de documentos recuperados (ej: 5 si topK=5)
     * @param relevantDocs Número de documentos relevantes entre los recuperados
     * @param latencyMs Tiempo de respuesta en milisegundos
     * 
     * Ejemplo de uso:
     * metricsService.recordMetrics(
     *     "¿Qué es RAG?",
     *     5,    // Se recuperaron 5 documentos
     *     4,    // 4 de ellos eran relevantes
     *     1234  // Tardó 1234ms
     * );
     * 
     * Esto guardará:
     * - precision = 4/5 = 0.8 (80% de los recuperados eran relevantes)
     * - recall = 1.0 (simplificado, asume que se encontraron todos)
     * - latency = 1234ms
     * 
     * NOTA: En un sistema real, recall requiere conocer el total de documentos
     * relevantes en toda la base de datos, lo cual es difícil de determinar.
     * Por eso aquí está simplificado.
     */
    public void recordMetrics(String query, int retrievedDocs, int relevantDocs, long latencyMs) {
        // Calcula precision: ¿Qué proporción de los recuperados son relevantes?
        // Fórmula: precision = documentos_relevantes_recuperados / total_documentos_recuperados
        // 
        // Ejemplo:
        // - Recuperamos 5 documentos (topK=5)
        // - 4 de ellos son relevantes
        // - precision = 4/5 = 0.8 (80%)
        // 
        // Precision alta = El sistema recupera documentos relevantes
        // Precision baja = El sistema recupera mucho ruido
        double precision = relevantDocs > 0 ? (double) relevantDocs / retrievedDocs : 0.0;
        
        // Calcula recall: ¿Qué proporción de todos los relevantes fueron encontrados?
        // Fórmula: recall = documentos_relevantes_recuperados / total_documentos_relevantes_en_bd
        // 
        // SIMPLIFICACIÓN: Aquí asumimos recall=1.0 si encontramos algo relevante
        // En un sistema real, necesitarías:
        // 1. Un dataset de evaluación con respuestas correctas conocidas
        // 2. Saber cuántos documentos relevantes existen en total
        // 
        // Recall alto = El sistema encuentra la mayoría de documentos relevantes
        // Recall bajo = El sistema se pierde documentos relevantes
        double recall = relevantDocs > 0 ? 1.0 : 0.0; // Simplificado
        
        // SQL para insertar las métricas en la base de datos
        // 
        // Tabla evaluation_metrics:
        // - query: La pregunta
        // - retrieved_docs: Cuántos documentos se recuperaron
        // - relevant_docs: Cuántos eran relevantes
        // - precision: Precisión calculada
        // - recall: Recall calculado
        // - latency_ms: Tiempo de respuesta
        // - timestamp: Cuándo se hizo la consulta (automático)
        String sql = """
            INSERT INTO evaluation_metrics (query, retrieved_docs, relevant_docs, precision, recall, latency_ms)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        // Ejecuta el INSERT con los parámetros
        // Los ? se reemplazan con los valores en orden
        jdbcTemplate.update(sql, query, retrievedDocs, relevantDocs, precision, recall, latencyMs);
        
        // Imprime las métricas en la consola para debugging
        System.out.println("Metrics recorded - Precision: " + precision + 
                          ", Recall: " + recall + 
                          ", Latency: " + latencyMs + "ms");
    }
    
    /**
     * Obtiene los promedios de todas las métricas registradas.
     * 
     * Útil para:
     * - Evaluar el rendimiento general del sistema
     * - Comparar diferentes configuraciones (Sprint 4)
     * - Reportes y presentaciones
     * 
     * @return Map con los promedios de precision, recall y latency
     * 
     * Ejemplo de uso:
     * Map<String, Object> averages = metricsService.getAverageMetrics();
     * System.out.println("Precision promedio: " + averages.get("avg_precision"));
     * System.out.println("Recall promedio: " + averages.get("avg_recall"));
     * System.out.println("Latencia promedio: " + averages.get("avg_latency"));
     * 
     * Resultado esperado:
     * {
     *   "avg_precision": 0.85,
     *   "avg_recall": 0.92,
     *   "avg_latency": 1234.5
     * }
     * 
     * Interpretación:
     * - avg_precision = 0.85: En promedio, 85% de los documentos recuperados son relevantes
     * - avg_recall = 0.92: En promedio, se encuentran 92% de los documentos relevantes
     * - avg_latency = 1234.5ms: En promedio, las consultas tardan 1.2 segundos
     */
    public Map<String, Object> getAverageMetrics() {
        // SQL para calcular promedios
        // AVG() es una función de agregación de SQL que calcula el promedio
        String sql = """
            SELECT 
                AVG(precision) as avg_precision,
                AVG(recall) as avg_recall,
                AVG(latency_ms) as avg_latency
            FROM evaluation_metrics
            """;
        
        // Ejecuta la query y retorna un Map con los resultados
        // queryForMap() retorna una sola fila como Map<String, Object>
        // Las keys son los nombres de las columnas (avg_precision, avg_recall, avg_latency)
        return jdbcTemplate.queryForMap(sql);
    }
}
