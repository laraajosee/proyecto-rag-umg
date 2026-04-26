-- Habilitar extensión pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Tabla para almacenar chunks y embeddings
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(768),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsqueda vectorial
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx 
ON vector_store USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- Tabla para métricas de evaluación
CREATE TABLE IF NOT EXISTS evaluation_metrics (
    id SERIAL PRIMARY KEY,
    query TEXT NOT NULL,
    retrieved_docs INTEGER,
    relevant_docs INTEGER,
    precision DECIMAL(5,4),
    recall DECIMAL(5,4),
    latency_ms INTEGER,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
