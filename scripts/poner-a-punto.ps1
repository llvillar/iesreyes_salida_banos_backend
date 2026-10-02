$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$confirmacion = Read-Host "Esto borrará el volumen y todos los datos de ies_reyes. Escribe RECREAR para continuar"
if ($confirmacion -cne "RECREAR") {
    Write-Output "Operación cancelada. No se han modificado los datos."
    exit 0
}

$dbUsername = $env:DB_USERNAME
if ([string]::IsNullOrWhiteSpace($dbUsername)) {
    $dbUsername = "postgres"
}
$passwordHash = '$2a$10$upNxQwGAQUV/oqh4A6aJEOoTEgjgLPDrDz9At2ugfqItUy7uCO9cy'

docker compose down -v
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo detener Compose y borrar el volumen de desarrollo."
}

docker compose up -d db
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo iniciar PostgreSQL. Comprueba Docker Desktop."
}

$baseLista = $false
for ($intento = 0; $intento -lt 60; $intento++) {
    docker compose exec -T db pg_isready -U $dbUsername -d ies_reyes *> $null
    if ($LASTEXITCODE -eq 0) {
        $baseLista = $true
        break
    }
    Start-Sleep -Seconds 2
}
if (-not $baseLista) {
    throw "PostgreSQL no quedó disponible. Consulta los registros con docker compose logs db."
}

docker compose exec -T db psql `
    -U $dbUsername `
    -d ies_reyes `
    -v ON_ERROR_STOP=1 `
    -c "UPDATE profesores SET nombre = 'Luis', apellidos = 'Villar', email = 'llvillar@gmail.com' WHERE dni = '00000001A'; INSERT INTO usuarios_app (email, contrasena_hash, perfil, profesor_id) SELECT 'llvillar@gmail.com', '$passwordHash', 'GESTION_CATALOGOS', id FROM profesores WHERE dni = '00000001A';"
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo crear el profesor y la cuenta de desarrollo."
}

docker compose up -d --build api
if ($LASTEXITCODE -ne 0) {
    throw "La base está lista, pero no se pudo iniciar la API."
}

Write-Output "Base de datos recreada. Acceso local: llvillar@gmail.com / 1234. Ejecuta .\scripts\cargar-datos-prueba.ps1 para cargar 5.000 permisos históricos."
