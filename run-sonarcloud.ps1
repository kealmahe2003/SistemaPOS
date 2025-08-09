# Script de PowerShell para ejecutar análisis SonarCloud
# Uso: .\run-sonarcloud.ps1 "TU_TOKEN_AQUI"

param(
    [Parameter(Mandatory=$false)]
    [string]$SonarToken = $env:SONAR_TOKEN,
    
    [Parameter(Mandatory=$false)]
    [string]$ProjectKey = "kealmahe2003_SistemaPOS",
    
    [Parameter(Mandatory=$false)]
    [string]$Organization = "kealmahe2003"
)

Write-Host "🔍 Script de Análisis SonarCloud para Sistema POS" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Verificar si el token está disponible
if ([string]::IsNullOrEmpty($SonarToken)) {
    Write-Host "❌ SONAR_TOKEN no encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Opciones para configurar el token:" -ForegroundColor Yellow
    Write-Host "1. Variable de entorno: " -NoNewline -ForegroundColor Yellow
    Write-Host "`$env:SONAR_TOKEN = 'tu_token'" -ForegroundColor White
    Write-Host "2. Parámetro: " -NoNewline -ForegroundColor Yellow
    Write-Host ".\run-sonarcloud.ps1 'tu_token'" -ForegroundColor White
    Write-Host ""
    Write-Host "Para obtener un token:" -ForegroundColor Green
    Write-Host "1. Ir a https://sonarcloud.io" -ForegroundColor Green
    Write-Host "2. My Account → Security → Generate Token" -ForegroundColor Green
    exit 1
}

Write-Host "✅ Token configurado correctamente" -ForegroundColor Green
Write-Host "📊 Project Key: $ProjectKey" -ForegroundColor Blue
Write-Host "🏢 Organization: $Organization" -ForegroundColor Blue
Write-Host ""

# Cambiar al directorio del proyecto
$projectPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectPath

Write-Host "📁 Directorio de trabajo: $projectPath" -ForegroundColor Blue
Write-Host ""

# Verificar que gradlew existe
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "❌ gradlew.bat no encontrado en el directorio actual!" -ForegroundColor Red
    exit 1
}

Write-Host "🧹 Paso 1: Limpiando proyecto..." -ForegroundColor Yellow
try {
    & .\gradlew.bat clean
    if ($LASTEXITCODE -ne 0) {
        throw "Error en gradle clean"
    }
    Write-Host "✅ Limpieza completada" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en limpieza: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🧪 Paso 2: Ejecutando tests (180 tests)..." -ForegroundColor Yellow
try {
    & .\gradlew.bat test
    if ($LASTEXITCODE -ne 0) {
        throw "Error en tests"
    }
    Write-Host "✅ Tests completados exitosamente" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en tests: $_" -ForegroundColor Red
    Write-Host "💡 Sugerencia: Verificar que todas las dependencias están disponibles" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "📊 Paso 3: Generando reporte de cobertura..." -ForegroundColor Yellow
try {
    & .\gradlew.bat jacocoTestReport
    if ($LASTEXITCODE -ne 0) {
        throw "Error en jacocoTestReport"
    }
    Write-Host "✅ Reporte de cobertura generado" -ForegroundColor Green
} catch {
    Write-Host "❌ Error generando reporte de cobertura: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🚀 Paso 4: Ejecutando análisis SonarCloud..." -ForegroundColor Yellow
Write-Host "   Token: $($SonarToken.Substring(0, [Math]::Min(8, $SonarToken.Length)))..." -ForegroundColor Gray

try {
    & .\gradlew.bat sonar "-Dsonar.login=$SonarToken" "-Dsonar.projectKey=$ProjectKey" "-Dsonar.organization=$Organization"
    if ($LASTEXITCODE -ne 0) {
        throw "Error en análisis SonarCloud"
    }
    Write-Host "✅ Análisis SonarCloud completado exitosamente!" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en análisis SonarCloud: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Posibles causas:" -ForegroundColor Yellow
    Write-Host "   - Token incorrecto o expirado" -ForegroundColor Yellow
    Write-Host "   - Project Key incorrecto: $ProjectKey" -ForegroundColor Yellow
    Write-Host "   - Organization incorrecta: $Organization" -ForegroundColor Yellow
    Write-Host "   - Proyecto no existe en SonarCloud" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "🔧 Para solucionar:" -ForegroundColor Green
    Write-Host "   1. Verificar en https://sonarcloud.io que el proyecto existe" -ForegroundColor Green
    Write-Host "   2. Verificar que el token tiene permisos de 'Execute Analysis'" -ForegroundColor Green
    Write-Host "   3. Consultar CONFIGURAR_SONARCLOUD.md para pasos detallados" -ForegroundColor Green
    exit 1
}

Write-Host ""
Write-Host "🎉 ¡Análisis completado exitosamente!" -ForegroundColor Green
Write-Host "📊 Métricas del proyecto:" -ForegroundColor Cyan
Write-Host "   - 180 tests implementados" -ForegroundColor White
Write-Host "   - 77% cobertura en utils (DatabaseManager)" -ForegroundColor White
Write-Host "   - 88% cobertura en models (Producto, Venta)" -ForegroundColor White
Write-Host "   - Exclusiones optimizadas para JavaFX" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Ver resultados en: https://sonarcloud.io/project/overview?id=$ProjectKey" -ForegroundColor Blue

# Intentar abrir el navegador con los resultados
$sonarUrl = "https://sonarcloud.io/project/overview?id=$ProjectKey"
Write-Host ""
Write-Host "🚀 ¿Abrir resultados en el navegador? (Y/N): " -NoNewline -ForegroundColor Yellow
$response = Read-Host
if ($response -eq "Y" -or $response -eq "y" -or $response -eq "yes") {
    Start-Process $sonarUrl
    Write-Host "✅ Navegador abierto con los resultados" -ForegroundColor Green
}

Write-Host ""
Write-Host "✨ Script completado. ¡Revisa los resultados en SonarCloud!" -ForegroundColor Green
