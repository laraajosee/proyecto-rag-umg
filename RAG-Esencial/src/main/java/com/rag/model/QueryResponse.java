package com.rag.model;

import java.util.List;

/**
 * Modelo para las respuestas de consulta RAG
 * Contiene la respuesta generada, fuentes utilizadas y métricas
 */
public class QueryResponse {
    private String answer;
    private List<DocumentSource> sources;
    private boolean hasEvidence;
    private long latencyMs;

    public QueryResponse() {}

    public QueryResponse(String answer, List<DocumentSource> sources, boolean hasEvidence, long latencyMs) {
        this.answer = answer;
        this.sources = sources;
        this.hasEvidence = hasEvidence;
        this.latencyMs = latencyMs;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<DocumentSource> getSources() {
        return sources;
    }

    public void setSources(List<DocumentSource> sources) {
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

    /**
     * Clase interna para representar las fuentes de documentos
     */
    public static class DocumentSource {
        private String content;
        private DocumentMetadata metadata;

        public DocumentSource() {}

        public DocumentSource(String content, DocumentMetadata metadata) {
            this.content = content;
            this.metadata = metadata;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public DocumentMetadata getMetadata() {
            return metadata;
        }

        public void setMetadata(DocumentMetadata metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Clase interna para metadatos de documentos
     */
    public static class DocumentMetadata {
        private String source;
        private int chunk_index;
        private int total_chunks;

        public DocumentMetadata() {}

        public DocumentMetadata(String source, int chunk_index, int total_chunks) {
            this.source = source;
            this.chunk_index = chunk_index;
            this.total_chunks = total_chunks;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public int getChunk_index() {
            return chunk_index;
        }

        public void setChunk_index(int chunk_index) {
            this.chunk_index = chunk_index;
        }

        public int getTotal_chunks() {
            return total_chunks;
        }

        public void setTotal_chunks(int total_chunks) {
            this.total_chunks = total_chunks;
        }
    }
}