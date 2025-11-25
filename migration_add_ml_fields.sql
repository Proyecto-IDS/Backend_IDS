-- Script de migración para agregar campos del modelo ML a la tabla alerts
-- Ejecutar en la base de datos PostgreSQL

ALTER TABLE alerts ADD COLUMN IF NOT EXISTS prediction VARCHAR(255);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS attack_probability DOUBLE PRECISION;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS category VARCHAR(255);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS standard_protocol TEXT;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS probabilities TEXT;

-- Comentarios para documentación
COMMENT ON COLUMN alerts.prediction IS 'Tipo de ataque predicho por el modelo ML (e.g., neptune, apache2, etc.)';
COMMENT ON COLUMN alerts.attack_probability IS 'Probabilidad de que sea un ataque (0.0 - 1.0)';
COMMENT ON COLUMN alerts.category IS 'Categoría del ataque (e.g., dos, probe, r2l, u2r)';
COMMENT ON COLUMN alerts.standard_protocol IS 'Protocolo estándar y recomendaciones de mitigación';
COMMENT ON COLUMN alerts.probabilities IS 'JSON con todas las probabilidades por tipo de ataque';
