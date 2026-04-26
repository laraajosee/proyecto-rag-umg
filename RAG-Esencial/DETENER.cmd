@echo off
chcp 65001 > nul
echo ========================================
echo 🛑 RAG-Esencial - Detener Servicios
echo ========================================
echo.

echo Deteniendo contenedores Docker...
docker-compose down

echo.
echo ✅ Servicios detenidos
echo.
pause
