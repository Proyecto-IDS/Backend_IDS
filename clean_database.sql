-- Script para limpiar la base de datos de reuniones y alertas y actualizar esquema
-- Ejecutar en PostgreSQL

-- Limpiar tabla de participantes de reuniones
DELETE FROM meeting_participants;

-- Limpiar tabla de historial de participación si existe
DELETE FROM meeting_participation_history;

-- Limpiar tabla de reuniones
DELETE FROM meeting;

-- Limpiar tabla de alertas
DELETE FROM alerts;

-- Agregar nueva columna si no existe
ALTER TABLE meeting 
ADD COLUMN IF NOT EXISTS duration_minutes BIGINT;

-- Remover columna max_participants si existe
ALTER TABLE meeting 
DROP COLUMN IF EXISTS max_participants;

-- Reiniciar secuencias (IDs) para que vuelvan a empezar desde 1
ALTER SEQUENCE IF EXISTS meeting_participants_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS meeting_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS alerts_id_seq RESTART WITH 1;

-- Verificar que las tablas estén vacías
SELECT 'Reuniones:', COUNT(*) FROM meeting;
SELECT 'Participantes:', COUNT(*) FROM meeting_participants;
SELECT 'Alertas:', COUNT(*) FROM alerts;
