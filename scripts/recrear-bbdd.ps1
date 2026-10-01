$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$confirmacion = Read-Host "Esto borrará las tablas, cuentas y datos de ies_reyes y cargará datos de prueba. Escribe RECREAR para continuar"
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
    -f /scripts/esquema.sql

if ($LASTEXITCODE -ne 0) {
    throw "No se pudo recrear el esquema. Comprueba que el servicio db de Docker Compose está en ejecución."
}

docker compose exec -T db psql `
    -U $dbUsername `
    -d ies_reyes `
    -v ON_ERROR_STOP=1 `
    -f /scripts/datos-prueba.sql

if ($LASTEXITCODE -ne 0) {
    throw "No se pudieron cargar los datos de prueba."
}

Write-Output "Base recreada con esquema, catálogos y datos de prueba."
