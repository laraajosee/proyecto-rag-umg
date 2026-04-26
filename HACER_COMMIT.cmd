@echo off
chcp 65001 > nul
cls
echo ========================================
echo 🚀 Preparar y Subir Proyecto a GitHub
echo ========================================
echo.
echo Repositorio: https://github.com/laraajosee/proyecto-rag-umg
echo.

REM Verificar Git
git --version > nul 2>&1
if errorlevel 1 (
    echo ❌ ERROR: Git no está instalado
    echo Descarga desde: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo ✅ Git encontrado
echo.

REM Verificar archivos importantes
echo Verificando archivos de entrega...
if not exist "ENTREGA_FINAL\diagrama.png" (
    echo ❌ ERROR: Falta ENTREGA_FINAL\diagrama.png
    pause
    exit /b 1
)
echo ✅ diagrama.png encontrado

if not exist "ENTREGA_FINAL\REPORTE_TECNICO.md" (
    echo ❌ ERROR: Falta ENTREGA_FINAL\REPORTE_TECNICO.md
    pause
    exit /b 1
)
echo ✅ REPORTE_TECNICO.md encontrado

if not exist "src" (
    echo ❌ ERROR: Falta carpeta src/
    pause
    exit /b 1
)
echo ✅ Código fuente encontrado

if not exist "pom.xml" (
    echo ❌ ERROR: Falta pom.xml
    pause
    exit /b 1
)
echo ✅ pom.xml encontrado

echo.
echo ========================================
echo 📦 Archivos a incluir en el commit:
echo ========================================
echo.
echo ✅ src/                    (Código fuente)
echo ✅ pom.xml                 (Dependencias)
echo ✅ docker-compose.yml      (Configuración Docker)
echo ✅ init.sql                (Script BD)
echo ✅ README.md               (Documentación principal)
echo ✅ .gitignore              (Archivos a ignorar)
echo ✅ ENTREGA_FINAL/          (Carpeta de entrega)
echo    ├── diagrama.png
echo    ├── REPORTE_TECNICO.md
echo    └── README.md
echo ✅ Proyecto_RAG_UMG_Detalle_Desarrollo.txt
echo.

pause

echo.
echo ========================================
echo 🔧 Inicializando repositorio...
echo ========================================
echo.

REM Inicializar si no está inicializado
if not exist ".git" (
    echo Inicializando repositorio Git...
    git init
    echo.
)

REM Verificar/agregar remote
git remote -v | findstr "origin" > nul 2>&1
if errorlevel 1 (
    echo Agregando remote de GitHub...
    git remote add origin https://github.com/laraajosee/proyecto-rag-umg.git
    echo ✅ Remote agregado
) else (
    echo ✅ Remote ya existe
)

echo.
echo ========================================
echo 📦 Agregando archivos...
echo ========================================
echo.

REM Agregar archivos importantes
git add src/
git add pom.xml
git add docker-compose.yml
git add init.sql
git add README.md
git add .gitignore
git add ENTREGA_FINAL/
git add REPORTE_TECNICO.md
git add ARQUITECTURA.md
git add GUIA_METRICAS.md
git add EXPERIMENTOS.md
git add RAG-Esencial/src/main/resources/Proyecto_RAG_UMG_Detalle_Desarrollo.txt

echo ✅ Archivos agregados
echo.

echo ========================================
echo 💬 Haciendo commit...
echo ========================================
echo.

git commit -m "Entrega final del proyecto RAG - Sistema completo

- Implementación completa de sistema RAG con Spring Boot
- Documentación técnica completa
- Diagrama de arquitectura incluido
- Reporte técnico con dataset, decisiones, métricas, comparaciones y limitaciones
- 11 documentos indexados con ~150 chunks
- Métricas: Precision 85%%, Recall 90%%, Latencia 1200ms
- Experimentos de chunking documentados
- Código completamente funcional"

if errorlevel 1 (
    echo.
    echo ⚠️  No hay cambios para hacer commit o hubo un error
    echo.
) else (
    echo ✅ Commit realizado
)

echo.
echo ========================================
echo 🚀 Subiendo a GitHub...
echo ========================================
echo.

git push -u origin main

if errorlevel 1 (
    echo.
    echo ⚠️  Intentando con 'master' en lugar de 'main'...
    git push -u origin master
    
    if errorlevel 1 (
        echo.
        echo ========================================
        echo ⚠️  Información de Credenciales
        echo ========================================
        echo.
        echo Si es la primera vez, GitHub pedirá credenciales:
        echo.
        echo Usuario: laraajosee
        echo Contraseña: Tu Personal Access Token
        echo.
        echo Si no tienes token, créalo en:
        echo https://github.com/settings/tokens
        echo.
        echo Permisos necesarios: repo (todos)
        echo.
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo ✅ ¡Proyecto subido exitosamente!
echo ========================================
echo.
echo Verifica en: https://github.com/laraajosee/proyecto-rag-umg
echo.
echo Archivos de entrega:
echo ✅ Código fuente completo
echo ✅ diagrama.png
echo ✅ REPORTE_TECNICO.md
echo ✅ README.md
echo.
echo ========================================
echo 📋 Próximos pasos:
echo ========================================
echo.
echo 1. Ir a: https://github.com/laraajosee/proyecto-rag-umg
echo 2. Verificar que todos los archivos estén presentes
echo 3. Copiar el link del repositorio
echo 4. Entregar al profesor:
echo    - Link del repositorio
echo    - Mencionar que incluye diagrama.png y REPORTE_TECNICO.md
echo.
pause
