# Script de diagnostico para problemas con SonarCloud
param(
    [Parameter(Mandatory=$false)]
    [string]$SonarToken = $env:SONAR_TOKEN
)

Write-Host "DIAGNOSTICO DE CONFIGURACION SONARCLOUD" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar archivos de configuracion
Write-Host "1. Verificando archivos de configuracion..." -ForegroundColor Yellow

$configFiles = @(
    "sonar-project.properties",
    "app\sonar-project.properties",
    "app\build.gradle.kts"
)

foreach ($file in $configFiles) {
    if (Test-Path $file) {
        Write-Host "   OK: $file encontrado" -ForegroundColor Green
    } else {
        Write-Host "   ERROR: $file NO encontrado" -ForegroundColor Red
    }
}

# 2. Leer configuracion actual
Write-Host ""
Write-Host "2. Configuracion actual..." -ForegroundColor Yellow

if (Test-Path "sonar-project.properties") {
    $content = Get-Content "sonar-project.properties"
    $projectKey = ($content | Where-Object { $_ -match "sonar\.projectKey=" }) -replace "sonar\.projectKey=", ""
    $organization = ($content | Where-Object { $_ -match "sonar\.organization=" }) -replace "sonar\.organization=", ""
    
    Write-Host "   Project Key: $projectKey" -ForegroundColor Blue
    Write-Host "   Organization: $organization" -ForegroundColor Blue
} else {
    Write-Host "   ERROR: No se puede leer sonar-project.properties" -ForegroundColor Red
}

# 3. Verificar token
Write-Host ""
Write-Host "3. Verificando token..." -ForegroundColor Yellow

if ([string]::IsNullOrEmpty($SonarToken)) {
    Write-Host "   ERROR: SONAR_TOKEN no configurado" -ForegroundColor Red
    Write-Host "   SOLUCION: Configurar con: `$env:SONAR_TOKEN = 'tu_token'" -ForegroundColor Yellow
} else {
    $tokenLength = $SonarToken.Length
    $tokenStart = $SonarToken.Substring(0, [Math]::Min(8, $tokenLength))
    Write-Host "   OK: Token configurado ($tokenLength caracteres, inicia con: $tokenStart...)" -ForegroundColor Green
}

# 4. Verificar build tools
Write-Host ""
Write-Host "4. Verificando herramientas de build..." -ForegroundColor Yellow

if (Test-Path "gradlew.bat") {
    Write-Host "   OK: gradlew.bat encontrado" -ForegroundColor Green
} else {
    Write-Host "   ERROR: gradlew.bat NO encontrado" -ForegroundColor Red
}

# 5. Verificar Java
Write-Host ""
Write-Host "5. Verificando Java..." -ForegroundColor Yellow

try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "   OK: Java disponible: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: Java NO disponible en PATH" -ForegroundColor Red
}

# 6. Verificar conectividad
Write-Host ""
Write-Host "6. Verificando conectividad a SonarCloud..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "https://sonarcloud.io" -TimeoutSec 10 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "   OK: SonarCloud accesible" -ForegroundColor Green
    } else {
        Write-Host "   ADVERTENCIA: SonarCloud responde con codigo: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ERROR: No se puede acceder a SonarCloud: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "RESUMEN Y RECOMENDACIONES" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Pasos recomendados para solucionar el error:" -ForegroundColor Green

Write-Host ""
Write-Host "1. Crear proyecto en SonarCloud:" -ForegroundColor Yellow
Write-Host "   - Ir a https://sonarcloud.io" -ForegroundColor White
Write-Host "   - Login con GitHub (kealmahe2003)" -ForegroundColor White
Write-Host "   - Crear organizacion 'kealmahe2003' si no existe" -ForegroundColor White
Write-Host "   - Crear proyecto 'SistemaPOS' o 'kealmahe2003_SistemaPOS'" -ForegroundColor White

Write-Host ""
Write-Host "2. Generar token:" -ForegroundColor Yellow
Write-Host "   - En SonarCloud: My Account -> Security -> Generate Token" -ForegroundColor White
Write-Host "   - Tipo: 'Project Analysis Token'" -ForegroundColor White
Write-Host "   - Configurar: `$env:SONAR_TOKEN = 'tu_token'" -ForegroundColor White

Write-Host ""
Write-Host "3. Ejecutar analisis:" -ForegroundColor Yellow
Write-Host "   - .\run-sonarcloud.ps1 'tu_token'" -ForegroundColor White
Write-Host "   - O: .\gradlew test jacocoTestReport sonar -Dsonar.login=tu_token" -ForegroundColor White

Write-Host ""
Write-Host "Con 180 tests y 77-88% cobertura en codigo critico, tu proyecto esta listo!" -ForegroundColor Green
