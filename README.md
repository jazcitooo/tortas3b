# Tortas y Chocolates 3B — Sistema de gestión web
> Estado: **Sistema 100% Completado y Operativo** (Las 10 etapas de desarrollo, pruebas y documentación finalizadas).

## Descripción

"Tortas y Chocolates 3B" es una microempresa familiar del barrio San Miguel, en Coronel Oviedo. Elabora tortas, cupcakes y chocolates artesanales por pedido. Este sistema web reemplaza el registro manual. Centraliza pedidos, clientes, productos, inventario, ventas, delivery, proveedores y reportes.

## Objetivo

Brindar un sistema web sencillo, intuitivo y adaptable a computadora y teléfono. El sistema organiza la producción y respeta el límite de 5 entregas por hora. También controla el stock de ingredientes y registra ventas y ganancias.

## Integrantes

| Nombre | Rol en el proyecto |
|---|---|
| _(completar)_ | _(completar)_ |

## Tecnologías

- Java 17 + Spring Boot 3.3 (Spring MVC)
- Thymeleaf, HTML5, CSS3 y JavaScript
- MySQL 8 con acceso mediante `JdbcTemplate` y SQL parametrizado
- Spring Security + BCrypt (desde la Etapa 3)
- Maven y Git

## Requisitos

- **JDK 17**, por ejemplo Eclipse Temurin 17.
- **MySQL Server 8.0.16 o superior.** Se recomienda también MySQL Workbench.
- **IntelliJ IDEA Community** (recomendado: trae Maven integrado) o Apache Maven 3.9+.
- **Git.**

Instalación rápida en Windows con `winget` (en PowerShell). Si algún identificador cambió, buscarlo con `winget search <nombre>`.

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
winget install Oracle.MySQL
winget install JetBrains.IntelliJIDEA.Community
winget install Git.Git
```

## Instalación y carga de la base de datos

1. Abrir MySQL Workbench y conectarse al servidor local.
2. Abrir y ejecutar `database/01_schema.sql`.
   - **Atención:** este script borra la base `tortas3b` si ya existe.
3. Abrir y ejecutar `database/02_datos_prueba.sql`.
   - Las fechas de los pedidos se calculan desde el día en que se ejecuta el script.

Desde la línea de comandos, el equivalente es:

```powershell
mysql -u root -p < database/01_schema.sql
mysql -u root -p < database/02_datos_prueba.sql
```

## Configuración

1. Copiar `src/main/resources/application-local.properties.example` como `application-local.properties`, en la misma carpeta.
2. Escribir en ese archivo el usuario y la contraseña de su MySQL.
3. Ese archivo **no se sube a Git** (está en `.gitignore`).

Alternativa: definir las variables de entorno `DB_USER` y `DB_PASSWORD`.

Las reglas del negocio se configuran en `application.properties`:

| Propiedad | Valor | Significado |
|---|---|---|
| `negocio.porcentaje-ganancia` | 0.38 | 38 % de ganancia |
| `negocio.max-entregas-por-hora` | 5 | límite de producción |
| `negocio.hora-apertura` / `negocio.hora-cierre` | 08:00 / 20:00 | horario permitido para entregas |

## Ejecución

- **IntelliJ:** abrir la carpeta del proyecto (detecta `pom.xml`) y ejecutar `Tortas3bApplication`.
- **Maven:** `mvn spring-boot:run`

Luego abrir http://localhost:8080. En la Etapa 2, la página de inicio muestra la cantidad de registros de cada tabla. Así se confirma que la conexión funciona.

## Publicación en internet

El sistema se puede publicar con GitHub, Supabase (PostgreSQL), Render y UptimeRobot, todos en plan gratuito. Los pasos están en [docs/DESPLIEGUE.md](docs/DESPLIEGUE.md). Los scripts de PostgreSQL están en `database/postgres/`.

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `password` | Administrador |
| `operador` | `password` | Operador |

Son credenciales ficticias, solo para pruebas. El login se habilita en la Etapa 3.

## Estructura de carpetas

```
tortas3b/
├── database/        scripts SQL (esquema y datos de prueba)
├── docs/            plan, diagrama ER, bitácora de IA (luego: manual y pruebas)
├── pom.xml          dependencias Maven
└── src/main/
    ├── java/py/tortas3b/
    │   ├── config/      configuración (seguridad, etc.)
    │   ├── model/       una clase por tabla
    │   ├── repository/  consultas SQL
    │   ├── service/     reglas del negocio
    │   ├── controller/  rutas web
    │   └── util/        utilidades
    └── resources/
        ├── templates/   páginas HTML (Thymeleaf)
        └── static/      css/ y js/
```

> Estado: **Sistema Completo (Etapas 1 a 10 finalizadas y operativas)**.

## Avance por etapas

- [x] Etapa 1 — Plan de implementación ([docs/PLAN_IMPLEMENTACION.md](file:///c:/Users/emman/tortas3b/docs/PLAN_IMPLEMENTACION.md))
- [x] Etapa 2 — Base de datos y archivos iniciales (`database/01_schema.sql`, `database/02_datos_prueba.sql`)
- [x] Etapa 3 — Login, roles (`ADMIN` / `OPERADOR`), BCrypt, CRUD de usuarios y página 403
- [x] Etapa 4 — CRUD de clientes, productos, proveedores e inventario con alerta roja y ajustes de stock
- [x] Etapa 5 — Pedidos, detalle dinámico, límite transaccional de 5 entregas/hora y estados de producción
- [x] Etapa 6 — Ventas/caja diaria, logística de delivery y compras de reposición con suma automática de stock
- [x] Etapa 7 — Dashboard dinámico (auto-refresco cada 60s) y reportes analíticos con impresión (entregas, ganancias 38% y ranking)
- [x] Etapa 8 — Pruebas automáticas JUnit 5 (6 pruebas exitosas, 0 fallos) y pulido visual responsive
- [x] Etapa 9 — Documentación técnica, manual de usuario ([docs/manual_usuario.md](file:///c:/Users/emman/tortas3b/docs/manual_usuario.md)) y plan de pruebas ([docs/plan_pruebas.md](file:///c:/Users/emman/tortas3b/docs/plan_pruebas.md))
- [x] Etapa 10 — Guion para la defensa oral con roles y preguntas frecuentes ([docs/guion_defensa.md](file:///c:/Users/emman/tortas3b/docs/guion_defensa.md))

## Uso de IA

Ver bitácora detallada en [docs/bitacora_ia.md](file:///c:/Users/emman/tortas3b/docs/bitacora_ia.md).
