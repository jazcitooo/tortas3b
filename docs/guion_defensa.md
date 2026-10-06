# Guion para la Defensa Oral — Tortas y Chocolates 3B

> **Proyecto:** Sistema de Gestión Web para la microempresa familiar "Tortas y Chocolates 3B" (Coronel Oviedo).  
> **Duración sugerida:** 15 a 20 minutos (10 min presentación + 5-10 min preguntas).

---

## 1. Distribución de Roles entre Integrantes

Si el equipo cuenta con 2 a 4 integrantes, se recomienda la siguiente división temática:

| Integrante | Rol Temático | Secciones a Exponer |
|---|---|---|
| **Integrante 1** | Líder / Contexto de Negocio | Introducción, problemática de la pastelería, objetivos, usuarios y alcance del sistema. |
| **Integrante 2** | Arquitecto de Software | Arquitectura MVC, modelo de base de datos relacional (12 tablas), JdbcTemplate y seguridad Spring Security. |
| **Integrante 3** | Desarrollador de Negocio | Regla central del negocio: límite de 5 entregas/hora, flujo de pedidos, delivery y arqueo de ventas. |
| **Integrante 4** (o compartido) | Calidad / Operaciones | Inventario con alertas, compras con suma de stock, reportes financieros (38%) y demostración en vivo. |

---

## 2. Guion de Exposición Paso a Paso

### 2.1. Introducción y Planteamiento del Problema (2 minutos)
* **Palabras del expositor:**  
  *"Buenos días a los miembros de la mesa evaluadora. Presentamos el sistema de gestión web para 'Tortas y Chocolates 3B', una microempresa familiar ubicada en el barrio San Miguel de Coronel Oviedo. Anteriormente, la pastelería registraba sus pedidos en cuadernos y notas de WhatsApp, lo que generaba tres problemas críticos: sobrecarga de producción en horarios pico, falta de control del stock de ingredientes perecederos y dificultad para conocer las ganancias netas reales. Diseñamos este sistema para digitalizar y ordenar completamente su operación."*

### 2.2. Arquitectura Tecnológica y Base de Datos (3 minutos)
* **Puntos clave a mencionar:**
  1. **Stack:** Java 17 + Spring Boot 3 con servidor Tomcat embebido.
  2. **Acceso a Datos:** `JdbcTemplate` con SQL parametrizado (`?`), garantizando total visibilidad de las consultas, rendimiento óptimo y protección contra inyecciones SQL (sin la sobrecarga de un ORM complejo).
  3. **Base de Datos:** MySQL 8 en Tercera Forma Normal (3FN), compuesta por 12 tablas con restricciones de clave foránea (`FOREIGN KEY`), unicidad (`UNIQUE`) y verificación de datos (`CHECK`).
  4. **Seguridad:** Spring Security con encriptación de contraseñas mediante **BCrypt**, sesiones con cookies seguras `HttpOnly`, protección contra CSRF y control estricto de roles (`ADMIN` y `OPERADOR`).

### 2.3. Demostración en Vivo del Sistema (7 minutos)

Siga exactamente este recorrido frente a la mesa examinadora:

#### Paso 1: Inicio de Sesión y Control de Roles
1. Ingrese con el usuario `operador` y contraseña `password`.
2. Muestre cómo el menú oculta automáticamente los módulos sensibles (Inventario, Compras, Proveedores, Usuarios).
3. Intente ingresar manualmente por URL a `http://localhost:8080/usuarios` para demostrar la protección en el servidor: el sistema devuelve la pantalla personalizada **403 - Acceso Denegado**.
4. Cierre sesión e ingrese con `admin` / `password`. El menú despliega todas las funciones habilitadas.

#### Paso 2: Panel de Control Dinámico (Dashboard)
1. Muestre las tarjetas de métricas en tiempo real: pedidos pendientes, entregas programadas de hoy, ventas cobradas y la tabla de **Próximas entregas**.
2. Mencione que el panel se actualiza automáticamente cada 60 segundos mediante una llamada asíncrona a la API REST `/api/dashboard`.

#### Paso 3: Registro de Pedido y Límite de 5 Entregas por Hora (¡Punto central!)
1. Vaya a **Pedidos ➔ Nuevo Pedido**.
2. Seleccione un cliente y configure fecha de entrega para hoy o mañana.
3. Agregue productos: demuestre que al cambiar cantidades, el JavaScript calcula los subtotales y el total al vuelo.
4. **Demostración de la regla de producción:**
   - Seleccione una hora donde ya existan 5 pedidos (por ejemplo, las 14:00 según los datos de prueba).
   - Al pulsar "Guardar Pedido", el sistema bloquea la transacción y muestra el mensaje claro:  
     `"Ya hay 5 entregas programadas para el [fecha] entre 14:00 y 14:59. Elija otro horario."`
   - Explique al jurado: *"Esto protege la capacidad física del horno y del equipo de decoradores de la pastelería."*
5. Cambie la hora a las 16:00 y guarde el pedido satisfactoriamente.

#### Paso 4: Ciclo de Producción, Delivery y Cobro
1. Desde la ficha del pedido recién creado, cambie el estado: `PENDIENTE` ➔ `CONFIRMADO` ➔ `EN PREPARACIÓN` ➔ `LISTO`.
2. Si es delivery, demuestre cómo desde el módulo **Delivery** al marcar el envío como `ENTREGADO`, el pedido principal se actualiza automáticamente.
3. Pulse **💵 Registrar Cobro en Caja**, seleccione método de pago (*Efectivo* o *Transferencia*) y confirme el ingreso.
4. Vaya a **Ventas / Caja** para verificar el nuevo cobro sumado al arqueo total del día.

#### Paso 5: Inventario, Alertas y Recepción de Compras
1. Ingrese a **Inventario**: muestre la fila resaltada en **rojo** (`🚨 STOCK BAJO`) para aquellos ingredientes con existencias menores o iguales al stock mínimo.
2. Demuestre un **Ajuste Manual**:
   - Intente restar más cantidad de la disponible para mostrar que el sistema rechaza existencias negativas.
   - Aplique un ajuste válido con motivo y muestre el asiento en el **Historial de Movimientos de Stock**.
3. Ingrese a **Compras**: abra una orden pendiente y pulse **📦 Recibir y Sumar al Stock**. Muestre cómo el inventario aumenta inmediatamente en forma atómica.

#### Paso 6: Reportes Analíticos e Impresión
1. Abra **Reporte de Ganancias**:
   - Muestre la fórmula visible: `Ganancia = Ingresos × 38%` (configurable en `application.properties`).
   - Muestre los kilos totales calculados a partir del peso unitario de cada producto.
2. Haga clic en **🖨️ Imprimir Reporte**: demuestre cómo el CSS `@media print` oculta los menús y botones, dejando un informe limpio listo para exportar a PDF o enviar a impresora física.

---

## 3. Preguntas Frecuentes de la Mesa Evaluadora y Respuestas Modelo

### P1: *"¿Por qué eligieron JdbcTemplate en lugar de JPA/Hibernate?"*
* **Respuesta:**  
  *"Elegimos `JdbcTemplate` para garantizar que todo el SQL sea explícito, comprensible y controlable. En una defensa académica, JPA puede generar consultas complejas o 'invisibles' difíciles de auditar. Con `JdbcTemplate` aseguramos que todas las consultas sean parametrizadas con `?`, previniendo inyecciones SQL al 100%, y tenemos control milimétrico sobre bloqueos concurrentes como `SELECT ... FOR UPDATE` para el límite de entregas."*

### P2: *"¿Cómo aseguran que dos usuarios simultáneos no reserven el sexto pedido en la misma hora?"*
* **Respuesta:**  
  *"El método `registrar` en `PedidoService` está anotado con `@Transactional`. Dentro de la misma transacción, ejecutamos una consulta con bloqueo pesimista `SELECT COUNT(*) FROM pedidos ... FOR UPDATE`. De esta manera, el motor MySQL adquiere un bloqueo exclusivo sobre los registros de esa fecha y hora hasta que se confirme la transacción, impidiendo que una segunda petición concurrente supere el cupo máximo de 5."*

### P3: *"¿Dónde se define el 38% de ganancia y qué pasa si el negocio decide cambiarlo?"*
* **Respuesta:**  
  *"El porcentaje de ganancia no está escrito a fuego en el código Java. Se encuentra desacoplado en el archivo `application.properties` bajo la propiedad `negocio.porcentaje-ganancia=0.38`. Si la pastelería reajusta su margen al 40%, solo basta con modificar este valor en la configuración sin necesidad de recompilar la aplicación."*

### P4: *"¿Cómo se protegen las contraseñas de los usuarios?"*
* **Respuesta:**  
  *"Utilizamos el algoritmo de cifrado `BCryptPasswordEncoder` provisto por Spring Security con factor de costo 10 y generación automática de salt criptográfico. Las contraseñas nunca se transmiten ni se almacenan en texto plano en la base de datos."*

### P5: *"¿Cuentan con pruebas automatizadas?"*
* **Respuesta:**  
  *"Sí, desarrollamos una suite de pruebas unitarias con JUnit 5 y Mockito que valida las reglas críticas del negocio: el rechazo estricto del sexto pedido por hora, la prevención de stock en negativo, el cálculo seguro de totales y la fórmula del 38% de rentabilidad. Todas las pruebas pasan con 100% de éxito en Maven."*
