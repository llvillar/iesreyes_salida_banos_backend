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

## Frontend web

El frontend está en `frontend/` y ofrece un formulario para registrar permisos,
un historial filtrable y una sección de administración para dar de alta,
modificar y eliminar alumnos, profesores, grupos y franjas horarias. Al crear o
editar un alumno se debe seleccionar su grupo. La interfaz es adaptable a
móvil. Necesitas instalar Node.js LTS, que incluye `npm`, y abrir una terminal
nueva después de instalarlo. Comprueba que ambos comandos están disponibles:

```powershell
node --version
npm --version
```

Inicia antes la API (por ejemplo con `docker compose up --build`) y, en otra
terminal, ejecuta desde la carpeta del proyecto:

```powershell
cd frontend
npm install
npm run dev
```

Si PowerShell indica que `npm` no se reconoce, instala Node.js LTS y reinicia
la terminal o IntelliJ para actualizar el `PATH`. Si `npm` está instalado pero
PowerShell bloquea `npm.ps1` por la política de ejecución, usa el lanzador de
Windows:

```powershell
npm.cmd install
npm.cmd run dev
```

Abre la dirección local que indique Vite (normalmente `http://localhost:5173`).
Durante el desarrollo, Vite reenvía las peticiones `/api` a
`http://localhost:8080`.

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

## Entorno local de preproducción

Para probar en el PC una configuración más parecida a producción, Docker Compose
puede iniciar la web compilada y servida por Nginx, la API y PostgreSQL. No hace
falta crear una máquina virtual: Docker aísla cada servicio en su contenedor.
Este entorno usa un volumen separado del entorno de desarrollo y solo publica
la web en el propio PC; la API y la base de datos no se exponen directamente.

En PowerShell, desde la carpeta del proyecto:

```powershell
$env:DB_PASSWORD = "una-clave-local-distinta"
docker compose -f compose.preproduccion.yaml up --build -d
```

Abre `http://localhost:8081`. Nginx sirve el frontend compilado y reenvía las
peticiones `/api` a la API. Para ver el estado y los registros:

```powershell
docker compose -f compose.preproduccion.yaml ps
docker compose -f compose.preproduccion.yaml logs -f
```

Para detenerlo sin borrar los datos:

```powershell
docker compose -f compose.preproduccion.yaml down
```

Para borrar también la base de datos de preproducción y sus datos de prueba:

```powershell
docker compose -f compose.preproduccion.yaml down -v
```

La primera inicialización carga los datos de ejemplo de
`db/recrear-bbdd.sql`. La variable `$env:DB_PASSWORD` solo se aplica a una base
de datos creada por primera vez; cambiarla no modifica la contraseña de un
volumen existente. Esta configuración sirve para pruebas en el PC, no para
publicar el sistema en Internet.

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
| `GET` | `/api/alumnos/{id}` | Consultar un alumno |
| `POST` | `/api/alumnos` | Crear un alumno |
| `PUT` | `/api/alumnos/{id}` | Actualizar un alumno |
| `DELETE` | `/api/alumnos/{id}` | Eliminar un alumno |
| `GET` | `/api/profesores/{id}` | Consultar un profesor |
| `POST` | `/api/profesores` | Crear un profesor |
| `PUT` | `/api/profesores/{id}` | Actualizar un profesor |
| `DELETE` | `/api/profesores/{id}` | Eliminar un profesor |
| `GET` | `/api/grupos/{id}` | Consultar un grupo |
| `POST` | `/api/grupos` | Crear un grupo |
| `PUT` | `/api/grupos/{id}` | Actualizar un grupo |
| `DELETE` | `/api/grupos/{id}` | Eliminar un grupo |
| `GET` | `/api/franjas-horarias/{id}` | Consultar una franja |
| `POST` | `/api/franjas-horarias` | Crear una franja |
| `PUT` | `/api/franjas-horarias/{id}` | Actualizar una franja |
| `DELETE` | `/api/franjas-horarias/{id}` | Eliminar una franja |

Para crear o actualizar un alumno se envía `dni`, `nombre`, `apellidos`,
`email` (opcional) y `grupoId`. Para un profesor se envía `dni`, `nombre`,
`apellidos` y `email` (opcional). Los DNI deben tener ocho cifras y una letra.

Un grupo recibe `curso` (por ejemplo `1º ESO` o `2º Bachillerato`) y `seccion`
(`A` o `B`); la API genera automáticamente el código del grupo. Una franja
recibe `numero` entre 1 y 6; su nombre (`1º Hora` … `6º Hora`) se genera
automáticamente.

Ejemplos para crear recursos:

```json
{
  "dni": "12345678Z",
  "nombre": "Ana",
  "apellidos": "Pérez",
  "email": "ana@example.test",
  "grupoId": 1
}
```

```json
{
  "curso": "1º ESO",
  "seccion": "A"
}
```

```json
{
  "numero": 1
}
```

Si se intenta eliminar un registro utilizado por un permiso u otro registro, o
crear un DNI/código/curso/franja duplicados, la API devuelve HTTP `409`. Un
recurso inexistente devuelve `404`; los datos inválidos devuelven `400`.

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

### Filtrar el historial de permisos

`GET /api/permisos` permite combinar estos parámetros opcionales; si no se
indica ninguno, devuelve el historial completo ordenado por fecha y hora
descendentes:

| Parámetro | Descripción | Ejemplo |
| --- | --- | --- |
| `fecha` | Fecha exacta (`AAAA-MM-DD`) | `fecha=2026-09-28` |
| `desde`, `hasta` | Rango de fechas, ambos extremos incluidos | `desde=2026-09-01&hasta=2026-09-30` |
| `grupo` | Código del grupo | `grupo=1ºA%20ESO` |
| `grupoId` | ID del grupo | `grupoId=1` |
| `alumnoId`, `alumnoDni` | Filtrar por alumno | `alumnoDni=12345678Z` |
| `profesorId`, `profesorDni` | Filtrar por profesor | `profesorId=2` |
| `franjaHorariaId`, `numeroFranja` | Franja por ID o número (1–6) | `numeroFranja=3` |
| `horaDesde`, `horaHasta` | Rango horario inclusivo (`HH:mm:ss`) | `horaDesde=10:00:00&horaHasta=11:00:00` |

Los filtros se pueden combinar. Por ejemplo, para consultar los permisos de un
alumno de un grupo concreto durante septiembre y en la tercera franja:

```text
http://localhost:8080/api/permisos?desde=2026-09-01&hasta=2026-09-30&grupoId=1&alumnoId=1&numeroFranja=3
```

Una fecha inicial posterior a la final, o una hora inicial posterior a la final,
devuelve HTTP `400`.
