$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$confirmacion = Read-Host "Esto borrará los permisos existentes y cargará datos de prueba. Escribe RECREAR para continuar"
if ($confirmacion -cne "RECREAR") {
    Write-Output "Operación cancelada. No se han modificado los datos."
    exit 0
}

$dbUsername = $env:DB_USERNAME
if ([string]::IsNullOrWhiteSpace($dbUsername)) {
    $dbUsername = "postgres"
}

docker compose exec -T db psql `
    -U $dbUsername `
    -d ies_reyes `
    -v ON_ERROR_STOP=1 `
    -f /scripts/recrear-bbdd.sql

if ($LASTEXITCODE -ne 0) {
    throw "No se pudo recrear la tabla. Comprueba que el servicio db de Docker Compose está en ejecución."
}

Write-Output "Base recreada con catálogos, 12 alumnos y seis permisos de prueba."
