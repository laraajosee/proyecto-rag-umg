package com.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Servicio para ingestar documentos en la base vectorial
 * Procesa archivos de texto, los divide en chunks y genera embeddings
 */
@Service
public class DocumentIngestionService {
    
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    
    @Value("${rag.chunk-size:500}")
    private int chunkSize;
    
    @Value("${rag.chunk-overlap:50}")
    private int chunkOverlap;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * Ingesta todos los documentos de la carpeta de recursos
     * Procesa archivos .txt y los convierte en chunks vectorizados
     */
    public void ingestDocuments() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:documents/*.txt");
            
            List<Document> allDocuments = new ArrayList<>();
            
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    // Leer documento
                    TextReader textReader = new TextReader(resource);
                    List<Document> documents = textReader.get();
                    
                    // Dividir en chunks
                    List<Document> chunks = textSplitter.apply(documents);
                    
                    // Agregar metadatos a cada chunk
                    for (int i = 0; i < chunks.size(); i++) {
                        Document chunk = chunks.get(i);
                        chunk.getMetadata().putAll(Map.of(
                            "source", filename,
                            "chunk_index", i,
                            "total_chunks", chunks.size()
                        ));
                    }
                    
                    allDocuments.addAll(chunks);
                }
            }
            
            // Almacenar en base vectorial
            if (!allDocuments.isEmpty()) {
                vectorStore.add(allDocuments);
                System.out.println("Ingestados " + allDocuments.size() + " chunks de " + resources.length + " documentos");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error al ingestar documentos: " + e.getMessage(), e);
        }
    }
}