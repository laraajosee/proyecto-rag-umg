# Arquitectura del Sistema RAG

## Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                        Cliente (UI/API)                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot REST API                      │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ DocumentController│  │  QueryController │                │
│  └────────┬─────────┘  └────────┬─────────┘                │
└───────────┼─────────────────────┼──────────────────────────┘
            │                     │
            ▼                     ▼
┌───────────────────────┐  ┌──────────────────────────────────┐
│ DocumentIngestionService│  │       RAGService                │
│                       │  │                                  │
│ 1. Leer PDFs          │  │ 1. Convertir query a embedding   │
│ 2. Extraer texto      │  │ 2. Buscar en VectorStore (top-k) │
│ 3. Chunking           │  │ 3. Construir prompt con contexto │
│ 4. Generar embeddings │  │ 4. Llamar LLM                    │
│ 5. Guardar en DB      │  │ 5. Retornar respuesta + fuentes  │
└───────────┬───────────┘  └──────────┬───────────────────────┘
            │                         │
            │                         │
            ▼                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring AI Framework                       │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ EmbeddingClient  │  │    ChatClient    │                │
│  │  (OpenAI)        │  │    (OpenAI)      │                │
│  └────────┬─────────┘  └────────┬─────────┘                │
└───────────┼─────────────────────┼──────────────────────────┘
            │                     │
            ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      OpenAI API                              │
│  • text-embedding-ada-002 (embeddings)                      │
│  • gpt-3.5-turbo (generación)                               │
└─────────────────────────────────────────────────────────────┘

            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL + pgvector                           │
│                                                              │
│  Tabla: vector_store                                        │
│  ┌────────────────────────────────────────────┐            │
│  │ id | content | metadata | embedding (1536) │            │
│  └────────────────────────────────────────────┘            │
│                                                              │
│  Índice: HNSW (búsqueda vectorial rápida)                  │
└─────────────────────────────────────────────────────────────┘
```

## Flujo de Ingesta

1. Usuario coloca PDFs en `src/main/resources/documents/`
2. Llama a `POST /api/documents/ingest`
3. `DocumentIngestionService`:
   - Lee cada PDF con Apache PDFBox
   - Extrae texto completo
   - Divide en chunks (500 caracteres, overlap 50)
   - Genera metadata (source, chunk_index)
4. Spring AI genera embeddings con OpenAI
5. Se almacenan en PostgreSQL con pgvector

## Flujo de Query

1. Usuario envía pregunta: `POST /api/query`
2. `RAGService`:
   - Convierte query a embedding
   - Busca top-k chunks similares (cosine similarity)
   - Construye prompt con contexto recuperado
   - Envía a GPT-3.5-turbo
3. LLM genera respuesta basada en contexto
4. Se retorna:
   - Respuesta generada
   - Fuentes utilizadas
   - Metadata (latencia, hasEvidence)

## Tecnologías

- **Spring Boot 3.2.4**: Framework base
- **Spring AI 0.8.1**: Abstracción para LLMs y embeddings
- **PostgreSQL + pgvector**: Base de datos vectorial
- **OpenAI API**: Embeddings y generación
- **Apache PDFBox**: Procesamiento de PDFs
- **Lombok**: Reducción de boilerplate

## Configuración de Experimentos

Para cumplir con los experimentos obligatorios, modifica `application.yml`:

```yaml
rag:
  chunk-size: 300  # Experimento 1: 300 vs 500
  top-k: 3         # Experimento 2: 3 vs 5

spring:
  ai:
    openai:
      embedding:
        options:
          model: text-embedding-ada-002  # Experimento 3: probar otros modelos
```

## Métricas Implementadas

- **Precision@k**: Relevancia de documentos recuperados
- **Recall@k**: Cobertura de documentos relevantes
- **Latency**: Tiempo de respuesta end-to-end
- **Faithfulness**: Respuesta basada en evidencia (hasEvidence flag)

## Próximas Mejoras (Sprint 4)

1. Re-ranking con modelo cross-encoder
2. Búsqueda híbrida (keyword + vectorial)
3. Filtros por metadata
4. Memoria de conversación
5. UI con Thymeleaf o React
