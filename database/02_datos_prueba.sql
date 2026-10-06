-- =====================================================================
-- Tortas y Chocolates 3B - Datos de prueba (TODOS FICTICIOS)
-- Ejecutar después de 01_schema.sql.
-- Las fechas de pedidos se calculan a partir del día en que se ejecuta
-- el script (CURDATE()), así el dashboard siempre tiene datos "de hoy".
-- Las credenciales de los usuarios de prueba están en el README.
-- =====================================================================
USE tortas3b;

-- MySQL Workbench trae activado "safe updates", que bloquea los UPDATE
-- de abajo (recalculan todos los totales). Se desactiva solo en esta sesión.
SET SQL_SAFE_UPDATES = 0;

SET @hoy = CURDATE();

-- ---------------------------------------------------------------------
-- Usuarios (hash BCrypt; nunca se guarda la contraseña en texto plano)
-- ---------------------------------------------------------------------
INSERT INTO usuarios (nombre, username, password_hash, rol) VALUES
('Administrador de prueba', 'admin',    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN'),
('Operador de prueba',      'operador', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'OPERADOR');

-- ---------------------------------------------------------------------
-- Clientes ficticios
-- ---------------------------------------------------------------------
INSERT INTO clientes (nombre, telefono, direccion, email, observaciones) VALUES
('Ana Benítez (ficticia)',     '0990000101', 'Calle Las Rosas 120, Bº San Miguel',   'ana.ficticia@ejemplo.com',    NULL),
('Carlos Duarte (ficticio)',   '0990000102', 'Avda. Central 455, Bº Centro',          NULL,                          'Prefiere transferencia'),
('Lucía Giménez (ficticia)',   '0990000103', 'Calle 14 de Mayo 78, Bº San Miguel',    'lucia.ficticia@ejemplo.com',  NULL),
('Jorge Martínez (ficticio)',  '0990000104', 'Calle Los Lapachos 300, Bº Industrial', NULL,                          NULL),
('Sofía Ramírez (ficticia)',   '0990000105', 'Calle Itapúa 12, Bº San Miguel',        NULL,                          'Alergia a frutos secos'),
('Diego Ortiz (ficticio)',     '0990000106', 'Calle 1 N° 230, Bº Santa Lucía',        'diego.ficticio@ejemplo.com',  NULL),
('Valeria López (ficticia)',   '0990000107', 'Calle Palma 55, Bº Centro',             NULL,                          NULL),
('Martín Acosta (ficticio)',   '0990000108', 'Calle Tacuarí 410, Bº San Miguel',      NULL,                          'Cliente frecuente'),
('Camila Fernández (ficticia)','0990000109', 'Calle Yegros 88, Bº Las Mercedes',      'camila.ficticia@ejemplo.com', NULL),
('Pablo Rojas (ficticio)',     '0990000110', 'Calle Cerro Corá 19, Bº San Miguel',    NULL,                          NULL);

-- ---------------------------------------------------------------------
-- Productos (precios en guaraníes)
-- ---------------------------------------------------------------------
INSERT INTO productos (nombre, descripcion, categoria, precio, costo_kilo, peso_kg) VALUES
('Torta de chocolate 1 kg',          'Bizcocho de chocolate relleno de dulce de leche', 'TORTA',     120000,  74000, 1.000),
('Torta tres leches 1 kg',           'Clásica torta húmeda tres leches',                'TORTA',     110000,  68000, 1.000),
('Torta selva negra 1,5 kg',         'Chocolate, crema y cerezas',                      'TORTA',     180000,  74000, 1.500),
('Torta de frutillas 1 kg',          'Bizcocho de vainilla con crema y frutillas',      'TORTA',     125000,  77000, 1.000),
('Torta de cumpleaños decorada 2 kg','Torta personalizada con decoración temática',     'TORTA',     250000,  77000, 2.000),
('Cupcake de vainilla',              'Cupcake con frosting de vainilla',                'CUPCAKE',     8000,  62000, 0.080),
('Cupcake de chocolate',             'Cupcake con ganache de chocolate',                'CUPCAKE',     9000,  70000, 0.080),
('Cupcake red velvet',               'Cupcake red velvet con queso crema',              'CUPCAKE',    10000,  77000, 0.080),
('Caja de 6 cupcakes surtidos',      'Seis cupcakes de sabores variados',               'CUPCAKE',    50000,  65000, 0.480),
('Caja de 12 bombones artesanales',  'Bombones rellenos surtidos',                      'CHOCOLATE',  45000, 155000, 0.180),
('Tableta de chocolate amargo',      'Chocolate 70 % cacao, 100 g',                     'CHOCOLATE',  25000, 155000, 0.100),
('Chocolate relleno de dulce de leche','Unidad individual, 60 g',                       'CHOCOLATE',  15000, 155000, 0.060);

-- ---------------------------------------------------------------------
-- Proveedores ficticios
-- ---------------------------------------------------------------------
INSERT INTO proveedores (nombre, telefono, direccion, insumos) VALUES
('Distribuidora Harinera Ejemplo', '0990000201', 'Ruta 2 km 140 (ficticia)', 'Harina, azúcar, polvo de hornear'),
('Lácteos Ficticios S.A.',         '0990000202', 'Calle Lechera 10 (ficticia)', 'Leche, crema, manteca, leche condensada, dulce de leche'),
('Chocolatería Mayorista Demo',    '0990000203', 'Avda. Cacao 99 (ficticia)', 'Cacao, chocolate cobertura, esencias'),
('Embalajes y Más (ficticio)',     '0990000204', 'Calle Cartón 7 (ficticia)', 'Cajas, pirotines, bandejas');

-- ---------------------------------------------------------------------
-- Ingredientes (3 con stock bajo: Manteca, Crema de leche, Chocolate cobertura)
-- ---------------------------------------------------------------------
INSERT INTO ingredientes (nombre, unidad, stock_actual, stock_minimo, proveedor_id) VALUES
('Harina 0000',               'KG',      25.000,  10.000, 1),
('Azúcar',                    'KG',      18.000,  10.000, 1),
('Huevos',                    'UNIDAD', 120.000,  60.000, NULL),
('Manteca',                   'KG',       3.000,   5.000, 2),
('Leche entera',              'L',       12.000,   8.000, 2),
('Crema de leche',            'L',        4.000,   4.000, 2),
('Cacao en polvo',            'KG',       2.500,   1.000, 3),
('Chocolate cobertura semiamargo','KG',   1.500,   3.000, 3),
('Dulce de leche',            'KG',       6.000,   3.000, 2),
('Frutillas',                 'KG',       2.000,   1.000, NULL),
('Polvo de hornear',          'KG',       0.800,   0.500, 1),
('Esencia de vainilla',       'ML',     400.000, 200.000, 3),
('Leche condensada',          'UNIDAD',  20.000,  12.000, 2),
('Cajas para torta',          'UNIDAD',  30.000,  15.000, 4),
('Pirotines',                 'UNIDAD', 300.000, 200.000, 4);

-- ---------------------------------------------------------------------
-- Pedidos (25). Columnas: cliente, usuario que lo cargó, fecha del pedido,
-- fecha y hora de entrega, tipo, estado, observaciones.
-- Mañana entre 15:00 y 15:59 hay 5 pedidos activos (franja LLENA) y uno
-- cancelado, para demostrar el límite de 5 entregas por hora.
-- ---------------------------------------------------------------------
INSERT INTO pedidos (cliente_id, usuario_id, fecha_pedido, fecha_entrega, hora_entrega, tipo_entrega, estado, observaciones) VALUES
-- días anteriores
( 1, 1, TIMESTAMP(@hoy - INTERVAL 9 DAY, '09:15'), @hoy - INTERVAL 7 DAY, '10:00', 'RETIRO',   'ENTREGADO', NULL),
( 2, 2, TIMESTAMP(@hoy - INTERVAL 8 DAY, '10:20'), @hoy - INTERVAL 6 DAY, '15:30', 'DELIVERY', 'ENTREGADO', NULL),
( 3, 2, TIMESTAMP(@hoy - INTERVAL 7 DAY, '11:00'), @hoy - INTERVAL 5 DAY, '11:00', 'RETIRO',   'ENTREGADO', NULL),
( 4, 1, TIMESTAMP(@hoy - INTERVAL 6 DAY, '16:45'), @hoy - INTERVAL 4 DAY, '17:00', 'DELIVERY', 'ENTREGADO', 'Tocar timbre dos veces'),
( 5, 2, TIMESTAMP(@hoy - INTERVAL 5 DAY, '08:30'), @hoy - INTERVAL 4 DAY, '09:30', 'RETIRO',   'CANCELADO', 'Cliente canceló por viaje'),
( 6, 2, TIMESTAMP(@hoy - INTERVAL 5 DAY, '13:10'), @hoy - INTERVAL 3 DAY, '14:00', 'RETIRO',   'ENTREGADO', NULL),
( 7, 1, TIMESTAMP(@hoy - INTERVAL 4 DAY, '09:00'), @hoy - INTERVAL 2 DAY, '16:00', 'DELIVERY', 'ENTREGADO', NULL),
( 8, 2, TIMESTAMP(@hoy - INTERVAL 3 DAY, '10:40'), @hoy - INTERVAL 2 DAY, '18:30', 'RETIRO',   'ENTREGADO', 'Para cumpleaños infantil'),
( 9, 2, TIMESTAMP(@hoy - INTERVAL 3 DAY, '12:00'), @hoy - INTERVAL 1 DAY, '10:30', 'DELIVERY', 'ENTREGADO', NULL),
(10, 1, TIMESTAMP(@hoy - INTERVAL 2 DAY, '15:20'), @hoy - INTERVAL 1 DAY, '12:00', 'RETIRO',   'ENTREGADO', NULL),
( 1, 2, TIMESTAMP(@hoy - INTERVAL 2 DAY, '17:00'), @hoy - INTERVAL 1 DAY, '19:00', 'RETIRO',   'ENTREGADO', NULL),
-- hoy
( 2, 2, TIMESTAMP(@hoy - INTERVAL 3 DAY, '09:30'), @hoy, '09:00', 'RETIRO',   'ENTREGADO',      NULL),
( 3, 1, TIMESTAMP(@hoy - INTERVAL 2 DAY, '10:00'), @hoy, '11:00', 'DELIVERY', 'EN_CAMINO',      NULL),
( 4, 2, TIMESTAMP(@hoy - INTERVAL 2 DAY, '14:30'), @hoy, '14:00', 'RETIRO',   'LISTO',          NULL),
( 5, 2, TIMESTAMP(@hoy - INTERVAL 1 DAY, '09:10'), @hoy, '16:30', 'DELIVERY', 'EN_PREPARACION', 'Sin nueces (alergia)'),
( 6, 1, TIMESTAMP(@hoy - INTERVAL 1 DAY, '11:25'), @hoy, '18:00', 'RETIRO',   'CONFIRMADO',     NULL),
-- mañana, franja 15:00-15:59 (5 activos + 1 cancelado)
( 7, 2, TIMESTAMP(@hoy - INTERVAL 1 DAY, '16:00'), @hoy + INTERVAL 1 DAY, '15:00', 'RETIRO',   'CONFIRMADO', NULL),
( 8, 2, TIMESTAMP(@hoy - INTERVAL 1 DAY, '17:30'), @hoy + INTERVAL 1 DAY, '15:15', 'DELIVERY', 'CONFIRMADO', NULL),
( 9, 1, TIMESTAMP(@hoy, '08:10'),                  @hoy + INTERVAL 1 DAY, '15:30', 'RETIRO',   'PENDIENTE',  NULL),
(10, 2, TIMESTAMP(@hoy, '08:40'),                  @hoy + INTERVAL 1 DAY, '15:45', 'DELIVERY', 'PENDIENTE',  NULL),
( 1, 2, TIMESTAMP(@hoy, '09:05'),                  @hoy + INTERVAL 1 DAY, '15:50', 'RETIRO',   'PENDIENTE',  NULL),
( 2, 1, TIMESTAMP(@hoy, '09:20'),                  @hoy + INTERVAL 1 DAY, '15:20', 'RETIRO',   'CANCELADO',  'Cancelado: no cuenta para el límite'),
-- próximos días
( 3, 2, TIMESTAMP(@hoy, '10:00'), @hoy + INTERVAL 2 DAY, '10:00', 'DELIVERY', 'PENDIENTE',  'Dedicatoria: "Feliz cumple"'),
( 4, 1, TIMESTAMP(@hoy, '10:30'), @hoy + INTERVAL 3 DAY, '17:00', 'RETIRO',   'CONFIRMADO', NULL),
( 5, 2, TIMESTAMP(@hoy, '11:00'), @hoy + INTERVAL 5 DAY, '11:30', 'DELIVERY', 'PENDIENTE',  NULL);

-- ---------------------------------------------------------------------
-- Detalle de pedidos (pedido, producto, cantidad).
-- Precio y subtotal se completan abajo desde la tabla productos para que
-- los montos sean siempre coherentes.
-- ---------------------------------------------------------------------
INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio_unitario, subtotal) VALUES
( 1,  1, 1, 0, 0), ( 1,  6, 4, 0, 0),
( 2,  3, 1, 0, 0),
( 3,  5, 1, 0, 0), ( 3, 10, 2, 0, 0),
( 4,  2, 1, 0, 0), ( 4,  7, 6, 0, 0),
( 5,  1, 1, 0, 0),
( 6,  9, 2, 0, 0),
( 7,  4, 1, 0, 0), ( 7, 11, 3, 0, 0),
( 8,  8,12, 0, 0),
( 9,  3, 1, 0, 0), ( 9, 12, 4, 0, 0),
(10,  2, 1, 0, 0),
(11, 10, 3, 0, 0), (11, 11, 2, 0, 0),
(12,  1, 1, 0, 0),
(13,  5, 1, 0, 0),
(14,  6, 6, 0, 0), (14,  7, 6, 0, 0),
(15,  4, 1, 0, 0),
(16,  2, 1, 0, 0), (16, 10, 1, 0, 0),
(17,  1, 1, 0, 0),
(18,  3, 1, 0, 0),
(19,  9, 1, 0, 0),
(20,  8, 6, 0, 0),
(21,  2, 1, 0, 0),
(22,  1, 1, 0, 0),
(23,  5, 1, 0, 0), (23,  6,12, 0, 0),
(24,  4, 1, 0, 0),
(25, 11, 5, 0, 0), (25, 12, 5, 0, 0);

UPDATE detalle_pedido d
JOIN productos p ON p.id = d.producto_id
SET d.precio_unitario = p.precio,
    d.subtotal        = p.precio * d.cantidad;

UPDATE pedidos pe
SET pe.total = (SELECT SUM(d.subtotal) FROM detalle_pedido d WHERE d.pedido_id = pe.id);

-- ---------------------------------------------------------------------
-- Delivery: uno por cada pedido de tipo DELIVERY, con estado equivalente
-- ---------------------------------------------------------------------
INSERT INTO delivery (pedido_id, direccion, telefono_contacto, estado)
SELECT pe.id, c.direccion, c.telefono,
       CASE pe.estado
           WHEN 'PENDIENTE'      THEN 'PENDIENTE'
           WHEN 'CONFIRMADO'     THEN 'PENDIENTE'
           WHEN 'EN_PREPARACION' THEN 'PREPARANDO'
           WHEN 'LISTO'          THEN 'PREPARANDO'
           WHEN 'EN_CAMINO'      THEN 'EN_CAMINO'
           WHEN 'ENTREGADO'      THEN 'ENTREGADO'
           ELSE 'CANCELADO'
       END
FROM pedidos pe
JOIN clientes c ON c.id = pe.cliente_id
WHERE pe.tipo_entrega = 'DELIVERY';

-- ---------------------------------------------------------------------
-- Ventas: una por cada pedido ENTREGADO (11 ventas)
-- ---------------------------------------------------------------------
INSERT INTO ventas (pedido_id, usuario_id, fecha, metodo_pago, total)
SELECT pe.id,
       pe.usuario_id,
       TIMESTAMP(pe.fecha_entrega, pe.hora_entrega),
       CASE MOD(pe.id, 3) WHEN 0 THEN 'EFECTIVO' WHEN 1 THEN 'TRANSFERENCIA' ELSE 'OTRO' END,
       pe.total
FROM pedidos pe
WHERE pe.estado = 'ENTREGADO';

-- ---------------------------------------------------------------------
-- Compras de reposición: una recibida (hace 5 días) y una pendiente
-- ---------------------------------------------------------------------
INSERT INTO compras (proveedor_id, usuario_id, fecha, fecha_recepcion, estado, observaciones) VALUES
(1, 1, TIMESTAMP(@hoy - INTERVAL 6 DAY, '08:00'), TIMESTAMP(@hoy - INTERVAL 5 DAY, '10:00'), 'RECIBIDA',  NULL),
(2, 1, TIMESTAMP(@hoy - INTERVAL 1 DAY, '09:00'), NULL,                                       'PENDIENTE', 'Reponer manteca y crema (stock bajo)');

INSERT INTO detalle_compra (compra_id, ingrediente_id, cantidad, costo_unitario, subtotal) VALUES
(1, 1, 10.000, 6500, 65000),   -- Harina 10 kg
(1, 2,  8.000, 7000, 56000),   -- Azúcar 8 kg
(2, 4,  5.000, 45000, 225000), -- Manteca 5 kg
(2, 6,  6.000, 22000, 132000); -- Crema de leche 6 L

UPDATE compras co
SET co.total = (SELECT SUM(d.subtotal) FROM detalle_compra d WHERE d.compra_id = co.id);

-- ---------------------------------------------------------------------
-- Movimientos de stock (coherentes con el stock actual de arriba)
-- ---------------------------------------------------------------------
INSERT INTO movimientos_stock (ingrediente_id, usuario_id, compra_id, fecha, tipo, cantidad, stock_resultante, motivo) VALUES
(1, 1, 1,    TIMESTAMP(@hoy - INTERVAL 5 DAY, '10:00'), 'COMPRA', 10.000, 25.000, 'Recepción de compra N° 1'),
(2, 1, 1,    TIMESTAMP(@hoy - INTERVAL 5 DAY, '10:00'), 'COMPRA',  8.000, 18.000, 'Recepción de compra N° 1'),
(4, 1, NULL, TIMESTAMP(@hoy - INTERVAL 1 DAY, '18:00'), 'AJUSTE', -1.000,  3.000, 'Consumo de producción de la semana');

SET SQL_SAFE_UPDATES = 1;
