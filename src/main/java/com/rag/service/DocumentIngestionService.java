package com.rag.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de procesar e indexar documentos en el sistema RAG.
 * 
 * Este servicio implementa el pipeline de ingesta de documentos:
 * 1. Lee archivos PDF o TXT de una carpeta
 * 2. Extrae el texto de cada archivo
 * 3. Divide el texto en chunks (fragmentos)
 * 4. Genera embeddings (vectores) para cada chunk
 * 5. Almacena los chunks y embeddings en PostgreSQL con pgvector
 * 
 * @Service: Indica que esta clase es un servicio de Spring (lógica de negocio)
 * Spring la detecta automáticamente y la hace disponible para inyección
 */
@Service
public class DocumentIngestionService {
    
    // VectorStore: Interfaz de Spring AI para interactuar con la base vectorial
    // En este proyecto, usa PgVectorStore (PostgreSQL + pgvector)
    private final VectorStore vectorStore;
    
    // @Value: Inyecta valores desde application.yml
    // ${rag.chunk-size:500} → Lee rag.chunk-size, si no existe usa 500 por defecto
    @Value("${rag.chunk-size:500}")
    private int chunkSize;  // Tamaño de cada fragmento en caracteres (ej: 500)
    
    @Value("${rag.chunk-overlap:50}")
    private int chunkOverlap;  // Solapamiento entre chunks (ej: 50 caracteres)
    
    @Value("${rag.documents-path}")
    private String documentsPath;  // Ruta donde están los documentos (ej: src/main/resources/documents)
    
    /**
     * Constructor del servicio.
     * Spring inyecta automáticamente el VectorStore configurado.
     * 
     * @param vectorStore Almacén vectorial (PgVectorStore en este caso)
     */
    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    /**
     * Método principal para ingestar todos los documentos de la carpeta.
     * 
     * Proceso:
     * 1. Verifica que la carpeta de documentos exista
     * 2. Lista todos los archivos PDF y TXT
     * 3. Procesa cada archivo uno por uno
     * 4. Imprime mensajes de progreso en la consola
     * 
     * @throws IOException Si hay error al leer archivos
     * 
     * Ejemplo de logs:
     * Processing 5 documents
     * Processing: documento_rag.txt
     * Indexed 8 chunks from documento_rag.txt
     * Processing: chunking_estrategias.txt
     * Indexed 15 chunks from chunking_estrategias.txt
     * ...
     * Document ingestion completed
     */
    public void ingestDocuments() throws IOException {
        // Crea un objeto File apuntando a la carpeta de documentos
        File folder = new File(documentsPath);
        
        // Verifica si la carpeta existe
        if (!folder.exists()) {
            System.out.println("Documents folder not found: " + documentsPath);
            return;  // Sale del método si no existe
        }
        
        // Lista todos los archivos que terminan en .pdf o .txt (case-insensitive)
        // Lambda: (dir, name) -> condición
        File[] files = folder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".txt"));
        
        // Verifica si hay archivos
        if (files == null || files.length == 0) {
            System.out.println("No PDF or TXT files found in: " + documentsPath);
            return;
        }
        
        System.out.println("Processing " + files.length + " documents");
        
        // Procesa cada archivo uno por uno
        for (File file : files) {
            processDocument(file);
        }
        
        System.out.println("Document ingestion completed");
    }
    
    /**
     * Procesa un documento individual.
     * 
     * Pasos:
     * 1. Extrae el texto del archivo (PDF o TXT)
     * 2. Divide el texto en chunks
     * 3. Crea objetos Document de Spring AI con metadata
     * 4. Guarda los chunks en la base vectorial
     * 
     * @param file Archivo a procesar
     * @throws IOException Si hay error al leer el archivo
     * 
     * Ejemplo de metadata guardada:
     * {
     *   "source": "documento_rag.txt",
     *   "chunk_index": 0,
     *   "total_chunks": 8
     * }
     */
    private void processDocument(File file) throws IOException {
        System.out.println("Processing: " + file.getName());
        
        // Extrae el texto según el tipo de archivo
        String text;
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            text = extractTextFromPDF(file);  // Usa PDFBox para PDFs
        } else {
            text = extractTextFromTXT(file);  // Lee directamente para TXT
        }
        
        // Divide el texto en chunks (fragmentos)
        List<String> chunks = chunkText(text);
        
        // Crea una lista de objetos Document de Spring AI
        List<Document> documents = new ArrayList<>();
        
        // Para cada chunk, crea un Document con metadata
        for (int i = 0; i < chunks.size(); i++) {
            // Metadata: información adicional sobre el chunk
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", file.getName());        // Nombre del archivo original
            metadata.put("chunk_index", i);                // Posición del chunk (0, 1, 2...)
            metadata.put("total_chunks", chunks.size());   // Total de chunks del documento
            
            // Crea un Document de Spring AI
            // Document(contenido, metadata)
            documents.add(new Document(chunks.get(i), metadata));
        }
        
        // Guarda todos los chunks en la base vectorial
        // Spring AI automáticamente:
        // 1. Genera embeddings usando Ollama (nomic-embed-text)
        // 2. Guarda en PostgreSQL (tabla vector_store)
        vectorStore.add(documents);
        
        System.out.println("Indexed " + chunks.size() + " chunks from " + file.getName());
    }
    
    /**
     * Extrae texto de un archivo TXT.
     * 
     * @param file Archivo TXT
     * @return Texto completo del archivo
     * @throws IOException Si hay error al leer
     */
    private String extractTextFromTXT(File file) throws IOException {
        // Lee todos los bytes del archivo y los convierte a String
        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }
    
    /**
     * Extrae texto de un archivo PDF usando Apache PDFBox.
     * 
     * @param file Archivo PDF
     * @return Texto completo extraído del PDF
     * @throws IOException Si hay error al leer el PDF
     */
    private String extractTextFromPDF(File file) throws IOException {
        // try-with-resources: Cierra automáticamente el documento al terminar
        try (PDDocument document = PDDocument.load(file)) {
            // PDFTextStripper: Clase de PDFBox para extraer texto
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * Divide un texto largo en chunks (fragmentos) con solapamiento.
     * 
     * Estrategia: Fixed-size chunking con overlap
     * 
     * Ejemplo con chunkSize=500 y chunkOverlap=50:
     * Texto: "ABCDEFGHIJKLMNOPQRSTUVWXYZ..." (1000 caracteres)
     * 
     * Chunk 0: caracteres 0-500
     * Chunk 1: caracteres 450-950  (overlap de 50 con el anterior)
     * Chunk 2: caracteres 900-1000 (overlap de 50 con el anterior)
     * 
     * El overlap evita perder información en los límites entre chunks.
     * 
     * @param text Texto completo a dividir
     * @return Lista de chunks (fragmentos de texto)
     * 
     * Ventajas del overlap:
     * - Evita cortar información importante en los límites
     * - Mejora la recuperación de información
     * - Mantiene contexto entre chunks consecutivos
     */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;  // Posición inicial del chunk actual
        
        // Mientras no hayamos procesado todo el texto
        while (start < text.length()) {
            // Calcula la posición final del chunk
            // Math.min asegura que no nos pasemos del final del texto
            int end = Math.min(start + chunkSize, text.length());
            
            // Extrae el substring y lo agrega a la lista
            chunks.add(text.substring(start, end));
            
            // Avanza la posición inicial para el siguiente chunk
            // Resta el overlap para que haya solapamiento
            // Ejemplo: start=0, chunkSize=500, overlap=50
            //          próximo start = 0 + (500-50) = 450
            start += (chunkSize - chunkOverlap);
        }
        
        return chunks;
    }
}
