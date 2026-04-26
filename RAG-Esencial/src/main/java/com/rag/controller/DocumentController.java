package com.rag.controller;

import com.rag.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de documentos
 * Expone endpoint para ingestar documentos en la base vectorial
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {
    
    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    /**
     * Endpoint para ingestar documentos
     * Procesa todos los archivos .txt de la carpeta documents
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestDocuments() {
        try {
            documentIngestionService.ingestDocuments();
            return ResponseEntity.ok("Documents ingested successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error ingesting documents: " + e.getMessage());
        }
    }
}