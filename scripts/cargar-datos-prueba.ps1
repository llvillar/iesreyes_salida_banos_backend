$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$dbUsername = $env:DB_USERNAME
if ([string]::IsNullOrWhiteSpace($dbUsername)) {
    $dbUsername = "postgres"
}

docker compose exec -T db psql `
    -U $dbUsername `
    -d ies_reyes `
    -v ON_ERROR_STOP=1 `
    -f /scripts/02-datos-prueba.sql
if ($LASTEXITCODE -ne 0) {
    throw "No se pudieron cargar los datos. Comprueba que PostgreSQL está iniciado y ejecuta primero .\scripts\poner-a-punto.ps1."
}

Write-Output "Carga completada: 50 alumnos, 10 profesores y 5.000 permisos desde el 1 de enero de este año hasta hoy."
