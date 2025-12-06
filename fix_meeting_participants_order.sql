-- Script para verificar y corregir el orden de columnas en meeting_participants
-- El problema: las columnas user_id y meeting_id pueden estar en orden inverso
-- 
-- Orden correcto según JPA (@JoinTable):
--   1. meeting_id (joinColumns)
--   2. user_id (inverseJoinColumns)

-- Paso 1: Verificar el orden actual de las columnas
SELECT 
    column_name, 
    ordinal_position,
    data_type
FROM information_schema.columns
WHERE table_name = 'meeting_participants'
  AND column_name IN ('meeting_id', 'user_id')
ORDER BY ordinal_position;

-- Si las columnas están en el orden incorrecto (user_id antes que meeting_id),
-- ejecutar los siguientes comandos:

-- Paso 2: Crear tabla temporal con el orden correcto
CREATE TABLE meeting_participants_new (
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (meeting_id, user_id),
    FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES _user(id) ON DELETE CASCADE
);

-- Paso 3: Copiar datos existentes
INSERT INTO meeting_participants_new (meeting_id, user_id)
SELECT meeting_id, user_id FROM meeting_participants;

-- Paso 4: Eliminar tabla antigua
DROP TABLE meeting_participants CASCADE;

-- Paso 5: Renombrar tabla nueva
ALTER TABLE meeting_participants_new RENAME TO meeting_participants;

-- Paso 6: Crear índices
CREATE INDEX idx_meeting_participants_meeting ON meeting_participants(meeting_id);
CREATE INDEX idx_meeting_participants_user ON meeting_participants(user_id);

-- Paso 7: Verificar el resultado
SELECT 
    column_name, 
    ordinal_position,
    data_type
FROM information_schema.columns
WHERE table_name = 'meeting_participants'
ORDER BY ordinal_position;

COMMENT ON TABLE meeting_participants IS 'Tabla de relación muchos-a-muchos entre meetings y users. Orden: meeting_id, user_id';
