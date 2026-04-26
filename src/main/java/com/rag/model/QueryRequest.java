package com.rag.model;

/**
 * Clase que representa una petición de consulta al sistema RAG.
 * 
 * Esta clase es un DTO (Data Transfer Object) que se usa para:
 * - Recibir datos JSON del cliente HTTP
 * - Validar los parámetros de la consulta
 * - Pasar los datos al servicio RAG
 * 
 * Ejemplo de JSON que se mapea a esta clase:
 * {
 *   "query": "¿Qué es RAG?",
 *   "topK": 3
 * }
 * 
 * Spring Boot automáticamente convierte el JSON a un objeto QueryRequest
 * usando Jackson (librería de serialización JSON).
 */
public class QueryRequest {
    
    /**
     * La pregunta o consulta del usuario.
     * 
     * Ejemplos:
     * - "¿Qué es RAG?"
     * - "Explica las estrategias de chunking"
     * - "¿Cómo funcionan los embeddings?"
     */
    private String query;
    
    /**
     * Número de documentos a recuperar de la base vectorial.
     * 
     * - Si es null, se usa el valor por defecto de application.yml (rag.top-k)
     * - Valores típicos: 3, 5, 10
     * - Más alto = más contexto pero más lento y costoso
     * - Más bajo = más rápido pero puede perder información relevante
     * 
     * Ejemplos:
     * - topK = 3: Recupera los 3 chunks más similares
     * - topK = 5: Recupera los 5 chunks más similares
     */
    private Integer topK;
    
    /**
     * Constructor vacío requerido por Jackson para deserialización JSON.
     * 
     * Jackson necesita este constructor para crear el objeto
     * antes de asignar los valores desde el JSON.
     */
    public QueryRequest() {}
    
    /**
     * Constructor con parámetros para crear objetos fácilmente en código.
     * 
     * @param query La pregunta del usuario
     * @param topK Número de documentos a recuperar
     * 
     * Ejemplo de uso:
     * QueryRequest request = new QueryRequest("¿Qué es RAG?", 3);
     */
    public QueryRequest(String query, Integer topK) {
        this.query = query;
        this.topK = topK;
    }
    
    // ========== GETTERS Y SETTERS ==========
    // Requeridos por Jackson para serialización/deserialización JSON
    // También siguen el estándar JavaBean
    
    /**
     * Obtiene la pregunta del usuario.
     * @return La query
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * Establece la pregunta del usuario.
     * @param query La query a establecer
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * Obtiene el número de documentos a recuperar.
     * @return El valor de topK (puede ser null)
     */
    public Integer getTopK() {
        return topK;
    }
    
    /**
     * Establece el número de documentos a recuperar.
     * @param topK El valor de topK
     */
    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
