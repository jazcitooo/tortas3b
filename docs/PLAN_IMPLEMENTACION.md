# Plan de implementación — Sistema de gestión "Tortas y Chocolates 3B"

**Etapa 1 (análisis y planificación).** Este documento no contiene código del sistema. Se espera aprobación antes de pasar a la Etapa 2.

---

## 0. Cambio de tecnología solicitado

El prompt recomienda Python + Flask. Por pedido del equipo se usará **Java + MySQL**. Para que el código siga siendo simple y explicable se propone:

| Capa | Tecnología | Por qué |
|---|---|---|
| Lenguaje | Java 17 (LTS) | Versión estable, soportada por Spring Boot 3 |
| Framework web | Spring Boot 3 (Spring MVC) | Servidor embebido: se ejecuta con un solo comando, sin instalar Tomcat aparte |
| Vistas | Thymeleaf + HTML5 + CSS3 + JavaScript | Plantillas HTML normales, fáciles de leer; responsive con CSS propio |
| Acceso a datos | Spring `JdbcTemplate` con SQL escrito a mano | El SQL queda visible y explicable; todas las consultas son parametrizadas (`?`), lo que protege contra SQL Injection |
| Seguridad | Spring Security + BCrypt | Hash seguro de contraseñas, sesiones, control de roles y protección CSRF |
| Base de datos | MySQL 8 | La que pide el prompt |
| Build | Maven (con *Maven Wrapper* `mvnw`) | No hace falta instalar Maven a mano |
| Versionado | Git | La que pide el prompt |

Se descarta JPA/Hibernate a propósito: genera SQL "invisible" y es más difícil de defender en la exposición oral.

**Estado del equipo de desarrollo (verificado):** Java, Maven, MySQL y Git **no están instalados**. Antes de la Etapa 2 hay que instalar:
1. JDK 17 (por ejemplo, Eclipse Temurin 17).
2. MySQL Server 8 + MySQL Workbench (o XAMPP; MariaDB también es compatible).
3. Git for Windows.
4. (Opcional) IntelliJ IDEA Community o la extensión "Extension Pack for Java" de VS Code.

---

## 1. Arquitectura

Arquitectura en capas (MVC):

```
Navegador (HTML/CSS/JS)
   │  formularios y fetch() para el dashboard
   ▼
Controladores (@Controller)   → reciben la petición, validan y eligen la vista
   ▼
Servicios (@Service)          → reglas de negocio (límite 5/hora, totales, ganancia, stock)
   ▼
Repositorios (JdbcTemplate)   → SQL parametrizado
   ▼
MySQL 8 (tortas3b)
```

Reglas: los controladores no tienen SQL, los repositorios no tienen reglas de negocio y las vistas no calculan nada.

## 2. Estructura de carpetas

```
tortas3b/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
├── README.md
├── .gitignore                      (excluye application-local.properties y target/)
├── database/
│   ├── 01_schema.sql               (creación de tablas)
│   └── 02_datos_prueba.sql         (datos ficticios)
├── docs/
│   ├── PLAN_IMPLEMENTACION.md
│   ├── diagrama_er.md              (Mermaid, igual a 01_schema.sql)
│   ├── plan_pruebas.md
│   ├── manual_usuario.md
│   └── bitacora_ia.md
└── src/main/
    ├── java/py/tortas3b/
    │   ├── Tortas3bApplication.java
    │   ├── config/        SecurityConfig.java, WebConfig.java
    │   ├── model/         Usuario, Cliente, Producto, Pedido, DetallePedido,
    │   │                  Ingrediente, Proveedor, Compra, DetalleCompra, Venta, Delivery
    │   ├── repository/    un repositorio por tabla (ClienteRepository, ...)
    │   ├── service/       ClienteService, PedidoService, InventarioService,
    │   │                  VentaService, ReporteService, DashboardService, ...
    │   ├── controller/    AuthController, DashboardController, ClienteController, ...
    │   └── util/          Validaciones, Formatos (moneda Gs., fechas)
    └── resources/
        ├── application.properties          (sin contraseñas)
        ├── application-local.properties.example
        ├── templates/
        │   ├── layout.html                 (menú, encabezado, mensajes)
        │   ├── login.html, dashboard.html
        │   ├── clientes/  productos/  pedidos/  ingredientes/
        │   ├── proveedores/  compras/  ventas/  delivery/  usuarios/
        │   └── reportes/
        └── static/
            ├── css/estilos.css
            └── js/ (app.js, pedido-form.js, dashboard.js)
```

## 3. Base de datos (MySQL, 12 tablas, 3FN)

Convenciones: PK `id` INT AUTO_INCREMENT; montos en `DECIMAL(12,0)` (guaraníes sin decimales); cantidades en `DECIMAL(10,3)`; borrado lógico con `activo TINYINT(1)`; motor InnoDB; `utf8mb4`.

| Tabla | Campos principales | Restricciones |
|---|---|---|
| **usuarios** | id, nombre, username, password_hash, rol, activo, creado_en | username UNIQUE; rol ENUM('ADMIN','OPERADOR') |
| **clientes** | id, nombre, telefono, direccion, email, observaciones, activo, creado_en | telefono NOT NULL; UNIQUE(telefono) para evitar duplicados |
| **productos** | id, nombre, descripcion, categoria, precio, costo_kilo, peso_kg, activo | categoria ENUM('TORTA','CUPCAKE','CHOCOLATE','OTRO'); precio > 0; peso_kg > 0; UNIQUE(nombre) |
| **pedidos** | id, cliente_id (FK), usuario_id (FK), fecha_pedido, fecha_entrega, hora_entrega, tipo_entrega, estado, total, observaciones | tipo_entrega ENUM('RETIRO','DELIVERY'); estado ENUM('PENDIENTE','CONFIRMADO','EN_PREPARACION','LISTO','EN_CAMINO','ENTREGADO','CANCELADO'); INDEX(fecha_entrega, hora_entrega) |
| **detalle_pedido** | id, pedido_id (FK), producto_id (FK), cantidad, precio_unitario, subtotal | cantidad > 0; precio_unitario guarda el precio del momento; ON DELETE CASCADE con pedido |
| **delivery** | id, pedido_id (FK, UNIQUE), direccion, telefono_contacto, estado, observaciones, actualizado_en | 1 a 1 con pedidos de tipo DELIVERY; estado ENUM('PENDIENTE','PREPARANDO','EN_CAMINO','ENTREGADO','CANCELADO') |
| **ventas** | id, pedido_id (FK, UNIQUE), usuario_id (FK), fecha, metodo_pago, total | metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','OTRO'); una venta por pedido |
| **proveedores** | id, nombre, telefono, direccion, insumos, activo | UNIQUE(nombre); insumos = descripción en texto |
| **ingredientes** | id, nombre, unidad, stock_actual, stock_minimo, proveedor_id (FK, opcional), activo | unidad ENUM('KG','G','L','ML','UNIDAD'); stock >= 0; UNIQUE(nombre) |
| **compras** (pedidos de reposición) | id, proveedor_id (FK), usuario_id (FK), fecha, estado, total, observaciones | estado ENUM('PENDIENTE','RECIBIDA','CANCELADA') |
| **detalle_compra** | id, compra_id (FK), ingrediente_id (FK), cantidad, costo_unitario, subtotal | cantidad > 0 |

| **movimientos_stock** (D5 aprobada) | id, ingrediente_id (FK), usuario_id (FK), compra_id (FK, opcional), fecha, tipo, cantidad (+/−), stock_resultante, motivo | tipo ENUM('AJUSTE','COMPRA') |

### Relaciones
- clientes 1—N pedidos
- usuarios 1—N pedidos, ventas y compras (quién lo registró)
- pedidos 1—N detalle_pedido N—1 productos
- pedidos 1—0..1 delivery
- pedidos 1—0..1 ventas
- proveedores 1—N compras 1—N detalle_compra N—1 ingredientes
- proveedores 1—N ingredientes (proveedor habitual, opcional)
- ingredientes 1—N movimientos_stock; compras 1—N movimientos_stock (opcional); usuarios 1—N movimientos_stock

El diagrama ER se generará en Mermaid desde el mismo `01_schema.sql` para que coincida exactamente.

## 4. Roles y permisos

| Módulo | Administrador | Operador |
|---|---|---|
| Dashboard | ✔ | ✔ (sin datos de ganancia) |
| Usuarios | CRUD | ✘ |
| Clientes | CRUD | Consultar, crear y editar (no desactivar) |
| Productos | CRUD | Solo consultar |
| Pedidos | CRUD y cambio de estado | Registrar, consultar, editar y cambiar estado |
| Delivery | ✔ | Consultar y actualizar estado |
| Ventas | ✔ (incluye anular) | Registrar y consultar |
| Ingredientes / Inventario | ✔ | ✘ |
| Proveedores / Compras | ✔ | ✘ |
| Reportes | ✔ | Solo entregas diarias |

Control doble: el menú oculta lo no permitido y además el servidor bloquea la URL (Spring Security, `/admin/**` y reglas por ruta). Un acceso no permitido muestra la página 403 "No tiene permiso".

## 5. Módulos y pantallas

| # | Pantalla | Contenido |
|---|---|---|
| 1 | Login | usuario, contraseña, mensaje de error genérico |
| 2 | Dashboard | tarjetas: pedidos pendientes, pedidos de hoy, ventas del día (Gs.), productos activos, ingredientes con stock bajo; tabla "próximas entregas" (las siguientes 8). Se refresca cada 60 s vía `fetch('/api/dashboard')` |
| 3 | Clientes | lista + búsqueda (nombre/teléfono), formulario, ficha con historial de pedidos |
| 4 | Productos | lista con filtro por categoría, formulario |
| 5 | Pedidos | lista ordenada por fecha → hora → estado, filtros (fecha, estado, tipo de entrega); formulario con filas de productos dinámicas (JS calcula subtotales y total para mostrar, el servidor los recalcula); detalle; cambio de estado |
| 6 | Delivery | lista de entregas a domicilio del día/fecha, cambio de estado |
| 7 | Ventas / Caja | registrar venta desde un pedido, lista filtrada por fecha, total del día |
| 8 | Ingredientes | lista con alerta roja de stock bajo, formulario, ajuste manual (+/−) con motivo |
| 9 | Proveedores | CRUD |
| 10 | Compras (reposición) | crear pedido a proveedor con ingredientes; al marcar "Recibida" se suma el stock |
| 11 | Usuarios | CRUD (solo admin), cambio de contraseña |
| 12 | Reportes | Entregas diarias, Ganancias diarias, Productos más vendidos (adicional) |

## 6. Flujos principales

**Pedido**
1. Elegir cliente (o crearlo).
2. Agregar productos y cantidades.
3. Elegir fecha, hora y tipo de entrega (si es delivery, cargar dirección).
4. Guardar. El servidor, dentro de una transacción:
   1. Valida los datos.
   2. Bloquea y cuenta los pedidos no cancelados de esa fecha y franja horaria.
   3. Si ya hay 5, rechaza con el mensaje *"Ya hay 5 entregas programadas para el 12/10/2026 entre 14:00 y 14:59. Elija otro horario."*
   4. Recalcula los precios desde la BD.
   5. Inserta el pedido, su detalle y el delivery.
5. El pedido avanza por los estados: Pendiente → Confirmado → En preparación → Listo → (En camino) → Entregado, o pasa a Cancelado.

**Límite de 5 por hora**
- Se cuentan los pedidos con la misma `fecha_entrega` y `HOUR(hora_entrega)`, con `estado <> 'CANCELADO'`, excluyendo el propio pedido si se está editando.
- La consulta usa `SELECT ... FOR UPDATE` para que dos operadores no puedan cargar el 6.º pedido al mismo tiempo.
- La regla también se aplica al editar la fecha u hora y al reactivar un pedido cancelado.

**Venta**
- Pedido entregado (o listo para retiro) → "Registrar venta" → método de pago.
- El total se toma del pedido.
- Un pedido no puede tener dos ventas, y un pedido cancelado no puede venderse.

**Delivery**
- Al crear un pedido DELIVERY se crea su registro de delivery.
- Al marcar el delivery como "Entregado", el pedido también pasa a "Entregado". Al cancelar el pedido, se cancela el delivery.

**Reposición**
- Compra PENDIENTE → RECIBIDA: suma `cantidad` al `stock_actual` de cada ingrediente (transacción).

**Stock bajo**
- `stock_actual <= stock_minimo` → alerta en la lista de ingredientes y en el dashboard.

## 7. Cálculo de ganancia (38 %)

Fórmulas propuestas (se muestran en el reporte):

```
Ingresos del día         = Σ ventas.total del día
Cantidad vendida (kg)    = Σ (detalle_pedido.cantidad × productos.peso_kg) de los pedidos vendidos ese día
Ganancia calculada       = Ingresos × 0,38
```

El 38 % se guarda en `application.properties` (`negocio.porcentaje-ganancia=0.38`) para poder cambiarlo sin tocar código. Ver Decisión D1.

## 8. Reportes (con filtros)

1. **Entregas diarias.** Columnas: fecha, hora, cliente, productos, tipo de entrega, estado y total. Filtros: fecha (o rango) y estado. Orden: hora.
2. **Ganancias diarias.** Columnas: fecha, n.º de ventas, ingresos, kg vendidos y ganancia. Filtro: fecha o rango. Incluye una fila de totales y la fórmula visible.
3. **Adicional:** productos más vendidos por rango de fechas. Ayuda a planificar la producción.

Los tres reportes tienen botón "Imprimir" (CSS de impresión) para guardarlos como PDF desde el navegador.

## 9. Validaciones

| Tipo | Frontend (HTML5/JS) | Backend (servicio) |
|---|---|---|
| Obligatorios | `required` | verificación en el servicio y mensaje por campo |
| Teléfono | `pattern` (solo dígitos, de 7 a 15) | misma expresión regular |
| Precios / cantidades | `type=number min` | > 0 y numérico |
| Fechas / horas | `min` = hoy | la entrega no puede ser pasada; horario de atención configurable (por defecto 08:00–20:00) |
| Relaciones | selects con datos activos | se verifica que el cliente/producto exista y esté activo |
| Duplicados | — | username, teléfono de cliente y nombres de producto/ingrediente/proveedor |
| Stock | — | un ajuste no puede dejar el stock negativo |
| Límite 5/hora | — | regla transaccional (sección 6) |

Los mensajes se muestran como avisos de color: verde = éxito, rojo = error, amarillo = advertencia. Las acciones destructivas (desactivar, cancelar) piden confirmación en una ventana del propio sitio.

## 10. Seguridad

- Contraseñas con BCrypt. Nunca se guardan ni se muestran en texto plano.
- Sesión del lado del servidor: cookie HttpOnly, expiración a los 30 minutos de inactividad y cierre de sesión.
- Protección CSRF en todos los formularios (Spring Security).
- Autorización por rol en el servidor, no solo en el menú.
- Solo SQL parametrizado (`JdbcTemplate` con `?`). Nunca se concatena texto del usuario.
- Thymeleaf escapa el HTML automáticamente, lo que da protección básica contra XSS.
- Las credenciales de la BD se leen de variables de entorno o de `application-local.properties` (ignorado por Git). En el repositorio solo hay un `.example`.
- Las páginas de error no muestran trazas técnicas.

## 11. Plan de pruebas (resumen; detalle en `docs/plan_pruebas.md`)

| Área | Casos |
|---|---|
| Login | credenciales correctas, incorrectas, campos vacíos, usuario inactivo |
| Roles | el operador entra a `/admin/usuarios` y recibe 403; el menú muestra solo lo permitido |
| CRUD (×4) | crear, editar, desactivar, buscar; duplicados; vacíos; valores negativos |
| Pedido | cálculo de subtotal y total; fecha pasada; sin productos; cliente inactivo |
| Límite 5/hora | 5 pedidos se guardan y el 6.º se rechaza; si se cancela uno, el 6.º entra; al editar la hora hacia una franja llena, se rechaza |
| Inventario | alerta cuando stock = mínimo; un ajuste que deja el stock negativo se rechaza; recibir una compra suma stock |
| Venta | venta duplicada; pedido cancelado; filtro por fecha |
| Delivery | delivery entregado → pedido entregado |
| Reportes | los totales coinciden con los datos de prueba; los filtros funcionan |
| Persistencia | reiniciar la app y los datos siguen ahí |

Pruebas automáticas mínimas con JUnit sobre las reglas de negocio (límite 5/hora, cálculo de ganancia, stock) y pruebas manuales documentadas con capturas.

## 12. Datos de prueba (ficticios)

- 2 usuarios: `admin` y `operador`. Sus contraseñas se documentan solo en el README.
- 10 clientes inventados.
- 12 productos (tortas, cupcakes y chocolates).
- 15 ingredientes, 3 de ellos con stock bajo.
- 4 proveedores.
- 2 compras.
- 25 pedidos repartidos en distintos días y estados, con una franja que ya tiene 5 pedidos para demostrar el límite.
- 12 ventas.

## 13. Etapas

| Etapa | Entregable |
|---|---|
| 1 | Este plan ← **estamos aquí** |
| 2 | Proyecto Maven, `01_schema.sql`, `02_datos_prueba.sql`, conexión a la BD, layout y CSS base |
| 3 | Login, roles, BCrypt, CRUD de usuarios, página 403 |
| 4 | CRUD de clientes, productos, proveedores e ingredientes (más ajuste de stock y alerta) |
| 5 | Pedidos, detalle, límite 5/hora, estados, organización de entregas |
| 6 | Ventas/caja, delivery y compras de reposición |
| 7 | Dashboard dinámico, reportes y filtros |
| 8 | Pruebas, corrección de errores y pulido visual/responsive |
| 9 | README, diagrama ER, plan de pruebas, manual de usuario y bitácora de IA |
| 10 | Guion para la defensa oral (qué explica cada integrante) |

## 14. Riesgos

| Riesgo | Mitigación |
|---|---|
| El equipo no tiene el entorno instalado | Guía de instalación paso a paso en el README (Etapa 2) |
| Spring "mágico" y difícil de explicar | JdbcTemplate con SQL visible, sin JPA, y comentarios en los puntos clave |
| Dos pedidos simultáneos superan el límite | Transacción con `SELECT ... FOR UPDATE` |
| El diagrama ER queda desactualizado | Se genera al final, desde el script SQL definitivo |
| Ambigüedad del 38 % | Decisión D1 explícita y porcentaje configurable |

---

## 15. Decisiones (APROBADAS el 05/10/2026: "sí a todo")

Consecuencias de la aprobación:
- D2: se cuentan retiros y deliveries juntos.
- D5: se agrega la tabla `movimientos_stock` (12 tablas en total).
- D6: Spring Boot.

Ajustes menores de la Etapa 2:
- "Anular venta" (solo admin) elimina el registro de venta, con confirmación. Así se mantiene `UNIQUE(pedido_id)`.
- No se incluye Maven Wrapper. Se ejecuta con IntelliJ IDEA (trae Maven integrado) o con Maven instalado.


- **D1. Ganancia del 38 %.** Cupcakes y chocolates no se venden por kilo. Propuesta: ganancia = ingresos × 0,38. Los kilos se muestran como dato informativo usando `peso_kg` por producto.
- **D2. "5 entregas por hora".** Propuesta: se cuentan **retiros y deliveries** juntos (es un límite de producción) por franja horaria completa (14:00–14:59). ¿O solo los deliveries?
- **D3. Pedidos sin consumo automático de ingredientes.** El análisis no define recetas, así que el stock se maneja con ajustes manuales y compras recibidas. Agregar recetas sería inventar un requisito.
- **D4. Estados de delivery vs. pedido.** Son listas distintas en el prompt. Propuesta: se mantienen separadas y se sincronizan solo en "Entregado" y "Cancelado".
- **D5. Historial de movimientos de stock.** Opcional: una tabla `movimientos_stock` para auditar ajustes. No se incluye salvo aprobación.
- **D6. Stack Java.** Spring Boot + Thymeleaf + JdbcTemplate. La alternativa más "clásica" es Servlets + JSP + JDBC puro sobre Tomcat: más código repetitivo y la seguridad se hace a mano.
