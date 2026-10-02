ALTER TABLE IF EXISTS usuarios_app
    ADD COLUMN IF NOT EXISTS email VARCHAR(150);

DO $$
BEGIN
    IF to_regclass('public.usuarios_app') IS NOT NULL THEN
        UPDATE usuarios_app AS usuario
        SET email = profesor.email
        FROM profesores AS profesor
        WHERE profesor.id = usuario.profesor_id
          AND usuario.email IS NULL;

        ALTER TABLE usuarios_app
            ALTER COLUMN email SET NOT NULL;
    END IF;
END;
$$;
