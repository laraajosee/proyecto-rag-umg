@echo off
chcp 65001 > nul
echo ========================================
echo 🚀 RAG-Esencial - Ejecutar Proyecto
echo ========================================
echo.

REM Verificar Java
echo [1/4] Verificando Java...
java -version > nul 2>&1
if errorlevel 1 (
    echo ❌ ERROR: Java no está instalado o no está en PATH
    echo.
    echo Instala Java 17 desde: https://adoptium.net/
    pause
    exit /b 1
)
echo ✅ Java encontrado

REM Verificar Docker
echo.
echo [2/4] Verificando Docker...
docker --version > nul 2>&1
if errorlevel 1 (
    echo ❌ ERROR: Docker no está instalado o no está corriendo
    echo.
    echo Instala Docker Desktop desde: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo ✅ Docker encontrado

REM Verificar Ollama
echo.
echo [3/4] Verificando Ollama...
curl -s http://localhost:11434/api/tags > nul 2>&1
if errorlevel 1 (
    echo ⚠️  ADVERTENCIA: Ollama no está corriendo en localhost:11434
    echo.
    echo Asegúrate de:
    echo 1. Instalar Ollama desde: https://ollama.ai/download
    echo 2. Ejecutar: ollama pull llama2
    echo 3. Ejecutar: ollama pull nomic-embed-text
    echo.
    set /p continuar="¿Deseas continuar de todas formas? (S/N): "
    if /i not "%continuar%"=="S" exit /b 1
) else (
    echo ✅ Ollama está corriendo
)

REM Iniciar PostgreSQL
echo.
echo [4/4] Iniciando PostgreSQL con pgvector...
docker-compose up -d

echo.
echo ⏳ Esperando que PostgreSQL esté listo...
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo 🎯 Iniciando aplicación Spring Boot...
echo ========================================
echo.
echo La aplicación estará disponible en:
echo 👉 http://localhost:8080
echo.
echo Endpoints disponibles:
echo   POST http://localhost:8080/api/documents/ingest
echo   POST http://localhost:8080/api/query
echo.
echo Presiona Ctrl+C para detener
echo ========================================
echo.

REM Iniciar aplicación
mvn spring-boot:run

pause
