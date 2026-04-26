package com.rag.controller;

import com.rag.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controlador REST para manejar operaciones relacionadas con documentos.
 * 
 * Este controlador expone endpoints HTTP para:
 * - Ingestar (procesar y guardar) documentos en el sistema RAG
 * 
 * @RestController: Indica que esta clase maneja peticiones HTTP y retorna JSON
 * @RequestMapping: Define la ruta base "/api/documents" para todos los endpoints
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    
    // Servicio que contiene la lógica de negocio para procesar documentos
    // Spring lo inyecta automáticamente (Dependency Injection)
    private final DocumentIngestionService ingestionService;
    
    /**
     * Constructor del controlador.
     * Spring Boot inyecta automáticamente el DocumentIngestionService.
     * 
     * @param ingestionService Servicio para procesar documentos
     */
    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }
    
    /**
     * Endpoint para ingestar documentos.
     * 
     * Ruta completa: POST http://localhost:8080/api/documents/ingest
     * 
     * Proceso:
     * 1. Lee todos los archivos PDF/TXT de la carpeta documents/
     * 2. Extrae el texto de cada archivo
     * 3. Divide el texto en chunks (fragmentos)
     * 4. Genera embeddings (vectores) para cada chunk
     * 5. Guarda los chunks y embeddings en PostgreSQL
     * 
     * @return ResponseEntity con mensaje de éxito o error
     * 
     * Ejemplo de uso con curl:
     * curl -X POST http://localhost:8080/api/documents/ingest
     * 
     * Ejemplo de uso con PowerShell:
     * Invoke-RestMethod -Uri "http://localhost:8080/api/documents/ingest" -Method Post
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestDocuments() {
        try {
            // Llama al servicio para procesar todos los documentos
            ingestionService.ingestDocuments();
            
            // Si todo sale bien, retorna código 200 (OK) con mensaje de éxito
            return ResponseEntity.ok("Documents ingested successfully");
            
        } catch (IOException e) {
            // Si hay un error (archivo no encontrado, error de lectura, etc.)
            // Retorna código 500 (Internal Server Error) con el mensaje de error
            return ResponseEntity.internalServerError()
                .body("Error ingesting documents: " + e.getMessage());
        }
    }
}
