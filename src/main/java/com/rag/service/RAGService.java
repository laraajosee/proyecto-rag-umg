package com.rag.service;

import com.rag.model.DocumentChunk;
import com.rag.model.QueryResponse;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio que implementa el sistema RAG (Retrieval-Augmented Generation).
 * 
 * Este es el corazón del sistema RAG. Implementa el flujo completo:
 * 1. RETRIEVAL: Busca información relevante en la base vectorial
 * 2. AUGMENTATION: Construye un prompt con el contexto recuperado
 * 3. GENERATION: Genera una respuesta usando el LLM (Llama2)
 * 
 * RAG permite que el modelo responda basándose en documentos específicos
 * en lugar de solo su conocimiento pre-entrenado, reduciendo alucinaciones
 * y permitiendo citar fuentes.
 * 
 * @Service: Indica que esta clase es un servicio de Spring
 */
@Service
public class RAGService {
    
    // VectorStore: Para buscar documentos similares en PostgreSQL + pgvector
    private final VectorStore vectorStore;
    
    // ChatClient: Para interactuar con el LLM (Ollama/Llama2)
    private final ChatClient chatClient;
    
    // top-k: Cuántos documentos recuperar por defecto
    // Se puede sobrescribir en cada consulta
    @Value("${rag.top-k:5}")
    private int topK;
    
    /**
     * Prompt del sistema que define el comportamiento del asistente.
     * 
     * Este prompt es CRÍTICO para la calidad de las respuestas:
     * - Instruye al modelo a responder SOLO con el contexto proporcionado
     * - Evita que el modelo invente información (alucinaciones)
     * - Le dice que cite las fuentes
     * - Le dice que responda en español
     * 
     * El placeholder {context} se reemplaza con los chunks recuperados.
     * 
     * Ejemplo de prompt final:
     * "Eres un asistente que responde preguntas en ESPAÑOL...
     *  
     *  Contexto recuperado:
     *  [Fuente: documento_rag.txt]
     *  RAG es una técnica que combina...
     *  
     *  [Fuente: embeddings_vectores.txt]
     *  Los embeddings son representaciones..."
     */
    private static final String SYSTEM_PROMPT = """
            Eres un asistente que SIEMPRE responde en ESPAÑOL.
            
            INSTRUCCIONES OBLIGATORIAS:
            1. RESPONDE ÚNICAMENTE EN ESPAÑOL - Nunca uses inglés
            2. Basa tu respuesta SOLO en el contexto proporcionado abajo
            3. Si no hay suficiente información, di: "No tengo evidencia suficiente en los documentos recuperados para responder esta pregunta"
            4. Cita las fuentes mencionando el nombre del documento
            5. No inventes información que no esté en el contexto
            6. Sé preciso, claro y conciso
            
            IMPORTANTE: Tu respuesta debe estar completamente en español, sin excepciones.
            
            Contexto recuperado:
            {context}
            
            Recuerda: RESPONDE EN ESPAÑOL.
            """;
    
    /**
     * Constructor del servicio.
     * Spring inyecta automáticamente VectorStore y ChatClient.
     * 
     * @param vectorStore Almacén vectorial (PgVectorStore)
     * @param chatClient Cliente para interactuar con Ollama
     */
    public RAGService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }
    
    /**
     * Método principal que implementa el flujo RAG completo.
     * 
     * FLUJO DETALLADO:
     * 
     * 1. RETRIEVAL (Recuperación):
     *    - Convierte la query en embedding (vector)
     *    - Busca los k chunks más similares usando similitud coseno
     *    - Recupera los chunks de la base de datos
     * 
     * 2. AUGMENTATION (Aumento):
     *    - Construye un contexto con los chunks recuperados
     *    - Formatea cada chunk con su fuente
     *    - Crea el prompt del sistema con el contexto
     * 
     * 3. GENERATION (Generación):
     *    - Envía el prompt + query a Ollama (Llama2)
     *    - El modelo genera una respuesta basada en el contexto
     *    - Retorna la respuesta junto con las fuentes
     * 
     * @param query La pregunta del usuario (ej: "¿Qué es RAG?")
     * @param customTopK Número de documentos a recuperar (null = usa default)
     * @return QueryResponse con respuesta, fuentes, evidencia y latencia
     * 
     * Ejemplo de uso:
     * QueryResponse response = ragService.query("¿Qué es RAG?", 3);
     * 
     * Respuesta esperada:
     * {
     *   "answer": "RAG es una técnica que combina recuperación de información...",
     *   "sources": [
     *     {"content": "RAG es...", "metadata": {"source": "documento_rag.txt"}},
     *     {"content": "Los embeddings...", "metadata": {"source": "embeddings.txt"}}
     *   ],
     *   "hasEvidence": true,
     *   "latencyMs": 1234
     * }
     */
    public QueryResponse query(String query, Integer customTopK) {
        // Inicia el cronómetro para medir latencia
        long startTime = System.currentTimeMillis();
        
        // Usa customTopK si se proporciona, sino usa el valor por defecto
        // Operador ternario: condición ? valorSiTrue : valorSiFalse
        int k = customTopK != null ? customTopK : topK;
        
        // ========== PASO 1: RETRIEVAL (Recuperación) ==========
        // Busca los k documentos más similares a la query
        // 
        // Proceso interno:
        // 1. Spring AI convierte la query en embedding usando Ollama (nomic-embed-text)
        // 2. Ejecuta búsqueda vectorial en PostgreSQL:
        //    SELECT content, metadata, embedding <=> query_embedding AS distance
        //    FROM vector_store
        //    ORDER BY distance
        //    LIMIT k
        // 3. Retorna los k chunks más similares
        List<Document> retrievedDocs = vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(k)
        );
        
        // Verifica si se encontraron documentos relevantes
        boolean hasEvidence = !retrievedDocs.isEmpty();
        
        // ========== PASO 2: AUGMENTATION (Construcción del contexto) ==========
        // Construye un string con todos los chunks recuperados
        // 
        // Formato de cada chunk:
        // [Fuente: documento_rag.txt]
        // RAG es una técnica que combina...
        // 
        // [Fuente: embeddings_vectores.txt]
        // Los embeddings son representaciones...
        String context = retrievedDocs.stream()
            .map(doc -> String.format("[Fuente: %s]\n%s", 
                doc.getMetadata().get("source"),  // Nombre del archivo
                doc.getContent()))                 // Contenido del chunk
            .collect(Collectors.joining("\n\n"));  // Une con doble salto de línea
        
        // ========== PASO 3: GENERATION (Generación de respuesta) ==========
        String answer;
        
        if (!hasEvidence) {
            // Si no se encontraron documentos relevantes, retorna mensaje estándar
            // Esto evita que el modelo invente información
            answer = "No tengo evidencia suficiente en los documentos recuperados para responder esta pregunta.";
        } else {
            // Crea el mensaje del sistema con el contexto
            // SystemPromptTemplate reemplaza {context} con el contexto real
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));
            
            // Crea el mensaje del usuario con la pregunta
            // Agregamos "Responde en español:" al inicio para reforzar
            String userQuery = "Responde en español: " + query;
            UserMessage userMessage = new UserMessage(userQuery);
            
            // Crea el prompt completo combinando sistema + usuario
            // Prompt final:
            // [SYSTEM]: Eres un asistente que SIEMPRE responde en ESPAÑOL... Contexto: [chunks]
            // [USER]: Responde en español: ¿Qué es RAG?
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            
            // Envía el prompt a Ollama (Llama2) y obtiene la respuesta
            // Esto puede tardar 10-30 segundos con Ollama local
            ChatResponse response = chatClient.call(prompt);
            
            // Extrae el texto de la respuesta
            answer = response.getResult().getOutput().getContent();
        }
        
        // ========== PASO 4: PREPARAR RESPUESTA ==========
        // Convierte los Document de Spring AI a DocumentChunk para la respuesta
        List<DocumentChunk> sources = new ArrayList<>();
        for (Document doc : retrievedDocs) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setContent(doc.getContent());      // Texto del chunk
            chunk.setMetadata(doc.getMetadata());    // Metadata (source, chunk_index, etc.)
            sources.add(chunk);
        }
        
        // Calcula la latencia total (tiempo transcurrido)
        long latency = System.currentTimeMillis() - startTime;
        
        // Retorna la respuesta completa con:
        // - answer: La respuesta generada por el LLM
        // - sources: Los chunks usados como contexto
        // - hasEvidence: Si se encontró evidencia
        // - latencyMs: Tiempo total de procesamiento
        return new QueryResponse(answer, sources, hasEvidence, latency);
    }
}
