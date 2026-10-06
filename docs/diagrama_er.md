# Diagrama entidad-relación — Tortas y Chocolates 3B

Generado a partir de `database/01_schema.sql`. Si el script cambia, este diagrama se actualiza en la misma confirmación (*commit*).

Se ve como diagrama en GitHub, GitLab o VS Code con la extensión "Markdown Preview Mermaid Support". Para exportarlo como imagen se puede pegar en https://mermaid.live.

```mermaid
erDiagram
    usuarios {
        INT id PK
        VARCHAR nombre
        VARCHAR username UK
        VARCHAR password_hash
        ENUM rol "ADMIN | OPERADOR"
        TINYINT activo
        DATETIME creado_en
    }
    clientes {
        INT id PK
        VARCHAR nombre
        VARCHAR telefono UK
        VARCHAR direccion
        VARCHAR email
        VARCHAR observaciones
        TINYINT activo
        DATETIME creado_en
    }
    productos {
        INT id PK
        VARCHAR nombre UK
        VARCHAR descripcion
        ENUM categoria "TORTA | CUPCAKE | CHOCOLATE | OTRO"
        DECIMAL precio
        DECIMAL costo_kilo
        DECIMAL peso_kg
        TINYINT activo
    }
    proveedores {
        INT id PK
        VARCHAR nombre UK
        VARCHAR telefono
        VARCHAR direccion
        VARCHAR insumos
        TINYINT activo
    }
    ingredientes {
        INT id PK
        VARCHAR nombre UK
        ENUM unidad
        DECIMAL stock_actual
        DECIMAL stock_minimo
        INT proveedor_id FK
        TINYINT activo
    }
    pedidos {
        INT id PK
        INT cliente_id FK
        INT usuario_id FK
        DATETIME fecha_pedido
        DATE fecha_entrega
        TIME hora_entrega
        ENUM tipo_entrega "RETIRO | DELIVERY"
        ENUM estado
        DECIMAL total
        VARCHAR observaciones
    }
    detalle_pedido {
        INT id PK
        INT pedido_id FK
        INT producto_id FK
        INT cantidad
        DECIMAL precio_unitario
        DECIMAL subtotal
    }
    delivery {
        INT id PK
        INT pedido_id FK,UK
        VARCHAR direccion
        VARCHAR telefono_contacto
        ENUM estado
        VARCHAR observaciones
        DATETIME actualizado_en
    }
    ventas {
        INT id PK
        INT pedido_id FK,UK
        INT usuario_id FK
        DATETIME fecha
        ENUM metodo_pago "EFECTIVO | TRANSFERENCIA | OTRO"
        DECIMAL total
    }
    compras {
        INT id PK
        INT proveedor_id FK
        INT usuario_id FK
        DATETIME fecha
        DATETIME fecha_recepcion
        ENUM estado "PENDIENTE | RECIBIDA | CANCELADA"
        DECIMAL total
        VARCHAR observaciones
    }
    detalle_compra {
        INT id PK
        INT compra_id FK
        INT ingrediente_id FK
        DECIMAL cantidad
        DECIMAL costo_unitario
        DECIMAL subtotal
    }
    movimientos_stock {
        INT id PK
        INT ingrediente_id FK
        INT usuario_id FK
        INT compra_id FK
        DATETIME fecha
        ENUM tipo "AJUSTE | COMPRA"
        DECIMAL cantidad
        DECIMAL stock_resultante
        VARCHAR motivo
    }

    clientes     ||--o{ pedidos           : "realiza"
    usuarios     ||--o{ pedidos           : "registra"
    pedidos      ||--|{ detalle_pedido    : "contiene"
    productos    ||--o{ detalle_pedido    : "aparece en"
    pedidos      ||--o| delivery          : "se envía por"
    pedidos      ||--o| ventas            : "se cobra en"
    usuarios     ||--o{ ventas            : "registra"
    proveedores  ||--o{ ingredientes      : "provee habitualmente"
    proveedores  ||--o{ compras           : "recibe"
    usuarios     ||--o{ compras           : "registra"
    compras      ||--|{ detalle_compra    : "contiene"
    ingredientes ||--o{ detalle_compra    : "aparece en"
    ingredientes ||--o{ movimientos_stock : "tiene"
    usuarios     ||--o{ movimientos_stock : "registra"
    compras      |o--o{ movimientos_stock : "origina"
```

## Resumen de relaciones

| Relación | Cardinalidad | FK |
|---|---|---|
| clientes → pedidos | 1 : N | pedidos.cliente_id |
| usuarios → pedidos / ventas / compras / movimientos_stock | 1 : N | usuario_id |
| pedidos → detalle_pedido | 1 : N (borrado en cascada) | detalle_pedido.pedido_id |
| productos → detalle_pedido | 1 : N | detalle_pedido.producto_id |
| pedidos → delivery | 1 : 0..1 | delivery.pedido_id (UNIQUE) |
| pedidos → ventas | 1 : 0..1 | ventas.pedido_id (UNIQUE) |
| proveedores → ingredientes | 1 : N (opcional; SET NULL) | ingredientes.proveedor_id |
| proveedores → compras | 1 : N | compras.proveedor_id |
| compras → detalle_compra | 1 : N (borrado en cascada) | detalle_compra.compra_id |
| ingredientes → detalle_compra | 1 : N | detalle_compra.ingrediente_id |
| ingredientes → movimientos_stock | 1 : N | movimientos_stock.ingrediente_id |
| compras → movimientos_stock | 1 : N (opcional) | movimientos_stock.compra_id |
