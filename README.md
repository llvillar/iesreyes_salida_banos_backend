# IES Reyes: permisos de baño

API REST desarrollada con Spring Boot y PostgreSQL para registrar autorizaciones
de salida al baño. Alumnos, profesores, grupos y franjas se guardan en tablas
separadas; cada permiso referencia esos registros mediante claves foráneas.

## Aplicación y despliegue

El proyecto incluye una aplicación web React/Vite, una API Spring Boot y una
base de datos PostgreSQL. `compose.yaml` inicia la API y PostgreSQL para
desarrollo; `compose.preproduccion.yaml` añade la web compilada y servida por
Nginx. En esa configuración la web se abre en `http://localhost:8081`, Nginx
redirige `/api` a la API y solo la web se publica en el PC. No hay una URL de
despliegue público configurada en este repositorio.

Para iniciar desde cero el entorno local de desarrollo, ejecuta
`.\scripts\poner-a-punto.ps1` en PowerShell y confirma escribiendo `RECREAR`.
Este proceso borra el volumen local de desarrollo y crea la cuenta:

| Usuario | Contraseña | Perfil |
| --- | --- | --- |
| `llvillar@gmail.com` | `1234` | Gestión de catálogos |

La API queda en `http://localhost:8080`. La cuenta se configura con ese script;
un simple `docker compose up --build` no establece esta contraseña en una base
de datos ya existente. El script es exclusivamente para desarrollo local y
destructivo: no lo uses en preproducción ni contra datos que quieras conservar.
Para generar historial de ejemplo, el script opcional
`.\scripts\cargar-datos-prueba.ps1` añade alumnos, profesores y permisos.

## Modelo de datos

- `alumnos`: DNI, nombre, apellidos, correo opcional y grupo.
- `profesores`: DNI, nombre, apellidos y correo opcional.
- `grupos`: curso, sección y código único. El catálogo incluye secciones A y B
  para 1.º–4.º ESO y 1.º–2.º Bachillerato.
- `franjas_horarias`: seis valores fijos, de `1º Hora` a `6º Hora`, con hora de
  inicio y fin configurables.
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

El esquema y los catálogos iniciales están en `db/01-inicializar.sql`; los datos
de prueba históricos, en `db/02-datos-prueba.sql`. Para preparar desarrollo
desde cero utiliza el script de puesta a punto descrito arriba; el script de
carga de datos de prueba es opcional. Ambos están en `scripts/`. La aplicación
aplica las migraciones de `src/main/resources/db/migration` antes
de actualizar el esquema; las cuentas existentes conservan sus datos y reciben
el correo de su profesor asociado. En desarrollo se puede ejecutar desde
IntelliJ o con `mvnw spring-boot:run`; las pruebas automatizadas usan H2 en
memoria y no requieren una instancia de PostgreSQL.

## Usuarios y permisos

La API requiere iniciar sesión con el correo electrónico del profesor y su
contraseña. Las cuentas se guardan en `usuarios_app`, con contraseñas cifradas
mediante BCrypt y una relación única con un profesor; el correo del profesor es
el identificador de acceso. Se usan sesiones de servidor con cookie y protección
CSRF. El primer usuario se crea al iniciar la aplicación con
`AUTH_INITIAL_PASSWORD` y `AUTH_INITIAL_PROFESOR_DNI`; debe corresponder a un
profesor existente con correo electrónico y obtiene permiso para gestionar
catálogos. Solo se crea si la tabla de usuarios está vacía, por lo que cambiar
esas variables no restablece las cuentas de una base ya inicializada.

El usuario inicial puede crear las cuentas de los demás profesores desde
**Cuentas de usuario** y asignar o retirar el permiso de gestión del catálogo.
Para crear una cuenta, el profesor debe tener correo registrado; ese correo será
su usuario de acceso y no puede compartirse con otra cuenta. Cada profesor puede
tener una sola cuenta. Si se cambia el correo de un profesor, cambia también su
identificador de acceso. Todos los profesores pueden cambiar su contraseña
desde **Cambiar contraseña**, indicando la contraseña actual.
Las contraseñas deben tener al menos 12 caracteres y no superar 72 bytes en
UTF-8. No hay registro público: las altas y los permisos los controla un usuario
de gestión de catálogos.

No uses contraseñas reales en el repositorio ni las reutilices. El script local
de puesta a punto crea `llvillar@gmail.com` con contraseña `1234` y perfil
`GESTION_CATALOGOS` (gestión completa), listo para probar en desarrollo local.
Es una credencial débil, guardada como hash y exclusiva del entorno local:
cámbiala antes de cualquier uso compartido y no la uses fuera de tu PC. Para
preproducción, configura una contraseña segura. La cuenta inicial solo se crea
si la tabla de usuarios está vacía.
Preproducción exige configurar estas dos variables. Puedes copiar el archivo de
ejemplo, editar los valores y mantener `.env` sin añadirlo al repositorio:

```powershell
Copy-Item .env.example .env
docker compose -f compose.preproduccion.yaml up --build -d
```

Compose carga `.env` automáticamente, así que los comandos `logs`, `ps` y
`down` también funcionarán desde terminales nuevas.

En un servidor con HTTPS, configura `SESSION_COOKIE_SECURE=true`; no publiques
el servicio sin HTTPS. Esa propiedad está activada por defecto al ejecutar la
aplicación fuera de Compose; los Compose locales la desactivan porque usan HTTP.
Si ejecutas Spring Boot directamente en tu PC, define
`SESSION_COOKIE_SECURE=false`.

## Frontend web

El frontend está en `frontend/` y ofrece un formulario para registrar permisos,
un historial filtrable por rango inclusivo de fechas (con navegación al día
anterior o siguiente) y paginado de cinco en cinco, un top 10 de alumnos con
más permisos en ese periodo y una sección de administración para dar de alta,
modificar y eliminar alumnos,
profesores, grupos y franjas horarias. El ranking respeta el rango de fechas
seleccionado, aunque se apliquen filtros de alumno o profesor en el historial.
Al crear o editar un alumno se debe seleccionar su grupo. La interfaz es
adaptable a móvil. Necesitas instalar Node.js LTS, que incluye `npm`, y abrir
una terminal nueva después de instalarlo. Comprueba que ambos comandos están
disponibles:

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

Necesitas Docker Desktop abierto. Para preparar la base de desarrollo desde
cero, abre PowerShell en la carpeta del proyecto y ejecuta:

```powershell
.\scripts\poner-a-punto.ps1
```

El script pide escribir `RECREAR`, elimina el volumen de desarrollo y todos sus
datos, crea el esquema y los catálogos iniciales, configura la cuenta local y
arranca la API. La API queda disponible en `http://localhost:8080`. Acceso local:
`llvillar@gmail.com` / `1234`. Esta puesta a punto es destructiva y solo debe
usarse para reiniciar la base local; no la ejecutes contra datos que quieras
conservar.

Para cargar datos de historial de prueba, ejecuta el segundo script:

```powershell
.\scripts\cargar-datos-prueba.ps1
```

Añade 50 alumnos y 10 profesores ficticios, y genera 5.000 permisos en días
lectivos desde el 1 de enero del año actual hasta hoy. Se puede volver a
ejecutar: reemplaza el historial de esos alumnos sintéticos sin borrar los
permisos de otros alumnos. Los correos de prueba usan el dominio reservado
`example.test`.

Después de la primera preparación, puedes iniciar los servicios con
`docker compose up --build`; se conserva el volumen y los datos al detenerlos.
Para parar los contenedores ejecuta `docker compose down`. Para volver a empezar
desde cero, ejecuta otra vez el script de puesta a punto y confirma el borrado.

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

La primera inicialización ejecuta `db/01-inicializar.sql`, que crea el esquema,
los catálogos iniciales y el profesor de ejemplo
`profesor.inicial@example.test`. La variable `$env:DB_PASSWORD` solo se aplica a una base
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
| `PUT` | `/api/auth/password` | Cambiar la contraseña del usuario autenticado |
| `GET` | `/api/usuarios` | Listar las cuentas (solo gestión de catálogos) |
| `POST` | `/api/usuarios` | Crear una cuenta para un profesor (solo gestión de catálogos) |
| `PUT` | `/api/usuarios/{id}/perfil` | Cambiar sus permisos (solo gestión de catálogos) |
| `DELETE` | `/api/usuarios/{id}` | Eliminar una cuenta (solo gestión de catálogos) |
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

Para crear una cuenta se envía `password`, `profesorId` y `perfil`
(`GESTION_PERMISOS` o `GESTION_CATALOGOS`); el identificador se obtiene del
correo del profesor seleccionado. La contraseña debe tener al menos 12
caracteres. Para cambiar la propia contraseña se envían `contrasenaActual` y
`contrasenaNueva`; una contraseña actual incorrecta devuelve `400`. No se puede
eliminar la propia cuenta ni retirar o eliminar la última cuenta con permiso de
gestión de catálogos.

Un grupo recibe `curso` (por ejemplo `1º ESO` o `2º Bachillerato`) y `seccion`
(`A` o `B`); la API genera automáticamente el código del grupo. Una franja
recibe `numero` entre 1 y 6; su nombre (`1º Hora` … `6º Hora`) se genera
automáticamente. Una franja recibe también `horaInicio` y `horaFin` en formato
`HH:mm` (por ejemplo, `08:30` y `09:20`); la hora de fin debe ser posterior al
inicio.

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
