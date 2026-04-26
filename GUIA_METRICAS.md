# Guía de Métricas del Sistema RAG

## 📊 ¿Qué métricas registra el sistema?

El sistema RAG registra automáticamente tres métricas clave en cada consulta:

### 1. **Precision** (Precisión)
- **Qué mide**: Proporción de documentos relevantes entre los recuperados
- **Fórmula**: `precision = documentos_relevantes / documentos_recuperados`
- **Ejemplo**: Si recuperas 5 documentos y 4 son relevantes → precision = 0.8 (80%)
- **Interpretación**:
  - **Alta (>0.8)**: El sistema recupera documentos muy relevantes
  - **Media (0.6-0.8)**: El sistema recupera documentos mayormente relevantes
  - **Baja (<0.6)**: El sistema recupera mucho ruido

### 2. **Recall** (Exhaustividad)
- **Qué mide**: Proporción de documentos relevantes que fueron encontrados
- **Fórmula**: `recall = documentos_relevantes_encontrados / total_documentos_relevantes`
- **Nota**: En este proyecto está simplificado (1.0 si hay evidencia, 0.0 si no)
- **Interpretación**:
  - **Alto (>0.8)**: El sistema encuentra la mayoría de documentos relevantes
  - **Medio (0.6-0.8)**: El sistema se pierde algunos documentos relevantes
  - **Bajo (<0.6)**: El sistema se pierde muchos documentos relevantes

### 3. **Latency** (Latencia)
- **Qué mide**: Tiempo de respuesta en milisegundos
- **Incluye**: Búsqueda vectorial + generación de respuesta con LLM
- **Interpretación**:
  - **Excelente (<2000ms)**: Respuestas rápidas
  - **Aceptable (2000-5000ms)**: Respuestas moderadas
  - **Lenta (>5000ms)**: Considerar optimizaciones

## 🔧 Cómo funciona el registro automático

### Flujo de registro

```
Usuario hace consulta
    ↓
QueryController recibe la petición
    ↓
RAGService procesa la consulta (mide latencia)
    ↓
QueryController registra métricas automáticamente
    ↓
MetricsService guarda en PostgreSQL
```

### Código relevante

**QueryController.java** (registro automático):
```java
@PostMapping
public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
    // 1. Procesa la consulta
    QueryResponse response = ragService.query(request.getQuery(), request.getTopK());
    
    // 2. Registra métricas automáticamente
    int retrievedDocs = response.getSources().size();
    int relevantDocs = response.isHasEvidence() ? retrievedDocs : 0;
    
    metricsService.recordMetrics(
        request.getQuery(),
        retrievedDocs,
        relevantDocs,
        response.getLatencyMs()
    );
    
    return ResponseEntity.ok(response);
}
```

## 📈 Cómo consultar las métricas

### 1. Endpoint de métricas promedio

**GET** `http://localhost:8080/api/query/metrics`

```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/query/metrics"
```

```bash
# Bash/curl
curl http://localhost:8080/api/query/metrics
```

**Respuesta**:
```json
{
  "avg_precision": 0.85,
  "avg_recall": 0.92,
  "avg_latency": 1234.5
}
```

### 2. Consulta SQL directa

```bash
# Ver últimas 10 métricas
docker exec -it rag-postgres psql -U raguser -d ragdb -c "SELECT * FROM evaluation_metrics ORDER BY timestamp DESC LIMIT 10;"

# Ver métricas promedio
docker exec -it rag-postgres psql -U raguser -d ragdb -c "SELECT AVG(precision), AVG(recall), AVG(latency_ms) FROM evaluation_metrics;"

# Ver métricas por consulta específica
docker exec -it rag-postgres psql -U raguser -d ragdb -c "SELECT * FROM evaluation_metrics WHERE query LIKE '%RAG%';"
```

### 3. Script de prueba automatizado

```powershell
# Ejecuta varias consultas y muestra métricas
.\probar-metricas.ps1
```

Este script:
- Ejecuta 5 consultas de prueba
- Registra métricas automáticamente
- Muestra los promedios
- Interpreta los resultados

## 🧪 Experimentos para el Sprint 4

### Comparar diferentes configuraciones

**Experimento 1: Diferentes valores de top-k**
```powershell
# top-k = 3
$body = @{query = "¿Qué es RAG?"; topK = 3} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/query" -Method Post -ContentType "application/json" -Body $body

# top-k = 5
$body = @{query = "¿Qué es RAG?"; topK = 5} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/query" -Method Post -ContentType "application/json" -Body $body

# top-k = 10
$body = @{query = "¿Qué es RAG?"; topK = 10} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/query" -Method Post -ContentType "application/json" -Body $body

# Ver métricas promedio
Invoke-RestMethod -Uri "http://localhost:8080/api/query/metrics"
```

**Experimento 2: Diferentes tamaños de chunk**

1. Modifica `application.yml`:
```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        dimensions: 768
        chunk-size: 500  # Cambia este valor (300, 500, 1000)
```

2. Reinicia el sistema
3. Ejecuta las mismas consultas
4. Compara las métricas

**Experimento 3: Diferentes modelos de embeddings**

1. Modifica `application.yml`:
```yaml
spring:
  ai:
    ollama:
      embedding:
        model: nomic-embed-text  # Prueba otros modelos
```

2. Reinicia el sistema
3. Ejecuta las mismas consultas
4. Compara las métricas

## 📊 Estructura de la tabla evaluation_metrics

```sql
CREATE TABLE evaluation_metrics (
    id SERIAL PRIMARY KEY,
    query TEXT NOT NULL,              -- La pregunta del usuario
    retrieved_docs INTEGER NOT NULL,  -- Documentos recuperados
    relevant_docs INTEGER NOT NULL,   -- Documentos relevantes
    precision DOUBLE PRECISION,       -- Precisión calculada
    recall DOUBLE PRECISION,          -- Recall calculado
    latency_ms BIGINT,               -- Latencia en milisegundos
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- Cuándo se hizo la consulta
);
```

## 🎯 Mejoras futuras

### 1. Recall real (no simplificado)

Para calcular recall correctamente necesitas:
- Un dataset de evaluación con respuestas correctas conocidas
- Saber cuántos documentos relevantes existen en total para cada pregunta

**Ejemplo**:
```java
// Dataset de evaluación
Map<String, Set<String>> groundTruth = Map.of(
    "¿Qué es RAG?", Set.of("documento_rag.txt", "spring_ai_framework.txt"),
    "¿Qué son embeddings?", Set.of("embeddings_vectores.txt", "embeddings_explicacion.txt")
);

// Calcular recall real
Set<String> expectedDocs = groundTruth.get(query);
Set<String> retrievedDocs = response.getSources().stream()
    .map(doc -> doc.getMetadata().get("source"))
    .collect(Collectors.toSet());

int relevantRetrieved = Sets.intersection(expectedDocs, retrievedDocs).size();
double recall = (double) relevantRetrieved / expectedDocs.size();
```

### 2. Métricas adicionales

- **MRR (Mean Reciprocal Rank)**: Posición del primer documento relevante
- **NDCG (Normalized Discounted Cumulative Gain)**: Calidad del ranking
- **F1-Score**: Media armónica de precision y recall
- **Hit Rate**: Porcentaje de consultas con al menos un documento relevante

### 3. Dashboard de métricas

Crear un endpoint que retorne métricas detalladas:
```java
@GetMapping("/metrics/detailed")
public ResponseEntity<MetricsReport> getDetailedMetrics() {
    return ResponseEntity.ok(metricsService.getDetailedReport());
}
```

## 📝 Checklist para el Sprint 4

- [ ] Sistema registra métricas automáticamente
- [ ] Endpoint `/api/query/metrics` funciona
- [ ] Script `probar-metricas.ps1` ejecuta correctamente
- [ ] Realizar experimentos con diferentes top-k (3, 5, 10)
- [ ] Realizar experimentos con diferentes chunk sizes (300, 500, 1000)
- [ ] Documentar resultados en `EXPERIMENTOS.md`
- [ ] Comparar métricas entre configuraciones
- [ ] Identificar la mejor configuración
- [ ] Justificar la elección con datos

## 🔗 Referencias

- [Precision y Recall en IR](https://en.wikipedia.org/wiki/Precision_and_recall)
- [Evaluating RAG Systems](https://www.pinecone.io/learn/rag-evaluation/)
- [Spring AI Metrics](https://docs.spring.io/spring-ai/reference/)
