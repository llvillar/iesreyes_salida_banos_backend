$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$confirmacion = Read-Host "La migración cambiará el esquema y conservará los permisos existentes. Escribe MIGRAR para continuar"
if ($confirmacion -cne "MIGRAR") {
    Write-Output "Operación cancelada. No se han modificado los datos."
    exit 0
}

$dbUsername = $env:DB_USERNAME
if ([string]::IsNullOrWhiteSpace($dbUsername)) {
    $dbUsername = "postgres"
}

docker compose stop api
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo detener la API."
}

$migracionCompletada = $false
try {
    docker compose up -d db
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo iniciar PostgreSQL."
    }

    $baseLista = $false
    for ($intento = 0; $intento -lt 30; $intento++) {
        docker compose exec -T db pg_isready -U $dbUsername -d ies_reyes *> $null
        if ($LASTEXITCODE -eq 0) {
            $baseLista = $true
            break
        }
        Start-Sleep -Seconds 2
    }
    if (-not $baseLista) {
        throw "PostgreSQL no quedó disponible."
    }

    docker compose exec -T db psql `
        -U $dbUsername `
        -d ies_reyes `
        -v ON_ERROR_STOP=1 `
        -f /scripts/migrar-bbdd-normalizada.sql
    if ($LASTEXITCODE -ne 0) {
        throw "La migración SQL falló."
    }

    $migracionCompletada = $true
    docker compose up -d --build api
    if ($LASTEXITCODE -ne 0) {
        throw "La base se migró, pero no se pudo iniciar la API actualizada."
    }
} catch {
    if (-not $migracionCompletada) {
        docker compose start api
        throw "La migración falló y se revirtió. Se intentó iniciar la API anterior. Detalle: $_"
    }
    throw
}

Write-Output "Migración completada. Los permisos existentes se conservaron."
