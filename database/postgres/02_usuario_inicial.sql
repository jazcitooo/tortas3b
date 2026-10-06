-- =====================================================================
-- Tortas y Chocolates 3B - Arranque "limpio" para PostgreSQL (Supabase)
-- Alternativa a 02_datos_prueba.sql: crea SOLO un usuario administrador,
-- sin clientes, productos ni pedidos de ejemplo.
--
-- Usuario: admin    Contraseña: password
-- Cambie la contraseña apenas entre (Usuarios > Editar).
-- =====================================================================
INSERT INTO usuarios (nombre, username, password_hash, rol) VALUES
('Administrador', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN');
