-- =====================================================================
-- Tortas y Chocolates 3B - Script de creación de la base de datos
-- Motor: MySQL 8.0.16 o superior (necesario para que se apliquen los CHECK)
--
-- ATENCIÓN: este script BORRA la base "tortas3b" si ya existe y la crea
-- de nuevo vacía. Después ejecutar 02_datos_prueba.sql.
-- =====================================================================

DROP DATABASE IF EXISTS tortas3b;
CREATE DATABASE tortas3b CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tortas3b;

-- ---------------------------------------------------------------------
-- Usuarios del sistema (login). La contraseña se guarda con hash BCrypt.
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            ENUM('ADMIN','OPERADOR') NOT NULL,
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_username UNIQUE (username)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Clientes de la pastelería
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    telefono       VARCHAR(15)  NOT NULL,
    direccion      VARCHAR(200) NULL,
    email          VARCHAR(100) NULL,
    observaciones  VARCHAR(255) NULL,
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_clientes_telefono UNIQUE (telefono)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Productos (tortas, cupcakes, chocolates)
-- peso_kg: peso aproximado de UNA unidad, para el reporte de kilos vendidos
-- ---------------------------------------------------------------------
CREATE TABLE productos (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100)  NOT NULL,
    descripcion    VARCHAR(255)  NULL,
    categoria      ENUM('TORTA','CUPCAKE','CHOCOLATE','OTRO') NOT NULL,
    precio         DECIMAL(12,0) NOT NULL,
    costo_kilo     DECIMAL(12,0) NOT NULL DEFAULT 0,
    peso_kg        DECIMAL(6,3)  NOT NULL,
    activo         TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT uq_productos_nombre UNIQUE (nombre),
    CONSTRAINT ck_productos_precio CHECK (precio > 0),
    CONSTRAINT ck_productos_costo  CHECK (costo_kilo >= 0),
    CONSTRAINT ck_productos_peso   CHECK (peso_kg > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Proveedores de insumos
-- ---------------------------------------------------------------------
CREATE TABLE proveedores (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    telefono       VARCHAR(15)  NOT NULL,
    direccion      VARCHAR(200) NULL,
    insumos        VARCHAR(255) NULL,
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT uq_proveedores_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Ingredientes / inventario
-- Stock bajo cuando stock_actual <= stock_minimo
-- ---------------------------------------------------------------------
CREATE TABLE ingredientes (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100)  NOT NULL,
    unidad         ENUM('KG','G','L','ML','UNIDAD') NOT NULL,
    stock_actual   DECIMAL(10,3) NOT NULL DEFAULT 0,
    stock_minimo   DECIMAL(10,3) NOT NULL DEFAULT 0,
    proveedor_id   INT NULL,
    activo         TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT uq_ingredientes_nombre UNIQUE (nombre),
    CONSTRAINT ck_ingredientes_stock  CHECK (stock_actual >= 0),
    CONSTRAINT ck_ingredientes_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT fk_ingredientes_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedores (id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Pedidos de clientes (cabecera)
-- El índice por fecha/hora acelera el control de "máximo 5 entregas por hora"
-- ---------------------------------------------------------------------
CREATE TABLE pedidos (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id     INT NOT NULL,
    usuario_id     INT NOT NULL,
    fecha_pedido   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega  DATE NOT NULL,
    hora_entrega   TIME NOT NULL,
    tipo_entrega   ENUM('RETIRO','DELIVERY') NOT NULL,
    estado         ENUM('PENDIENTE','CONFIRMADO','EN_PREPARACION','LISTO',
                        'EN_CAMINO','ENTREGADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    total          DECIMAL(12,0) NOT NULL DEFAULT 0,
    observaciones  VARCHAR(255) NULL,
    CONSTRAINT ck_pedidos_total CHECK (total >= 0),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    INDEX idx_pedidos_entrega (fecha_entrega, hora_entrega)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Detalle de cada pedido. precio_unitario guarda el precio del momento,
-- así un cambio de precio posterior no altera pedidos viejos.
-- ---------------------------------------------------------------------
CREATE TABLE detalle_pedido (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id        INT NOT NULL,
    producto_id      INT NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  DECIMAL(12,0) NOT NULL,
    subtotal         DECIMAL(12,0) NOT NULL,
    CONSTRAINT uq_detalle_pedido_producto UNIQUE (pedido_id, producto_id),
    CONSTRAINT ck_detalle_pedido_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_pedido_precio   CHECK (precio_unitario >= 0),
    CONSTRAINT fk_detalle_pedido_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_pedido_producto FOREIGN KEY (producto_id)
        REFERENCES productos (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Delivery: solo para pedidos con tipo_entrega = 'DELIVERY' (1 a 1)
-- Cliente, fecha y hora se obtienen del pedido (no se repiten).
-- ---------------------------------------------------------------------
CREATE TABLE delivery (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id          INT NOT NULL,
    direccion          VARCHAR(200) NOT NULL,
    telefono_contacto  VARCHAR(15)  NOT NULL,
    estado             ENUM('PENDIENTE','PREPARANDO','EN_CAMINO','ENTREGADO','CANCELADO')
                       NOT NULL DEFAULT 'PENDIENTE',
    observaciones      VARCHAR(255) NULL,
    actualizado_en     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_delivery_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_delivery_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Ventas / caja: una venta por pedido
-- ---------------------------------------------------------------------
CREATE TABLE ventas (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id      INT NOT NULL,
    usuario_id     INT NOT NULL,
    fecha          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago    ENUM('EFECTIVO','TRANSFERENCIA','OTRO') NOT NULL,
    total          DECIMAL(12,0) NOT NULL,
    CONSTRAINT uq_ventas_pedido UNIQUE (pedido_id),
    CONSTRAINT ck_ventas_total CHECK (total > 0),
    CONSTRAINT fk_ventas_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedidos (id),
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    INDEX idx_ventas_fecha (fecha)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Compras = pedidos de reposición a proveedores
-- Al pasar a 'RECIBIDA' se suma el stock de los ingredientes.
-- ---------------------------------------------------------------------
CREATE TABLE compras (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    proveedor_id     INT NOT NULL,
    usuario_id       INT NOT NULL,
    fecha            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_recepcion  DATETIME NULL,
    estado           ENUM('PENDIENTE','RECIBIDA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    total            DECIMAL(12,0) NOT NULL DEFAULT 0,
    observaciones    VARCHAR(255) NULL,
    CONSTRAINT ck_compras_total CHECK (total >= 0),
    CONSTRAINT fk_compras_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (id),
    CONSTRAINT fk_compras_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuarios (id)
) ENGINE=InnoDB;

CREATE TABLE detalle_compra (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    compra_id        INT NOT NULL,
    ingrediente_id   INT NOT NULL,
    cantidad         DECIMAL(10,3) NOT NULL,
    costo_unitario   DECIMAL(12,0) NOT NULL DEFAULT 0,
    subtotal         DECIMAL(12,0) NOT NULL DEFAULT 0,
    CONSTRAINT uq_detalle_compra_ingrediente UNIQUE (compra_id, ingrediente_id),
    CONSTRAINT ck_detalle_compra_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_compra_costo    CHECK (costo_unitario >= 0),
    CONSTRAINT fk_detalle_compra_compra FOREIGN KEY (compra_id)
        REFERENCES compras (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_compra_ingrediente FOREIGN KEY (ingrediente_id)
        REFERENCES ingredientes (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Historial de movimientos de stock (auditoría de ajustes y compras)
-- cantidad positiva = entrada, negativa = salida
-- ---------------------------------------------------------------------
CREATE TABLE movimientos_stock (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    ingrediente_id    INT NOT NULL,
    usuario_id        INT NOT NULL,
    compra_id         INT NULL,
    fecha             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo              ENUM('AJUSTE','COMPRA') NOT NULL,
    cantidad          DECIMAL(10,3) NOT NULL,
    stock_resultante  DECIMAL(10,3) NOT NULL,
    motivo            VARCHAR(200) NULL,
    CONSTRAINT ck_movimientos_cantidad CHECK (cantidad <> 0),
    CONSTRAINT fk_movimientos_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingredientes (id),
    CONSTRAINT fk_movimientos_usuario     FOREIGN KEY (usuario_id)     REFERENCES usuarios (id),
    CONSTRAINT fk_movimientos_compra      FOREIGN KEY (compra_id)      REFERENCES compras (id)
) ENGINE=InnoDB;
