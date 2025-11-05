-- Script para limpiar usuarios duplicados y agregar constraint único

-- 1. Ver usuarios duplicados
SELECT email, COUNT(*) as count 
FROM _user 
GROUP BY email 
HAVING COUNT(*) > 1;

-- 2. Eliminar duplicados, manteniendo solo el más reciente de cada email
DELETE FROM _user a
USING _user b
WHERE a.id < b.id 
  AND a.email = b.email;

-- 3. Agregar constraint único para evitar duplicados en el futuro
ALTER TABLE _user ADD CONSTRAINT user_email_unique UNIQUE (email);

-- 4. Verificar que no hay duplicados
SELECT email, COUNT(*) as count 
FROM _user 
GROUP BY email 
HAVING COUNT(*) > 1;

-- 5. Ver todos los usuarios restantes
SELECT id, email, name, role FROM _user ORDER BY id;
