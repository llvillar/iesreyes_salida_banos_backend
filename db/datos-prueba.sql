BEGIN;

INSERT INTO grupos (curso, seccion, codigo)
VALUES
    ('1º ESO', 'A', '1ºA ESO'), ('1º ESO', 'B', '1ºB ESO'),
    ('2º ESO', 'A', '2ºA ESO'), ('2º ESO', 'B', '2ºB ESO'),
    ('3º ESO', 'A', '3ºA ESO'), ('3º ESO', 'B', '3ºB ESO'),
    ('4º ESO', 'A', '4ºA ESO'), ('4º ESO', 'B', '4ºB ESO'),
    ('1º Bachillerato', 'A', '1ºA Bachillerato'), ('1º Bachillerato', 'B', '1ºB Bachillerato'),
    ('2º Bachillerato', 'A', '2ºA Bachillerato'), ('2º Bachillerato', 'B', '2ºB Bachillerato');

INSERT INTO franjas_horarias (numero, nombre)
VALUES
    (1, '1º Hora'), (2, '2º Hora'), (3, '3º Hora'),
    (4, '4º Hora'), (5, '5º Hora'), (6, '6º Hora');

INSERT INTO profesores (dni, nombre, apellidos, email)
VALUES
    ('00000001A', 'Marta', 'López', 'marta.lopez@example.test'),
    ('00000002B', 'Javier', 'Sánchez', 'javier.sanchez@example.test'),
    ('00000003C', 'Elena', 'Ruiz', 'elena.ruiz@example.test'),
    ('00000004D', 'Carlos', 'Martín', 'carlos.martin@example.test'),
    ('00000005E', 'Ana', 'Torres', 'ana.torres@example.test'),
    ('00000006F', 'Pedro', 'Gómez', 'pedro.gomez@example.test');

INSERT INTO alumnos (dni, nombre, apellidos, email, grupo_id)
SELECT datos.dni, datos.nombre, datos.apellidos, datos.email, grupos.id
FROM (VALUES
    ('00000011A', 'Lucía', 'Fernández', 'lucia.fernandez@example.test', '1ºA ESO'),
    ('00000012B', 'Hugo', 'Martínez', 'hugo.martinez@example.test', '1ºA ESO'),
    ('00000013C', 'Sofía', 'Rodríguez', 'sofia.rodriguez@example.test', '2ºB ESO'),
    ('00000014D', 'Daniel', 'García', 'daniel.garcia@example.test', '3ºA ESO'),
    ('00000015E', 'Paula', 'Sánchez', 'paula.sanchez@example.test', '4ºB ESO'),
    ('00000016F', 'Mateo', 'Pérez', 'mateo.perez@example.test', '1ºB Bachillerato'),
    ('00000017G', 'Emma', 'Díaz', 'emma.diaz@example.test', '1ºB ESO'),
    ('00000018H', 'Leo', 'Romero', 'leo.romero@example.test', '2ºA ESO'),
    ('00000019J', 'Valeria', 'Navarro', 'valeria.navarro@example.test', '2ºB ESO'),
    ('00000020K', 'Adrián', 'Ortega', 'adrian.ortega@example.test', '3ºB ESO'),
    ('00000021L', 'Carmen', 'Molina', 'carmen.molina@example.test', '4ºA ESO'),
    ('00000022M', 'Mario', 'Castro', 'mario.castro@example.test', '2ºA Bachillerato')
) AS datos(dni, nombre, apellidos, email, codigo_grupo)
JOIN grupos ON grupos.codigo = datos.codigo_grupo;

INSERT INTO permisos_bano (alumno_id, profesor_id, franja_horaria_id, fecha, hora)
SELECT alumnos.id, profesores.id, franjas_horarias.id, CURRENT_DATE, datos.hora::time
FROM (VALUES
    ('00000011A', '00000001A', 1, '08:35:00'),
    ('00000012B', '00000001A', 2, '09:25:00'),
    ('00000013C', '00000002B', 3, '10:20:00'),
    ('00000014D', '00000003C', 4, '11:40:00'),
    ('00000015E', '00000004D', 5, '12:30:00'),
    ('00000016F', '00000005E', 6, '13:20:00')
) AS datos(alumno_dni, profesor_dni, numero_franja, hora)
JOIN alumnos ON alumnos.dni = datos.alumno_dni
JOIN profesores ON profesores.dni = datos.profesor_dni
JOIN franjas_horarias ON franjas_horarias.numero = datos.numero_franja;

COMMIT;
