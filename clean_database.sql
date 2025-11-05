-- Script para limpiar la base de datos de reuniones y alertas
-- Ejecutar en PostgreSQL

-- Limpiar tabla de participantes de reuniones
DELETE FROM meeting_participants;

-- Limpiar tabla de reuniones
DELETE FROM meeting;

-- Limpiar tabla de alertas
DELETE FROM alerts;

-- Reiniciar secuencias (IDs) para que vuelvan a empezar desde 1
ALTER SEQUENCE IF EXISTS meeting_participants_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS meeting_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS alerts_id_seq RESTART WITH 1;

-- Verificar que las tablas estén vacías
SELECT 'Reuniones:', COUNT(*) FROM meeting;
SELECT 'Participantes:', COUNT(*) FROM meeting_participants;
SELECT 'Alertas:', COUNT(*) FROM alerts;
