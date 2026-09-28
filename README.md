# IES Reyes: permisos de baño

API REST desarrollada con Spring Boot y PostgreSQL para registrar autorizaciones
de salida al baño. Alumnos, profesores, grupos y franjas se guardan en tablas
separadas; cada permiso referencia esos registros mediante claves foráneas.

## Modelo de datos

- `alumnos`: DNI, nombre, apellidos, correo opcional y grupo.
- `profesores`: DNI, nombre, apellidos y correo opcional.
- `grupos`: curso, sección y código único. El catálogo incluye secciones A y B
  para 1.º–4.º ESO y 1.º–2.º Bachillerato.
- `franjas_horarias`: seis valores fijos, de `1º Hora` a `6º Hora`.
- `permisos_bano`: fecha, hora y referencias a alumno, profesor y franja. El
  grupo del permiso se obtiene del grupo asignado al alumno.

Los nombres, DNI y correos incluidos en los ejemplos son ficticios.

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

Al crear un volumen nuevo, PostgreSQL ejecuta `db/recrear-bbdd.sql`; crea las
tablas normalizadas y carga grupos, seis franjas, doce alumnos y seis permisos
de prueba. Para migrar el esquema antiguo en un volumen existente y conservar
los permisos que ya tenga, abre PowerShell en el proyecto y ejecuta:

```powershell
.\scripts\migrar-bbdd-normalizada.ps1
```

La migración solicita confirmación, detiene la API mientras modifica el esquema
y conserva los permisos existentes. Se ejecuta una sola vez sobre la base
antigua.

Para borrar y recrear **todos** los datos de la base Docker con los ejemplos,
ejecuta `.\scripts\recrear-bbdd.ps1`; el script solicita confirmación. Esta
acción elimina los datos existentes de `ies_reyes`. No es necesaria para una
migración normal. También se puede borrar el volumen completo con
`docker compose down -v`.

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
Invoke-RestMethod "http://localhost:8080/api/alumnos"
Invoke-RestMethod "http://localhost:8080/api/profesores"
Invoke-RestMethod "http://localhost:8080/api/grupos"
Invoke-RestMethod "http://localhost:8080/api/franjas-horarias"
```

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/permisos` | Crear un permiso |
| `GET` | `/api/permisos` | Listar permisos; admite `fecha` (`AAAA-MM-DD`) y `grupo` |
| `GET` | `/api/permisos/{id}` | Consultar un permiso |
| `PUT` | `/api/permisos/{id}` | Actualizar los datos del permiso |
| `DELETE` | `/api/permisos/{id}` | Eliminar un permiso |
| `GET` | `/api/alumnos` | Consultar alumnos y su grupo |
| `GET` | `/api/profesores` | Consultar profesores |
| `GET` | `/api/grupos` | Consultar grupos |
| `GET` | `/api/franjas-horarias` | Consultar las seis franjas |

Para crear o actualizar un permiso, el cliente envía los identificadores
obtenidos de los catálogos:

```json
{
  "alumnoId": 1,
  "profesorId": 1,
  "franjaHorariaId": 3,
  "fecha": "2026-09-28",
  "hora": "10:15:00"
}
```

La respuesta incluye los datos relacionados del alumno, su grupo, profesor y
franja. Una solicitud inválida devuelve HTTP `400`; un identificador inexistente
devuelve HTTP `404`.
