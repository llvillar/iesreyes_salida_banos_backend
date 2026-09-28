# IES Reyes: permisos de baño

API REST desarrollada con Spring Boot y PostgreSQL para registrar autorizaciones
de salida al baño. Cada permiso guarda alumno, fecha, franja horaria, hora,
grupo y profesor.

## Configuración

Crea una base de datos PostgreSQL llamada `ies_reyes` y configura estas variables
de entorno para tu instalación:

| Variable | Valor predeterminado |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/ies_reyes` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |

Al iniciar, Hibernate crea o actualiza la tabla `permisos_bano`. En desarrollo
se puede ejecutar la aplicación desde IntelliJ o con `mvnw spring-boot:run`.
Las pruebas usan H2 en memoria y no requieren una instancia de PostgreSQL.

## Ejecutar con Docker

Necesitas Docker Desktop abierto. Desde la carpeta del proyecto, ejecuta:

```bash
docker compose up --build
```

Compose inicia PostgreSQL y la API; la API queda disponible en
`http://localhost:8080`. La base usa un volumen Docker para conservar los datos
al detener los contenedores. Para parar la aplicación, pulsa `Ctrl+C` y ejecuta
`docker compose down`. Para borrar también la base y sus datos:
`docker compose down -v`.

En la primera inicialización, PostgreSQL ejecuta `db/recrear-bbdd.sql`, que crea
la tabla y carga seis permisos de ejemplo con la fecha actual. Para volver a
crear la tabla y recargar esos ejemplos, con el servicio `db` en ejecución abre
PowerShell y ejecuta:

```powershell
.\scripts\recrear-bbdd.ps1
```

El script solicita confirmación y reemplaza todos los permisos existentes por
los seis registros de prueba. No elimina el volumen ni otras bases de datos.
PostgreSQL solo ejecuta automáticamente los scripts de inicialización la
primera vez que se crea el volumen.

Las credenciales predeterminadas (`postgres`) son solo para desarrollo local.
Puedes personalizarlas definiendo `DB_USERNAME` y `DB_PASSWORD` en el entorno
antes de ejecutar Compose. Por ejemplo, en PowerShell:

```powershell
$env:DB_PASSWORD = "cambia-esta-clave"
docker compose up --build
```

Para consultar los permisos cargados, incluidos los de hoy:

```powershell
Invoke-RestMethod "http://localhost:8080/api/permisos"
Invoke-RestMethod "http://localhost:8080/api/permisos?fecha=$(Get-Date -Format yyyy-MM-dd)"
```

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/permisos` | Crear un permiso |
| `GET` | `/api/permisos` | Listar permisos; admite `fecha` (`AAAA-MM-DD`) y `grupo` |
| `GET` | `/api/permisos/{id}` | Consultar un permiso |
| `PUT` | `/api/permisos/{id}` | Actualizar los datos del permiso |
| `DELETE` | `/api/permisos/{id}` | Eliminar un permiso |

Ejemplo de cuerpo para `POST` y `PUT`:

```json
{
  "alumno": "Ana Pérez",
  "fecha": "2026-09-28",
  "franjaHoraria": "3ª hora",
  "hora": "10:15:00",
  "grupo": "2ºB",
  "profesor": "Laura García"
}
```

Una solicitud inválida devuelve HTTP `400` con los errores de validación; un id
inexistente devuelve HTTP `404`.
