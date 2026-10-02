BEGIN;

INSERT INTO grupos (curso, seccion, codigo)
VALUES
    ('1º ESO', 'A', '1ºA ESO'), ('1º ESO', 'B', '1ºB ESO'),
    ('2º ESO', 'A', '2ºA ESO'), ('2º ESO', 'B', '2ºB ESO'),
    ('3º ESO', 'A', '3ºA ESO'), ('3º ESO', 'B', '3ºB ESO'),
    ('4º ESO', 'A', '4ºA ESO'), ('4º ESO', 'B', '4ºB ESO'),
    ('1º Bachillerato', 'A', '1ºA Bachillerato'), ('1º Bachillerato', 'B', '1ºB Bachillerato'),
    ('2º Bachillerato', 'A', '2ºA Bachillerato'), ('2º Bachillerato', 'B', '2ºB Bachillerato')
ON CONFLICT (curso, seccion) DO NOTHING;

INSERT INTO franjas_horarias (numero, nombre)
VALUES
    (1, '1º Hora'), (2, '2º Hora'), (3, '3º Hora'),
    (4, '4º Hora'), (5, '5º Hora'), (6, '6º Hora')
ON CONFLICT (numero) DO NOTHING;

INSERT INTO profesores (dni, nombre, apellidos, email)
SELECT
    (80000000 + numero)::text
        || substr('TRWAGMYFPDXBNJZSQVHLCKE', ((80000000 + numero) % 23) + 1, 1),
    'Profesor',
    'Prueba ' || lpad(numero::text, 2, '0'),
    'profesor.prueba.' || lpad(numero::text, 2, '0') || '@example.test'
FROM generate_series(1, 10) AS datos(numero)
ON CONFLICT (dni) DO NOTHING;

WITH grupos_ordenados AS (
    SELECT id, row_number() OVER (ORDER BY codigo) AS posicion, count(*) OVER () AS total
    FROM grupos
)
INSERT INTO alumnos (dni, nombre, apellidos, email, grupo_id)
SELECT
    (70000000 + datos.numero)::text
        || substr('TRWAGMYFPDXBNJZSQVHLCKE', ((70000000 + datos.numero) % 23) + 1, 1),
    'Alumno',
    'Prueba ' || lpad(datos.numero::text, 3, '0'),
    'alumno.prueba.' || lpad(datos.numero::text, 3, '0') || '@example.test',
    grupos.id
FROM generate_series(1, 50) AS datos(numero)
JOIN grupos_ordenados AS grupos
    ON grupos.posicion = ((datos.numero - 1) % grupos.total) + 1
ON CONFLICT (dni) DO NOTHING;

DELETE FROM permisos_bano
USING alumnos
WHERE permisos_bano.alumno_id = alumnos.id
  AND alumnos.email LIKE 'alumno.prueba.%@example.test';

WITH dias_lectivos AS (
    SELECT
        fecha::date AS fecha,
        row_number() OVER (ORDER BY fecha) AS numero,
        count(*) OVER () AS total
    FROM generate_series(
        date_trunc('year', CURRENT_DATE)::date,
        CURRENT_DATE,
        INTERVAL '1 day'
    ) AS fechas(fecha)
    WHERE extract(isodow FROM fecha) BETWEEN 1 AND 5
),
alumnos_prueba AS (
    SELECT id, row_number() OVER (ORDER BY dni) AS numero
    FROM alumnos
    WHERE email LIKE 'alumno.prueba.%@example.test'
),
profesores_prueba AS (
    SELECT id, row_number() OVER (ORDER BY dni) AS numero
    FROM profesores
    WHERE email LIKE 'profesor.prueba.%@example.test'
),
permisos_generados AS (
    SELECT
        numero,
        ((numero * 17 - 1) % 50) + 1 AS numero_alumno,
        ((numero * 7 - 1) % 10) + 1 AS numero_profesor,
        ((numero * 5 - 1) % 6) + 1 AS numero_franja
    FROM generate_series(1, 5000) AS filas(numero)
)
INSERT INTO permisos_bano (alumno_id, profesor_id, franja_horaria_id, fecha, hora)
SELECT
    alumnos_prueba.id,
    profesores_prueba.id,
    franjas.id,
    dias_lectivos.fecha,
    TIME '08:30:00'
        + ((franjas.numero - 1) * INTERVAL '50 minutes')
        + ((permisos_generados.numero % 20) * INTERVAL '1 minute')
FROM permisos_generados
JOIN alumnos_prueba
    ON alumnos_prueba.numero = permisos_generados.numero_alumno
JOIN profesores_prueba
    ON profesores_prueba.numero = permisos_generados.numero_profesor
JOIN franjas_horarias AS franjas
    ON franjas.numero = permisos_generados.numero_franja
JOIN dias_lectivos
    ON dias_lectivos.numero =
        ((permisos_generados.numero - 1) % dias_lectivos.total) + 1;

COMMIT;
