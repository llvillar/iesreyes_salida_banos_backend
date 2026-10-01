BEGIN;

INSERT INTO profesores (dni, nombre, apellidos, email)
SELECT
    (80000000 + numero)::text ||
        substr('TRWAGMYFPDXBNJZSQVHLCKE', ((80000000 + numero) % 23) + 1, 1),
    'Profesor',
    'Prueba estadisticas ' || lpad(numero::text, 2, '0'),
    'profesor.estadisticas.' || lpad(numero::text, 2, '0') || '@example.test'
FROM generate_series(1, 12) AS numero
ON CONFLICT (dni) DO NOTHING;

WITH grupos_ordenados AS (
    SELECT
        id,
        row_number() OVER (ORDER BY id) AS posicion,
        count(*) OVER () AS total
    FROM grupos
)
INSERT INTO alumnos (dni, nombre, apellidos, email, grupo_id)
SELECT
    (70000000 + datos.numero)::text ||
        substr('TRWAGMYFPDXBNJZSQVHLCKE', ((70000000 + datos.numero) % 23) + 1, 1),
    'Alumno',
    'Prueba estadisticas ' || lpad(datos.numero::text, 2, '0'),
    'alumno.estadisticas.' || lpad(datos.numero::text, 2, '0') || '@example.test',
    grupos.id
FROM generate_series(1, 36) AS datos(numero)
JOIN grupos_ordenados AS grupos
    ON grupos.posicion = ((datos.numero - 1) % grupos.total) + 1
ON CONFLICT (dni) DO NOTHING;

DELETE FROM permisos_bano
USING alumnos
WHERE permisos_bano.alumno_id = alumnos.id
  AND alumnos.email LIKE 'alumno.estadisticas.%@example.test';

WITH dias_lectivos AS (
    SELECT
        fecha::date AS fecha,
        row_number() OVER (ORDER BY fecha) AS numero,
        count(*) OVER () AS total
    FROM generate_series(
        DATE '2026-01-01',
        CURRENT_DATE,
        INTERVAL '1 day'
    ) AS fechas(fecha)
    WHERE extract(isodow FROM fecha) BETWEEN 1 AND 5
),
alumnos_prueba AS (
    SELECT id, row_number() OVER (ORDER BY dni) AS numero
    FROM alumnos
    WHERE email LIKE 'alumno.estadisticas.%@example.test'
),
profesores_prueba AS (
    SELECT id, row_number() OVER (ORDER BY dni) AS numero
    FROM profesores
    WHERE email LIKE 'profesor.estadisticas.%@example.test'
),
permisos_generados AS (
    SELECT
        numero,
        CASE
            WHEN numero % 100 < 20 THEN 1
            WHEN numero % 100 < 35 THEN 2
            WHEN numero % 100 < 47 THEN 3
            WHEN numero % 100 < 57 THEN 4
            ELSE 5 + (numero % 32)
        END AS numero_alumno,
        ((numero * 7 - 1) % 12) + 1 AS numero_profesor,
        ((numero * 5 - 1) % 6) + 1 AS numero_franja
    FROM generate_series(1, 6000) AS filas(numero)
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
