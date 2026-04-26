# 🚀 Guía de Ejecución - RAG-Esencial

## 📋 Requisitos Previos

### 1. Java 17 o superior
```bash
# Verificar instalación
java -version
```
Si no está instalado: https://adoptium.net/

### 2. Maven 3.6+
```bash
# Verificar instalación
mvn -version
```
Si no está instalado: https://maven.apache.org/download.cgi

### 3. Docker Desktop
```bash
# Verificar instalación
docker --version
docker-compose --version
```
Si no está instalado: https://www.docker.com/products/docker-desktop

### 4. Ollama con modelos
```bash
# Verificar Ollama
curl http://localhost:11434/api/tags

# Instalar modelos necesarios
ollama pull llama2
ollama pull nomic-embed-text
```
Si no está instalado: https://ollama.ai/download

---

## 🎯 Método 1: Ejecución Automática (Recomendado)

### Windows
```cmd
EJECUTAR.cmd
```

Este script:
1. ✅ Verifica todos los requisitos
2. 🐘 Inicia PostgreSQL con pgvector
3. 🚀 Inicia la aplicación Spring Boot
4. 📡 Muestra los endpoints disponibles

---

## 🔧 Método 2: Ejecución Manual (Paso a Paso)

### Paso 1: Iniciar PostgreSQL
```bash
cd RAG-Esencial
docker-compose up -d
```

Verificar que esté corriendo:
```bash
docker ps
```

Deberías ver: `rag-postgres-esencial` en estado `Up`

### Paso 2: Verificar Ollama
```bash
# Verificar que Ollama esté corriendo
curl http://localhost:11434/api/tags

# Si no está corriendo, iniciarlo
ollama serve
```

En otra terminal, verificar modelos:
```bash
ollama list
```

Deberías ver:
- `llama2:latest`
- `nomic-embed-text:latest`

### Paso 3: Iniciar aplicación Spring Boot
```bash
mvn spring-boot:run
```

Espera a ver:
```
Started RagApplication in X.XXX seconds
```

---

## 🧪 Probar el Sistema

### 1. Ingestar documentos
```bash
curl -X POST http://localhost:8080/api/documents/ingest
```

Respuesta esperada:
```json
{
  "message": "Documentos ingestados exitosamente",
  "count": 3
}
```

### 2. Hacer una consulta
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"¿Qué es RAG?\", \"topK\": 3}"
```

Respuesta esperada:
```json
{
  "query": "¿Qué es RAG?",
  "answer": "RAG (Retrieval-Augmented Generation) es...",
  "sources": [
    {
      "content": "...",
      "similarity": 0.95
    }
  ],
  "processingTimeMs": 1234
}
```

### 3. Consultas de ejemplo

**Consulta sobre chunking:**
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"¿Qué es chunking?\", \"topK\": 3}"
```

**Consulta sobre embeddings:**
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"Explica los embeddings\", \"topK\": 3}"
```

**Consulta sobre ventajas:**
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"¿Cuáles son las ventajas de RAG?\", \"topK\": 5}"
```

---

## 🛑 Detener el Sistema

### Método 1: Script automático
```cmd
DETENER.cmd
```

### Método 2: Manual
```bash
# Detener Spring Boot (Ctrl+C en la terminal)

# Detener PostgreSQL
docker-compose down
```

---

## 🔍 Verificar Estado

### PostgreSQL
```bash
# Ver logs
docker logs rag-postgres-esencial

# Conectar a la base de datos
docker exec -it rag-postgres-esencial psql -U rag_user -d rag_db

# Dentro de psql:
\dt                          # Ver tablas
SELECT COUNT(*) FROM vector_store;  # Ver documentos
\q                           # Salir
```

### Ollama
```bash
# Ver modelos instalados
ollama list

# Probar modelo de chat
ollama run llama2 "Hola, ¿cómo estás?"

# Probar modelo de embeddings
curl http://localhost:11434/api/embeddings \
  -d '{"model": "nomic-embed-text", "prompt": "test"}'
```

### Aplicación Spring Boot
```bash
# Ver logs en tiempo real
# (en la terminal donde ejecutaste mvn spring-boot:run)

# Verificar endpoints
curl http://localhost:8080/actuator/health
```

---

## ❌ Solución de Problemas

### Error: "Puerto 5434 ya está en uso"
```bash
# Ver qué está usando el puerto
netstat -ano | findstr :5434

# Detener el contenedor existente
docker stop rag-postgres-esencial
docker rm rag-postgres-esencial

# O cambiar el puerto en docker-compose.yml
ports:
  - "5435:5432"  # Cambiar 5434 por 5435
```

### Error: "Ollama no responde"
```bash
# Verificar que Ollama esté corriendo
curl http://localhost:11434/api/tags

# Si no responde, iniciar Ollama
ollama serve

# En otra terminal, verificar modelos
ollama list
```

### Error: "No se pueden descargar dependencias Maven"
```bash
# Limpiar caché de Maven
mvn clean

# Forzar actualización
mvn clean install -U
```

### Error: "Java version mismatch"
```bash
# Verificar versión de Java
java -version

# Debe ser Java 17 o superior
# Si tienes múltiples versiones, configurar JAVA_HOME
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot
```

### Error: "Cannot connect to PostgreSQL"
```bash
# Verificar que el contenedor esté corriendo
docker ps

# Ver logs del contenedor
docker logs rag-postgres-esencial

# Reiniciar contenedor
docker-compose restart
```

---

## 📊 Puertos Utilizados

| Servicio | Puerto | URL |
|----------|--------|-----|
| Spring Boot | 8080 | http://localhost:8080 |
| PostgreSQL | 5434 | localhost:5434 |
| Ollama | 11434 | http://localhost:11434 |

---

## 📁 Estructura de Archivos

```
RAG-Esencial/
├── EJECUTAR.cmd              ← Ejecutar esto
├── DETENER.cmd               ← Detener servicios
├── GUIA_EJECUCION.md         ← Esta guía
├── docker-compose.yml        ← Configuración PostgreSQL
├── pom.xml                   ← Dependencias Maven
├── src/
│   └── main/
│       ├── java/com/rag/     ← Código fuente
│       └── resources/
│           ├── application.yml  ← Configuración
│           └── documents/       ← Documentos para ingestar
└── init.sql                  ← Script inicial BD
```

---

## 🎓 Próximos Pasos

1. ✅ Ejecutar el proyecto
2. ✅ Probar ingesta de documentos
3. ✅ Hacer consultas de prueba
4. 📝 Agregar tus propios documentos en `src/main/resources/documents/`
5. 🔧 Modificar configuración en `application.yml`
6. 🚀 Experimentar con diferentes consultas

---

## 📚 Recursos Adicionales

- **Spring AI**: https://docs.spring.io/spring-ai/reference/
- **Ollama**: https://ollama.ai/
- **pgvector**: https://github.com/pgvector/pgvector
- **RAG Concepts**: https://www.pinecone.io/learn/retrieval-augmented-generation/

---

¡Listo! Tu sistema RAG está funcionando 🎉
