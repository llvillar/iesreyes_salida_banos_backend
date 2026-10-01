$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$dbUsername = $env:DB_USERNAME
if ([string]::IsNullOrWhiteSpace($dbUsername)) {
    $dbUsername = "postgres"
}

$sqlPath = Join-Path $PSScriptRoot "..\db\cargar-datos-estadisticas.sql"
Get-Content -Raw -Encoding Ascii $sqlPath | docker compose exec -T db psql `
    -U $dbUsername `
    -d ies_reyes `
    -v ON_ERROR_STOP=1

if ($LASTEXITCODE -ne 0) {
    throw "No se pudieron cargar los datos de prueba. Comprueba que el servicio db de Docker Compose está en ejecución."
}

Write-Output "Datos cargados: 36 alumnos, 12 profesores y 6.000 permisos de prueba entre 2026-01-01 y hoy."
