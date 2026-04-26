# Guía de Experimentos (Sprint 4)

Para cumplir con los requisitos del proyecto, debes realizar 3 experimentos comparativos.

## 📊 Sistema de Métricas Automático

**¡IMPORTANTE!** El sistema ahora registra métricas automáticamente en cada consulta:

- **Precision**: Proporción de documentos relevantes entre los recuperados
- **Recall**: Proporción de documentos relevantes encontrados
- **Latency**: Tiempo de respuesta en milisegundos

### Cómo consultar las métricas

```powershell
# Ver métricas promedio
Invoke-RestMethod -Uri "http://localhost:8080/api/query/metrics"

# Ejecutar script de prueba automatizado
.\probar-metricas.ps1
```

Ver **GUIA_METRICAS.md** para más detalles sobre el sistema de métricas.

## Experimento 1: Tamaño de Chunks

### Configuración A (Baseline)
```yaml
rag:
  chunk-size: 500
  chunk-overlap: 50
```

### Configuración B
```yaml
rag:
  chunk-size: 300
  chunk-overlap: 30
```

### Pasos:
1. Modifica `application.yml` con Configuración A
2. Reinicia la app: `mvn spring-boot:run`
3. Ingesta documentos: `POST /api/documents/ingest`
4. Ejecuta queries de prueba: `.\probar-metricas.ps1`
5. Consulta métricas: `GET /api/query/metrics`
6. Guarda resultados en una tabla
7. Repite con Configuración B
8. Compara resultados

### Métricas a Comparar:
- Precision@k promedio
- Recall@k promedio
- Latencia promedio
- Calidad de respuestas (evaluación manual)

---

## Experimento 2: Valor de Top-K

### Configuración A (Baseline)
```yaml
rag:
  top-k: 5
```

### Configuración B
```yaml
rag:
  top-k: 3
```

### Pasos:
1. Usa el mismo dataset indexado
2. Modifica solo el valor de `top-k` en `application.yml`
3. Reinicia la app
4. Ejecuta queries: `.\probar-metricas.ps1`
5. Consulta métricas: `GET /api/query/metrics`
6. Compara resultados

### Hipótesis:
- Top-K menor → Más rápido, pero puede perder contexto
- Top-K mayor → Más contexto, pero más ruido y latencia

---

## Experimento 3: Modelos de Embedding

### Configuración A (Baseline)
```yaml
spring:
  ai:
    openai:
      embedding:
        options:
          model: text-embedding-ada-002
```

### Configuración B
```yaml
spring:
  ai:
    openai:
      embedding:
        options:
          model: text-embedding-3-small
```

### Pasos:
1. Cambia el modelo en `application.yml`
2. **IMPORTANTE**: Debes re-ingestar los documentos (los embeddings cambian)
3. Ejecuta queries: `.\probar-metricas.ps1`
4. Consulta métricas: `GET /api/query/metrics`
5. Compara resultados

### Consideraciones:
- `text-embedding-ada-002`: Modelo clásico, 1536 dimensiones
- `text-embedding-3-small`: Más nuevo, más eficiente
- Debes actualizar `dimensions` en la configuración si cambias el modelo

---

## Queries de Prueba Sugeridas

Crea un archivo `test-queries.json`:

```json
[
  {"query": "¿Qué es RAG?", "expected_source": "documento_rag.pdf"},
  {"query": "¿Cómo funciona el chunking?", "expected_source": "documento_chunking.pdf"},
  {"query": "Explica los embeddings", "expected_source": "documento_embeddings.pdf"},
  {"query": "¿Qué es pgvector?", "expected_source": "documento_pgvector.pdf"},
  {"query": "¿Cómo se evalúa un sistema RAG?", "expected_source": "documento_evaluacion.pdf"}
]
```

---

## Script de Evaluación Automática

Usa el script `probar-metricas.ps1` para ejecutar consultas automáticamente:

```powershell
# Ejecuta 5 consultas de prueba y muestra métricas
.\probar-metricas.ps1
```

O ejecuta consultas individuales:

```powershell
# Consulta individual
$body = @{query = "¿Qué es RAG?"; topK = 5} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/query" -Method Post -ContentType "application/json" -Body $body

# Ver métricas promedio
Invoke-RestMethod -Uri "http://localhost:8080/api/query/metrics"

# Ver métricas en la base de datos
docker exec -it rag-postgres psql -U raguser -d ragdb -c "SELECT * FROM evaluation_metrics ORDER BY timestamp DESC LIMIT 10;"
```

---

## Tabla de Resultados Esperada

| Experimento | Precision@5 | Recall@5 | Latency (ms) | Costo ($) |
|-------------|-------------|----------|--------------|-----------|
| Chunk 500   | 0.85        | 0.92     | 1200         | 0.002     |
| Chunk 300   | 0.82        | 0.88     | 1100         | 0.0025    |
| Top-K 5     | 0.85        | 0.92     | 1200         | 0.002     |
| Top-K 3     | 0.80        | 0.85     | 900          | 0.0015    |
| Ada-002     | 0.85        | 0.92     | 1200         | 0.002     |
| 3-small     | 0.87        | 0.94     | 1000         | 0.0015    |

---

## Decisión Final Justificada

Ejemplo de conclusión:

> **Configuración Óptima Seleccionada:**
> - Chunk size: 300 caracteres
> - Top-K: 5
> - Modelo: text-embedding-3-small
>
> **Justificación:**
> - Chunk 300 reduce latencia sin pérdida significativa de precisión
> - Top-K 5 ofrece mejor balance entre contexto y ruido
> - text-embedding-3-small es más eficiente y económico
> - Costo total estimado: $0.0015 por query
> - Latencia promedio: 1000ms (aceptable para producción)

---

## Reporte en el Documento Final

Incluye:
1. Tabla comparativa
2. Gráficos (precision vs latency)
3. Ejemplos de respuestas con cada configuración
4. Análisis de trade-offs
5. Decisión final justificada
