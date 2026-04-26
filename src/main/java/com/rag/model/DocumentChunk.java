package com.rag.model;

import java.util.Map;

/**
 * Clase que representa un fragmento (chunk) de documento.
 * 
 * Un chunk es una porción de texto de un documento más grande.
 * Los documentos se dividen en chunks para:
 * - Facilitar la búsqueda vectorial (chunks más pequeños = búsqueda más precisa)
 * - Caber en el contexto del LLM (los LLMs tienen límites de tokens)
 * - Mejorar la relevancia de la información recuperada
 * 
 * Ejemplo de chunk:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "content": "RAG es una técnica que combina recuperación de información...",
 *   "metadata": {
 *     "source": "documento_rag.txt",
 *     "chunk_index": 0,
 *     "total_chunks": 8
 *   },
 *   "score": 0.85
 * }
 */
public class DocumentChunk {
    
    /**
     * Identificador único del chunk.
     * 
     * Generalmente es un UUID generado por PostgreSQL.
     * Útil para:
     * - Referenciar chunks específicos
     * - Debugging
     * - Tracking de qué chunks se usan más
     */
    private String id;
    
    /**
     * El contenido textual del chunk.
     * 
     * Este es el fragmento de texto que:
     * - Se extrajo del documento original
     * - Se convirtió en embedding
     * - Se recuperó de la base vectorial
     * - Se usa como contexto para el LLM
     * 
     * Tamaño típico: 300-1000 caracteres
     * 
     * Ejemplo:
     * "RAG es una técnica de inteligencia artificial que combina
     *  recuperación de información con generación de texto. En lugar
     *  de que un modelo responda solo con su conocimiento pre-entrenado,
     *  RAG permite acceder a una base de conocimientos externa..."
     */
    private String content;
    
    /**
     * Metadata (información adicional) sobre el chunk.
     * 
     * Map<String, Object> permite almacenar cualquier tipo de información.
     * 
     * Campos típicos:
     * - "source": Nombre del archivo original (ej: "documento_rag.txt")
     * - "chunk_index": Posición del chunk en el documento (ej: 0, 1, 2...)
     * - "total_chunks": Total de chunks del documento (ej: 8)
     * - "page": Número de página (si es PDF)
     * - "section": Sección del documento (ej: "Introducción")
     * - "author": Autor del documento
     * - "date": Fecha de creación
     * 
     * Ejemplo:
     * {
     *   "source": "documento_rag.txt",
     *   "chunk_index": 0,
     *   "total_chunks": 8
     * }
     * 
     * Utilidad:
     * - Citar fuentes en la respuesta
     * - Filtrar búsquedas por metadata
     * - Debugging y análisis
     * - Mostrar contexto al usuario
     */
    private Map<String, Object> metadata;
    
    /**
     * Score de similitud con la query (0.0 a 1.0).
     * 
     * Indica qué tan similar es este chunk a la pregunta del usuario.
     * Se calcula usando similitud coseno entre embeddings.
     * 
     * Valores:
     * - 1.0: Idéntico (muy raro)
     * - 0.8-0.9: Muy similar
     * - 0.6-0.8: Similar
     * - 0.4-0.6: Algo similar
     * - < 0.4: Poco similar
     * 
     * Útil para:
     * - Ordenar resultados por relevancia
     * - Filtrar chunks poco relevantes
     * - Métricas de evaluación
     * - Debugging del retrieval
     * 
     * Ejemplo:
     * Query: "¿Qué es RAG?"
     * Chunk: "RAG es una técnica..." → score = 0.87 (muy relevante)
     * Chunk: "Los embeddings son..." → score = 0.45 (menos relevante)
     */
    private Double score;
    
    /**
     * Constructor vacío requerido por Jackson.
     */
    public DocumentChunk() {}
    
    /**
     * Constructor con todos los parámetros.
     * 
     * @param id Identificador único
     * @param content Contenido del chunk
     * @param metadata Información adicional
     * @param score Score de similitud
     */
    public DocumentChunk(String id, String content, Map<String, Object> metadata, Double score) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
        this.score = score;
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
}
