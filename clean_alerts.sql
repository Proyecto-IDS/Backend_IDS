-- Script para limpiar todas las alertas de la base de datos
-- ADVERTENCIA: Esto eliminará TODAS las alertas existentes

DELETE FROM alerts;

-- Reiniciar el contador de IDs (opcional)
ALTER SEQUENCE alerts_id_seq RESTART WITH 1;
