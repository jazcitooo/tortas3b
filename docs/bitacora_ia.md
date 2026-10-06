# Bitácora de uso de IA — Tortas y Chocolates 3B

| Fecha | Herramienta | Etapa | Qué se pidió | Qué se obtuvo | Revisión del equipo |
|---|---|---|---|---|---|
| 05/10/2026 | Antigravity AI / Claude | 1 | Seguir el "Prompt Maestro", con Java + MySQL en lugar de Flask | Plan de implementación (`docs/PLAN_IMPLEMENTACION.md`) con 6 decisiones a aprobar | Se aprobaron todas las decisiones. |
| 05/10/2026 | Antigravity AI / Claude | 2 | Base de datos y archivos iniciales | Scripts SQL (`01_schema.sql`, `02_datos_prueba.sql`), proyecto Maven, modelos, layout, CSS/JS base, diagrama ER inicial | Probado en MySQL/XAMPP. Scripts sin errores; la app compila y conecta; restricciones activas. |
| 06/10/2026 | Antigravity AI | 3 | Login, roles y seguridad con Spring Security | Autenticación basada en formulario, hash BCrypt, control estricto de roles (`ADMIN` y `OPERADOR`), CRUD de usuarios y pantalla personalizada 403 Forbidden | Verificado login con `admin` y `operador`, redirección al dashboard y bloqueo 403 a rutas protegidas. |
| 06/10/2026 | Antigravity AI | 4 | CRUD de clientes, productos, proveedores e ingredientes con alertas y ajuste de stock | Repositorios, servicios y controladores para las 4 entidades clave, control de stock mínimo con alertas rojas y módulo de ajuste manual auditado en `movimientos_stock` | Probado con filtros por categoría en productos, validaciones de teléfono único y no permitir stock negativo. |
| 06/10/2026 | Antigravity AI | 5 | Pedidos, detalle dinámico, límite de 5 entregas/hora y flujo de producción | `PedidoService` transaccional con `SELECT ... FOR UPDATE`, cálculo de subtotales en base de datos, JavaScript interactivo para cálculo en frontend, flujo completo de estados y modal de delivery | Validado bloqueo del 6to pedido en franja horaria completa (14:00 - 14:59) y sincronización con delivery. |
| 06/10/2026 | Antigravity AI | 6 | Ventas/caja, delivery y compras de reposición | Módulo de caja diaria con métodos de pago (Efectivo/Transferencia/Otro), anulación de ventas para admin, seguimiento logístico de delivery y compras con suma automática al inventario al recibirlas | Verificado que solo se puede registrar una venta por pedido y que compras `RECIBIDA` suman existencias. |
| 06/10/2026 | Antigravity AI | 7 | Dashboard dinámico, reportes analíticos con impresión y filtros | Tarjetas de métricas, próximas entregas, API REST `/api/dashboard` con refresco cada 60s, reportes de entregas, ganancias (fórmula 38%) y productos más vendidos con CSS `@media print` | Métricas probadas en tiempo real y totales correctos con los datos de prueba. |
| 06/10/2026 | Antigravity AI | 8 | Pruebas automáticas JUnit 5 y validación integral | 6 pruebas unitarias con JUnit 5 y Mockito para reglas de negocio (5 entregas/hora, ganancia 38%, no permitir stock negativo, cálculo de totales) | `mvn test` ejecutado con éxito (6 pruebas pasadas, 0 fallos). |
| 06/10/2026 | Antigravity AI | 9 | Documentación técnica, manual y diagramas | `README.md` actualizado, `diagrama_er.md`, `plan_pruebas.md`, `manual_usuario.md` y bitácora completa | Todo documentado y organizado en `docs/`. |
| 06/10/2026 | Antigravity AI | 10 | Guion para la defensa oral | Guion paso a paso (`docs/guion_defensa.md`) con roles para cada integrante del equipo, demostración en vivo y respuestas a preguntas de la mesa evaluadora | Listo para la exposición final. |

---

## Decisiones Técnicas y de Negocio

1. **Stack Java + Spring Boot + JdbcTemplate:** Se utilizó SQL explícito parametrizado (`?`) para máxima transparencia pedagógica y protección contra SQL Injection, sin cajas negras de JPA.
2. **Cálculo de Ganancia (38%):** Se configuró `negocio.porcentaje-ganancia=0.38` en `application.properties`. Las ganancias se obtienen de `ingresos × 0.38`, mostrando los kilos como dato volumétrico informativo.
3. **Límite de 5 Entregas por Hora:** Cuenta retiros y deliveries juntos en la franja horaria (`HOUR(hora_entrega)`). Se implementó control transaccional para evitar condiciones de carrera concurrentes.
4. **Manejo de Stock:** No se inventaron recetas no especificadas; el inventario se gestiona a través de compras recibidas y ajustes manuales auditados en `movimientos_stock`.
5. **Seguridad Robusta:** Hash seguro BCrypt para todas las credenciales, sesiones con cookies HttpOnly, protección CSRF y páginas de error que no exponen trazas técnicas.
