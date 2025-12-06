-- Agregar campo para almacenar las features originales del paquete
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS original_features TEXT;