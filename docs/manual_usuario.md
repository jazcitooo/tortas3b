# Manual de Usuario — Tortas y Chocolates 3B

Bienvenido al manual del usuario del sistema web de gestión de la pastelería artesanal **Tortas y Chocolates 3B** (Coronel Oviedo, Paraguay).

---

## 1. Acceso al Sistema (Login)

1. Abra su navegador web e ingrese a: `http://localhost:8080` (o la dirección del servidor local).
2. El sistema lo redirigirá a la pantalla de inicio de sesión:
   - **Administrador:** Usuario `admin`, Contraseña `password`.
   - **Operador:** Usuario `operador`, Contraseña `password`.
3. Haga clic en **Ingresar al Sistema**.

---

## 2. Roles y Permisos

El sistema implementa dos perfiles de usuario con permisos diferenciados:

| Módulo | Administrador | Operador |
|---|---|---|
| **Dashboard** | Métricas operativas y recaudación financiera en Gs. | Métricas operativas (sin datos de dinero) |
| **Pedidos** | Registro, edición, cancelación y avance de estados | Registro, edición y avance de estados |
| **Delivery** | Control y cambio de estados de envío | Control y cambio de estados de envío |
| **Clientes** | Registro, edición, ficha y desactivación | Registro, edición y consulta de historial |
| **Productos** | Alta, edición de precios y pesos, desactivación | Consulta de catálogo y precios |
| **Ventas / Caja** | Cobro de pedidos y anulación de cobros | Cobro de pedidos y consulta diaria |
| **Inventario** | Control de existencias, alertas y ajustes manuales | Sin acceso |
| **Compras** | Órdenes a proveedores y recepción con suma de stock | Sin acceso |
| **Proveedores** | Alta, edición y desactivación de proveedores | Sin acceso |
| **Usuarios** | Creación de cuentas, cambio de contraseña y roles | Sin acceso (pantalla 403) |
| **Reportes** | Entregas, Ganancias (38%) y Productos más vendidos | Reporte de entregas diarias |

---

## 3. Módulos del Sistema

### 3.1. Dashboard (Panel de Control)
- Muestra tarjetas de resumen: pedidos pendientes, entregas programadas para hoy, ventas cobradas hoy, ingredientes con stock crítico y catálogo activo.
- **Próximas entregas:** Lista las siguientes 8 entregas pendientes ordenadas por fecha y hora.
- Se actualiza automáticamente cada 60 segundos sin necesidad de recargar la página.

### 3.2. Clientes
- **Listado y Búsqueda:** Permite buscar clientes en tiempo real por nombre o teléfono de WhatsApp.
- **Ficha del Cliente:** Al pulsar sobre el nombre o en "Ficha", se visualizan los datos de contacto y el historial completo de pedidos que ha realizado.
- **Nuevo Cliente:** Requiere nombre y un número de teléfono válido (7 a 15 dígitos numéricos).

### 3.3. Catálogo de Productos
- Catálogo de tortas, cupcakes y chocolates artesanales.
- Filtros rápidos por categoría: *Tortas*, *Cupcakes*, *Chocolates* y *Otros*.
- Cada producto tiene su precio de venta en guaraníes (Gs.) y su peso aproximado en kilogramos (para el cálculo de producción).

### 3.4. Gestión de Pedidos (Regla de Producción)
1. Ingrese a **Pedidos** y haga clic en **➕ Nuevo Pedido**.
2. **Seleccione el cliente** (o cree uno nuevo rápidamente).
3. **Fecha y Hora de Entrega:**
   - La fecha no puede ser anterior al día de hoy.
   - El horario de atención permitido es de **08:00 a 20:00**.
   - **Regla de oro (Límite de producción):** El sistema permite un **máximo de 5 entregas por hora** (incluyendo retiros y deliveries). Si una franja horaria ya cuenta con 5 pedidos, el sistema bloqueará el registro informando: *"Ya hay 5 entregas programadas... Elija otro horario."*
4. **Modalidad de Entrega:**
   - **Retiro en local:** El cliente retira en la pastelería.
   - **Delivery:** Se despliega el formulario para cargar dirección de entrega, teléfono de contacto e indicaciones para el repartidor.
5. **Selección de Productos:**
   - Agregue los productos deseados y sus cantidades.
   - El formulario calcula automáticamente los subtotales y el total estimado en tiempo real.
6. Guarde el pedido.

### 3.5. Ficha del Pedido y Flujo de Estados
Desde la vista de detalle de cada pedido puede avanzar su ciclo de vida:
```
[PENDIENTE] ➔ [CONFIRMADO] ➔ [EN PREPARACIÓN] ➔ [LISTO] ➔ [EN CAMINO] ➔ [ENTREGADO]
                                                                  └─ (Solo si es Delivery)
```
- Cualquier pedido puede pasar a **CANCELADO**, liberando automáticamente el cupo de la franja horaria.
- Si el pedido es tipo delivery, marcar el envío como *Entregado* sincroniza el estado del pedido automáticamente.

### 3.6. Caja y Cobro de Ventas
1. Ingrese al detalle de un pedido listo o entregado.
2. Haga clic en el botón destacado **💵 Registrar Cobro en Caja**.
3. Seleccione el método de pago: *Efectivo*, *Transferencia Bancaria (SIPAP/QR)* o *Tarjeta/Otro*.
4. El sistema registra el ingreso en la caja diaria y vincula la venta al pedido.
5. Los administradores pueden anular cobros erróneos con confirmación de seguridad.

### 3.7. Inventario y Ajustes Manuales (Solo Administrador)
- Lista los ingredientes y materias primas (harinas, chocolates, dulce de leche, etc.).
- **Alerta de Stock Bajo:** Cuando el stock actual es menor o igual al stock mínimo de seguridad, la fila se resalta en **rojo** y se notifica en el Dashboard.
- **Ajuste Manual:** Permite sumar o restar existencias físicas (por ejemplo por compras menores o mermas).
  - El sistema **nunca permite que el stock quede en negativo**.
  - Todo ajuste exige ingresar un motivo que queda registrado en el **Historial de Movimientos de Stock** para auditoría.

### 3.8. Compras y Reposición a Proveedores (Solo Administrador)
1. Ingrese a **Compras** ➔ **➕ Nueva Orden de Compra**.
2. Seleccione el proveedor y agregue los ingredientes con cantidades y costos acordados.
3. La orden se crea en estado `PENDIENTE`.
4. Al recibir la mercadería en el local, ingrese al detalle de la orden y pulse **📦 Recibir y Sumar al Stock**.
5. El sistema aumentará automáticamente las cantidades en el inventario y generará los asientos de auditoría con tipo `COMPRA`.

### 3.9. Reportes Analíticos e Impresión
Todos los reportes cuentan con el botón **🖨️ Imprimir Reporte** con diseño optimizado para imprimir o guardar en formato PDF:
1. **Reporte de Entregas Diarias:** Listado cronológico de pedidos del día con filtros por estado y cliente.
2. **Reporte de Rentabilidad y Ganancias:**
   - Aplica la fórmula del negocio: `Ganancia = Ingresos × 38%`.
   - Muestra volumen producido en kilos y fila de totales del período.
3. **Ranking de Productos Más Vendidos:** Top de productos por volumen de unidades vendidas y facturación acumulada.
