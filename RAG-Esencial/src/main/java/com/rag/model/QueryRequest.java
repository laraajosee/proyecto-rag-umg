package com.rag.model;

/**
 * Modelo para las solicitudes de consulta RAG
 * Contiene la pregunta del usuario y parámetros de búsqueda
 */
public class QueryRequest {
    private String query;
    private int topK = 5;

    // Constructor vacío requerido por Jackson
    public QueryRequest() {}

    public QueryRequest(String query, int topK) {
        this.query = query;
        this.topK = topK;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}