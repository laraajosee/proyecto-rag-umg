package com.rag.model;

import java.util.List;

/**
 * Clase que representa la respuesta del sistema RAG a una consulta.
 * 
 * Esta clase es un DTO (Data Transfer Object) que:
 * - Encapsula toda la información de la respuesta
 * - Se serializa automáticamente a JSON por Spring Boot
 * - Incluye la respuesta, fuentes, evidencia y métricas
 * 
 * Ejemplo de JSON generado:
 * {
 *   "answer": "RAG es una técnica que combina recuperación de información...",
 *   "sources": [
 *     {
 *       "content": "RAG es una técnica...",
 *       "metadata": {
 *         "source": "documento_rag.txt",
 *         "chunk_index": 0,
 *         "total_chunks": 8
 *       }
 *     },
 *     {
 *       "content": "Los embeddings son...",
 *       "metadata": {
 *         "source": "embeddings_vectores.txt",
 *         "chunk_index": 2,
 *         "total_chunks": 10
 *       }
 *     }
 *   ],
 *   "hasEvidence": true,
 *   "latencyMs": 1234
 * }
 */
public class QueryResponse {
    
    /**
     * La respuesta generada por el LLM (Llama2).
     * 
     * Esta es la respuesta en lenguaje natural que el usuario verá.
     * Está basada en el contexto recuperado de los documentos.
     * 
     * Ejemplos:
     * - "RAG es una técnica que combina recuperación de información con generación..."
     * - "No tengo evidencia suficiente en los documentos recuperados"
     */
    private String answer;
    
    /**
     * Lista de chunks (fragmentos) de documentos usados como contexto.
     * 
     * Estos son los fragmentos que el sistema recuperó de la base vectorial
     * y que se usaron para generar la respuesta.
     * 
     * Propósito:
     * - Transparencia: El usuario puede ver de dónde viene la información
     * - Trazabilidad: Se pueden citar las fuentes
     * - Verificación: El usuario puede validar que la respuesta es correcta
     * - Debugging: Útil para evaluar la calidad del retrieval
     */
    private List<DocumentChunk> sources;
    
    /**
     * Indica si se encontró evidencia en los documentos.
     * 
     * - true: Se encontraron documentos relevantes y se generó una respuesta
     * - false: No se encontraron documentos relevantes
     * 
     * Este flag es importante para:
     * - Evitar alucinaciones (si no hay evidencia, no inventar)
     * - Métricas de evaluación (faithfulness)
     * - UX: Mostrar al usuario si la respuesta está respaldada
     */
    private boolean hasEvidence;
    
    /**
     * Tiempo total de procesamiento en milisegundos.
     * 
     * Incluye:
     * - Tiempo de búsqueda vectorial
     * - Tiempo de generación del LLM
     * - Tiempo de procesamiento de datos
     * 
     * Útil para:
     * - Monitoreo de rendimiento
     * - Optimización del sistema
     * - Métricas de evaluación
     * - SLA (Service Level Agreement)
     * 
     * Valores típicos:
     * - Con OpenAI: 500-2000ms
     * - Con Ollama local: 5000-30000ms (más lento pero gratis)
     */
    private long latencyMs;
    
    /**
     * Constructor vacío requerido por Jackson.
     */
    public QueryResponse() {}
    
    /**
     * Constructor con todos los parámetros.
     * 
     * @param answer La respuesta generada
     * @param sources Los chunks usados como contexto
     * @param hasEvidence Si se encontró evidencia
     * @param latencyMs Tiempo de procesamiento en ms
     * 
     * Ejemplo de uso:
     * QueryResponse response = new QueryResponse(
     *     "RAG es una técnica...",
     *     sourcesList,
     *     true,
     *     1234
     * );
     */
    public QueryResponse(String answer, List<DocumentChunk> sources, boolean hasEvidence, long latencyMs) {
        this.answer = answer;
        this.sources = sources;
        this.hasEvidence = hasEvidence;
        this.latencyMs = latencyMs;
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public List<DocumentChunk> getSources() {
        return sources;
    }
    
    public void setSources(List<DocumentChunk> sources) {
        this.sources = sources;
    }
    
    public boolean isHasEvidence() {
        return hasEvidence;
    }
    
    public void setHasEvidence(boolean hasEvidence) {
        this.hasEvidence = hasEvidence;
    }
    
    public long getLatencyMs() {
        return latencyMs;
    }
    
    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }
}
