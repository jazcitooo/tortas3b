# Plan y Resultados de Pruebas — Tortas y Chocolates 3B

> **Estado:** Completado (Etapa 8 y 9)  
> **Herramientas:** JUnit 5, Mockito, Spring Test, Navegador Web y scripts de prueba automatizados.

---

## 1. Resumen Ejecutivo de Pruebas

Se diseñaron y ejecutaron pruebas automatizadas y manuales sobre los módulos críticos del sistema:

| Categoría | Casos Diseñados | Casos Ejecutados | Resultado |
|---|---|---|---|
| **Seguridad y Control de Acceso** | 5 | 5 | ✅ 100% Exitoso |
| **Límite de 5 entregas/hora** | 4 | 4 | ✅ 100% Exitoso |
| **Cálculo de Totales y Precios** | 3 | 3 | ✅ 100% Exitoso |
| **Gestión de Stock y Movimientos** | 4 | 4 | ✅ 100% Exitoso |
| **Flujo de Ventas y Arqueo de Caja** | 3 | 3 | ✅ 100% Exitoso |
| **Reportes y Fórmula de Ganancia (38%)** | 3 | 3 | ✅ 100% Exitoso |
| **Pruebas Automatizadas (Maven Surefire)** | 6 | 6 | ✅ 100% Exitoso (0 fallos) |

---

## 2. Pruebas Automatizadas Unitarias (JUnit 5 + Mockito)

Las pruebas unitarias se ubican en `src/test/java/py/tortas3b/service/` y validan las reglas del negocio sin depender de una base de datos externa:

### 2.1. `PedidoServiceTest`
- `debeRechazarCuandoSeSuperaLimiteCincoEntregasPorHora`: Simula 5 pedidos en una misma franja horaria (ej. 14:00 - 14:59). Verifica que al intentar registrar un sexto pedido se lance `IllegalStateException` con el mensaje exacto: *"Ya hay 5 entregas programadas para el ... entre 14:00 y 14:59. Elija otro horario."*
- `debePermitirCuandoHayMenosDeCincoEntregas`: Verifica que con 4 pedidos existentes en la misma hora, el quinto sea aceptado sin excepciones.
- `debeRecalcularTotalesDesdeBaseDeDatos`: Valida que los subtotales y el total del pedido se recalculen utilizando los precios unitarios almacenados en el catálogo de productos (evitando alteraciones maliciosas desde el cliente).

### 2.2. `InventarioServiceTest`
- `debeRechazarAjusteQueDejeStockNegativo`: Simula un inventario con 3 kg de stock y un ajuste negativo de -4 kg. Verifica que se lance `IllegalArgumentException` y se bloquee la transacción, sin actualizar stock ni registrar movimientos.
- `debePermitirAjusteValidoYRegistrarMovimiento`: Verifica que un ajuste positivo (+2.5 kg) actualice el stock a 5.5 kg y cree un registro en `movimientos_stock` con `tipo = 'AJUSTE'` y motivo de auditoría.

### 2.3. `ReporteServiceTest`
- `debeCalcularGananciaTreintaYOchoPorCiento`: Verifica que para un ingreso de 1.000.000 Gs., la ganancia neta estimada calculada sea exactamente 380.000 Gs. (`1.000.000 × 0.38`), según la propiedad configurada `negocio.porcentaje-ganancia=0.38`.

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running py.tortas3b.service.InventarioServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running py.tortas3b.service.PedidoServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running py.tortas3b.service.ReporteServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 3. Matriz de Pruebas Funcionales y de Seguridad

| ID | Módulo | Caso de Prueba | Entrada | Comportamiento Esperado | Estado |
|---|---|---|---|---|---|
| **CP-01** | Autenticación | Login con credenciales válidas (Admin) | `admin` / `password` | Redirige al Dashboard mostrando rol ADMIN | ✅ Aprobado |
| **CP-02** | Autenticación | Login con credenciales válidas (Operador) | `operador` / `password` | Redirige al Dashboard mostrando rol OPERADOR | ✅ Aprobado |
| **CP-03** | Autenticación | Login con contraseña errónea | `admin` / `invalido` | Muestra alerta roja de credenciales incorrectas | ✅ Aprobado |
| **CP-04** | Seguridad | Acceso de Operador a módulo protegido | GET `/usuarios` | Código HTTP 403 Forbidden | ✅ Aprobado |
| **CP-05** | Seguridad | Menú dinámico según rol | Sesión de Operador | Oculta enlaces de Inventario, Compras, Proveedores y Usuarios | ✅ Aprobado |
| **CP-06** | Clientes | Validación de teléfono duplicado | Teléfono ya registrado | Mensaje de error bloqueando duplicados | ✅ Aprobado |
| **CP-07** | Productos | Validación de precio mayor a cero | Precio = 0 | Rechazado por validación de formulario y servicio | ✅ Aprobado |
| **CP-08** | Pedidos | Límite de 5 entregas/hora | 6to pedido en franja llena | Mensaje de rechazo descriptivo | ✅ Aprobado |
| **CP-09** | Pedidos | Cálculo dinámico en navegador | Seleccionar 2 tortas de 120.000 | JS calcula 240.000 Gs. al instante | ✅ Aprobado |
| **CP-10** | Pedidos | Reactivación de pedido cancelado | Pasar de CANCELADO a PENDIENTE | Revalida cupo de 5 entregas antes de reactivar | ✅ Aprobado |
| **CP-11** | Delivery | Sincronización al entregar | Delivery pasa a ENTREGADO | Pedido pasa automáticamente a ENTREGADO | ✅ Aprobado |
| **CP-12** | Ventas | Cobro único por pedido | Cobrar pedido ya vendido | Mensaje de error: ya tiene venta registrada | ✅ Aprobado |
| **CP-13** | Ventas | Anulación de cobro (Admin) | Botón "Anular" en ventas | Elimina venta y habilita cobro nuevamente | ✅ Aprobado |
| **CP-14** | Inventario | Alerta visual de stock bajo | Stock actual <= stock mínimo | Fila resaltada en rojo en lista e indicador en dashboard | ✅ Aprobado |
| **CP-15** | Compras | Recepción y suma de existencias | Marcar compra como RECIBIDA | Suma cantidades al stock y registra en movimientos | ✅ Aprobado |
| **CP-16** | Reportes | Impresión amigable | Clic en "Imprimir" | Oculta barras y menús vía CSS `@media print` | ✅ Aprobado |
