package com.rag.service;

import com.rag.model.QueryRequest;
import com.rag.model.QueryResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio principal RAG que coordina la recuperación y generación
 * Implementa el patrón Retrieval-Augmented Generation
 */
@Service
public class RAGService {
    
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RAGService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Procesa una consulta RAG completa
     * 1. Busca documentos relevantes en la base vectorial
     * 2. Genera respuesta usando LLM con contexto
     * 3. Retorna respuesta estructurada con fuentes
     */
    public QueryResponse processQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 1. Recuperar documentos relevantes
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.query(request.getQuery()).withTopK(request.getTopK())
        );
        
        // 2. Construir contexto para el LLM
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));
        
        // 3. Generar respuesta con contexto
        String prompt = String.format("""
            Responde en español: %s
            
            Contexto disponible:
            %s
            
            Si el contexto no contiene información relevante, indica que no tienes suficiente información.
            """, request.getQuery(), context);
        
        String answer = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        // 4. Preparar fuentes
        List<QueryResponse.DocumentSource> sources = relevantDocs.stream()
            .map(doc -> {
                String source = doc.getMetadata().getOrDefault("source", "documento").toString();
                int chunkIndex = Integer.parseInt(doc.getMetadata().getOrDefault("chunk_index", "0").toString());
                int totalChunks = Integer.parseInt(doc.getMetadata().getOrDefault("total_chunks", "1").toString());
                
                return new QueryResponse.DocumentSource(
                    doc.getContent(),
                    new QueryResponse.DocumentMetadata(source, chunkIndex, totalChunks)
                );
            })
            .collect(Collectors.toList());
        
        long latency = System.currentTimeMillis() - startTime;
        boolean hasEvidence = !relevantDocs.isEmpty();
        
        return new QueryResponse(answer, sources, hasEvidence, latency);
    }
}