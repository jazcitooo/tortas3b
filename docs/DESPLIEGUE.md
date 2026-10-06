# Publicar el sistema en internet

El sistema queda accesible desde cualquier celular o computadora con un link `https://...onrender.com`.

| Servicio | Para qué | Plan |
|---|---|---|
| GitHub | Guarda el código; Render lo toma de ahí | Gratis |
| Supabase | Base de datos PostgreSQL | Gratis (500 MB) |
| Render | Ejecuta el sistema (servidor web con HTTPS) | Gratis |
| UptimeRobot | Visita `/health` cada 5 minutos para que Render no se duerma y Supabase no se pause | Gratis |

En la PC el sistema sigue funcionando igual con MySQL (perfil `local`). En Render usa PostgreSQL (perfil `prod`).

---

## 1. Supabase (base de datos)

1. Entrar a <https://supabase.com> y crear una cuenta.
2. **New project**:
   - Name: `tortas3b`.
   - Database Password: generar una y **guardarla** (se usa en el paso 3).
   - Region: **South America (São Paulo)**, la más cercana a Paraguay.
3. Cuando el proyecto esté listo, abrir **SQL Editor** → **New query**.
4. Pegar todo el contenido de [`database/postgres/01_schema.sql`](../database/postgres/01_schema.sql) y pulsar **Run**.
5. En otra consulta nueva, pegar y ejecutar **uno** de estos:
   - [`database/postgres/02_datos_prueba.sql`](../database/postgres/02_datos_prueba.sql): con los datos ficticios de ejemplo (para mostrar o probar).
   - [`database/postgres/02_usuario_inicial.sql`](../database/postgres/02_usuario_inicial.sql): solo el usuario `admin`, sin datos (para uso real).
6. Pulsar **Connect** (arriba) → pestaña **Session pooler** (no "Direct connection": Render no la alcanza). Anotar:
   - **Host**, por ejemplo `aws-0-sa-east-1.pooler.supabase.com`
   - **Port**: `5432`
   - **User**, por ejemplo `postgres.abcdefghijklmn`

El script activa la protección de filas (RLS) en todas las tablas. Así la API pública de Supabase no puede leer los datos; el sistema sí, porque se conecta como `postgres`.

## 2. GitHub (código)

Hace falta Git instalado en la PC (<https://git-scm.com/download/win>) y una cuenta en <https://github.com>.

1. En GitHub: **New repository** → nombre `tortas3b` → **Private** → **Create repository** (sin README).
2. En la carpeta del proyecto, en una terminal:

```powershell
git init
git add .
git commit -m "Sistema Tortas y Chocolates 3B"
git branch -M main
git remote add origin https://github.com/USUARIO/tortas3b.git
git push -u origin main
```

`application-local.properties` (con la contraseña de MySQL) está en `.gitignore` y **no** se sube.

## 3. Render (servidor)

1. Entrar a <https://render.com> con la cuenta de GitHub.
2. **New** → **Web Service** → elegir el repositorio `tortas3b`.
3. Configurar:
   - Language: **Docker** (usa el `Dockerfile` del proyecto).
   - Region: **Virginia (US East)**.
   - Instance Type: **Free**.
4. En **Environment Variables** agregar:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST:5432/postgres?sslmode=require` (HOST del paso 1.6) |
| `DB_USER` | El User del paso 1.6 (`postgres.xxxxx`) |
| `DB_PASSWORD` | La contraseña de la base del paso 1.2 |

5. En **Advanced** → **Health Check Path**: `/health`.
6. **Deploy Web Service**. La primera vez tarda unos 5 a 10 minutos. Al terminar aparece el link, por ejemplo `https://tortas3b.onrender.com`.
7. Abrir el link, entrar con `admin` / `password` y **cambiar la contraseña enseguida** (Usuarios → Editar). Hacer lo mismo con `operador` si se cargaron los datos de prueba.

Cada `git push` a `main` vuelve a publicar el sistema automáticamente.

## 4. UptimeRobot (mantenerlo despierto)

El plan gratuito de Render apaga el servidor tras 15 minutos sin visitas; la siguiente visita tarda cerca de un minuto. Supabase pausa el proyecto tras 7 días sin uso.

1. Entrar a <https://uptimerobot.com> y crear una cuenta.
2. **New monitor**:
   - Monitor Type: **HTTP(s)**.
   - URL: `https://tortas3b.onrender.com/health` (con el link de Render).
   - Monitoring Interval: **5 minutes**.
3. **Create Monitor**. Si el sistema se cae, UptimeRobot avisa por correo.

`/health` responde `ok` y hace una consulta a la base, así mantiene activos a Render y a Supabase.

---

## Problemas frecuentes

| Síntoma | Causa probable |
|---|---|
| En Render, el log dice `Connection refused` o `timeout` hacia Supabase | Se usó "Direct connection". Usar el host del **Session pooler**. |
| `password authentication failed` | `DB_USER` debe ser `postgres.xxxxx` (con el código del proyecto), no solo `postgres`. |
| Las fechas de "hoy" están corridas | Volver a ejecutar las dos primeras líneas de `01_schema.sql` (`ALTER DATABASE ... timezone`). |
| `/health` responde 503 | El servidor anda pero no llega a la base: revisar las tres variables `DB_*`. |
| La primera visita tarda un minuto | Render estaba dormido: revisar que el monitor de UptimeRobot esté activo. |
