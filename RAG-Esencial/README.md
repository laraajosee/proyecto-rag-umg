# 🤖 RAG Esencial - Sistema Mínimo

Este es un proyecto RAG (Retrieval-Augmented Generation) con **solo las clases esenciales** para funcionar.

## 📁 Estructura Mínima

```
RAG-Esencial/
├── src/main/java/com/rag/
│   ├── RagApplication.java           # Clase principal
│   ├── controller/
│   │   ├── QueryController.java      # API para consultas
│   │   └── DocumentController.java   # API para ingesta
│   ├── service/
│   │   ├── RAGService.java          # Lógica RAG principal
│   │   └── DocumentIngestionService.java # Procesamiento documentos
│   └── model/
│       ├── QueryRequest.java        # Modelo petición
│       └── QueryResponse.java       # Modelo respuesta
├── src/main/resources/
│   ├── application.yml              # Configuración
│   └── documents/                   # Documentos para ingestar
├── docker-compose.yml               # PostgreSQL + pgvector
├── init.sql                        # Script BD
└── pom.xml                         # Dependencias Maven
```

## 🚀 Cómo usar

### 1. Iniciar PostgreSQL
```bash
docker-compose up -d
```

### 2. Iniciar aplicación
```bash
mvn spring-boot:run
```

### 3. Ingestar documentos
```bash
curl -X POST http://localhost:8080/api/documents/ingest
```

### 4. Hacer consulta
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "¿Qué es RAG?", "topK": 3}'
```

## 📋 Requisitos

- Java 17+
- Maven 3.6+
- Docker
- Ollama con modelos `llama2` y `nomic-embed-text`

## 🎯 Funcionalidades

✅ **Ingesta de documentos** - Procesa archivos .txt  
✅ **Búsqueda vectorial** - Encuentra documentos relevantes  
✅ **Generación RAG** - Respuestas con contexto  
✅ **API REST** - Endpoints para consultas  
✅ **Base vectorial** - PostgreSQL + pgvector  

## 🔧 Configuración

Edita `application.yml` para cambiar:
- Puerto de PostgreSQL
- Modelos de Ollama
- Tamaño de chunks
- Parámetros RAG

Este proyecto contiene **solo lo esencial** para un sistema RAG funcional.