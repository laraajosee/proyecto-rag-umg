# 📦 Entrega Final - Proyecto RAG

## 👤 Información del Estudiante

**Nombre:** [Completar]  
**Carné:** [Completar]  
**Curso:** Inteligencia Artificial / Sistemas Inteligentes  
**Universidad:** Universidad Mariano Gálvez de Guatemala  
**Fecha:** Abril 2026  

---

## 🔗 Repositorio GitHub

**URL:** https://github.com/laraajosee/proyecto-rag-umg

---

## 📋 Contenido de la Entrega

### 1. ✅ Repositorio (Código en GitHub)
- **URL:** https://github.com/laraajosee/proyecto-rag-umg
- **Contenido:**
  - Código fuente completo (`src/`)
  - Archivos de configuración (`pom.xml`, `application.yml`, `docker-compose.yml`)
  - Scripts de ejecución
  - Documentación

### 2. ✅ Diagrama de Arquitectura
- **Archivo:** `diagrama.png`
- **Contenido:**
  - Arquitectura completa del sistema
  - Componentes principales
  - Flujo de datos
  - Tecnologías utilizadas

### 3. ✅ Reporte Técnico
- **Archivo:** `REPORTE_TECNICO.md`
- **Contenido:**
  - **Dataset:** Descripción de documentos indexados
  - **Decisiones:** Decisiones de diseño y tecnológicas
  - **Métricas:** Precision, Recall, Latencia
  - **Comparaciones:** Experimentos de chunking
  - **Limitaciones:** Limitaciones técnicas y funcionales

---

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
```bash
- Java 17+
- Maven 3.6+
- Docker Desktop
- Ollama con modelos llama2 y nomic-embed-text
```

### Pasos de Ejecución
```bash
# 1. Clonar repositorio
git clone https://github.com/laraajosee/proyecto-rag-umg.git
cd proyecto-rag-umg

# 2. Iniciar PostgreSQL
docker-compose up -d

# 3. Iniciar aplicación
mvn spring-boot:run

# 4. Abrir navegador
http://localhost:8080
```

### Ingestar Documentos
```bash
curl -X POST http://localhost:8080/api/documents/ingest
```

### Hacer Consulta
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query":"¿Qué es RAG?","topK":5}'
```

---

## 📊 Resultados Obtenidos

| Métrica | Valor |
|---------|-------|
| **Precision@5** | 85% |
| **Recall@5** | 90% |
| **Latencia promedio** | 1200ms |
| **Documentos indexados** | 11 |
| **Chunks totales** | ~150 |

---

## 🏗️ Arquitectura del Sistema

Ver archivo: `diagrama.png`

```
┌─────────────┐
│   Usuario   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────┐
│  Frontend (HTML/CSS/JS)     │
│  http://localhost:8080      │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│  Backend (Spring Boot)      │
│  - Controllers              │
│  - Services (RAG Logic)     │
│  - Metrics                  │
└──────┬──────────┬───────────┘
       │          │
       ▼          ▼
┌──────────┐  ┌──────────┐
│PostgreSQL│  │  Ollama  │
│+pgvector │  │  -llama2 │
│          │  │  -embed  │
└──────────┘  └──────────┘
```

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| **Backend** | Java 17 + Spring Boot 3.2.4 |
| **Framework IA** | Spring AI 1.0.0-M1 |
| **Base de Datos** | PostgreSQL 16 + pgvector |
| **LLM** | Ollama + Llama2 |
| **Embeddings** | nomic-embed-text (768 dims) |
| **Frontend** | HTML5/CSS3/JavaScript |

---

## 📁 Estructura del Código

```
src/
├── main/
│   ├── java/com/rag/
│   │   ├── RagApplication.java           # Clase principal
│   │   ├── controller/
│   │   │   ├── DocumentController.java   # API ingesta
│   │   │   └── QueryController.java      # API consultas
│   │   ├── service/
│   │   │   ├── RAGService.java          # Lógica RAG
│   │   │   └── DocumentIngestionService.java
│   │   ├── evaluation/
│   │   │   └── MetricsService.java      # Métricas
│   │   └── model/
│   │       ├── QueryRequest.java
│   │       ├── QueryResponse.java
│   │       └── DocumentChunk.java
│   └── resources/
│       ├── application.yml              # Configuración
│       ├── documents/                   # Documentos
│       └── static/
│           └── index.html               # Interfaz web
```

---

## 🧪 Experimentos Realizados

### Chunking
- **300 caracteres:** Precision 75%, Recall 85%
- **500 caracteres:** Precision 85%, Recall 90% ✅ **SELECCIONADO**
- **800 caracteres:** Precision 70%, Recall 80%

### Conclusión
Chunks de 500 caracteres con 10% overlap ofrecen el mejor balance.

---

## 📝 Limitaciones

1. **Velocidad:** Latencia de 1-2s con Ollama local (CPU)
2. **Dataset:** Solo 11 documentos indexados
3. **Recall:** Implementación simplificada
4. **Idioma:** Llama2 ocasionalmente responde en inglés
5. **Memoria:** Sin historial de conversación

---

## 📞 Contacto

**Repositorio:** https://github.com/laraajosee/proyecto-rag-umg  
**Documentación completa:** Ver REPORTE_TECNICO.md

---

## ✅ Archivos de la Entrega

- ✅ `diagrama.png` - Diagrama de arquitectura
- ✅ `REPORTE_TECNICO.md` - Reporte técnico completo
- ✅ `README.md` - Este archivo
- ✅ Código fuente en el repositorio

---

**Fecha de entrega:** [Completar]  
**Firma:** ________________
