# 🚀 Configuración de Variables de Entorno en Render

## Variables Requeridas para Despliegue en Render

Para que tu aplicación funcione correctamente en Render, debes configurar las siguientes variables de entorno:

### 1. Base de Datos (PostgreSQL/Supabase)

Configura estas 3 variables en Render:

```bash
DATABASE_URL=jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres?ssl=true&sslmode=require
DB_USERNAME=postgres.zldkbsmzelnwsqlglmjc
DB_PASSWORD=super3
```

**IMPORTANTE:**
- La URL debe empezar con `jdbc:postgresql://` (NO incluir usuario:contraseña en la URL)
- El usuario y contraseña van en variables separadas
- Copia estos valores desde tu configuración de Supabase

### 2. Configuración de Correo (Gmail)

```bash
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-contraseña-de-aplicacion-gmail
```

**Cómo obtener la contraseña de aplicación de Gmail:**
1. Ve a tu cuenta de Google: https://myaccount.google.com/
2. **Seguridad** → **Verificación en dos pasos** (actívala si no está activada)
3. **Contraseñas de aplicaciones**
4. Genera una nueva para "Correo" y "Otro (nombre personalizado): Killer Kiss"
5. Copia la contraseña de 16 caracteres que te genera

### 3. Perfil de Spring (Opcional)

```bash
SPRING_PROFILES_ACTIVE=prod
```

---

## 📝 Cómo Configurar Variables en Render

1. Ve a tu servicio en Render Dashboard
2. Haz clic en **"Environment"** en el menú lateral
3. Haz clic en **"Add Environment Variable"**
4. Añade cada variable con su valor correspondiente
5. Haz clic en **"Save Changes"**
6. Render redesplegará automáticamente tu aplicación

---

## 🏠 Desarrollo Local

Para desarrollo local, el archivo `mail.config` sigue funcionando:

```properties
# mail.config (en src/main/resources/)
mail.remitente=tu-email@gmail.com
mail.contrasena=tu-contraseña-de-aplicacion

supabase.url=jdbc:postgresql://...
supabase.username=postgres.xxx
supabase.password=xxx
```

**IMPORTANTE:** `mail.config` está en `.gitignore` y NO debe subirse a Git por seguridad.

---

## 🔍 Verificación

Tu aplicación ahora funciona de esta manera:

- **Local:** Lee desde `mail.config` (si existe)
- **Producción (Render):** Lee desde variables de entorno
- **Fallback:** Si no encuentra ni archivo ni variables, usa valores por defecto

### Orden de Prioridad:

1. Variables de entorno (`MAIL_USERNAME`, `DATABASE_URL`, etc.)
2. Archivo `mail.config` (solo local)
3. Valores por defecto (si están configurados)

---

## ✅ Checklist de Despliegue

- [ ] `DATABASE_URL` configurada en Render
- [ ] `MAIL_USERNAME` configurada en Render
- [ ] `MAIL_PASSWORD` configurada en Render
- [ ] `SPRING_PROFILES_ACTIVE=prod` configurada en Render
- [ ] `mail.config` NO está en el repositorio (verificar con `git status`)
- [ ] Aplicación redesplegada en Render

---

## 🐛 Troubleshooting

**Error: "contraseñas al mail.config"**
- Verifica que las variables `MAIL_USERNAME` y `MAIL_PASSWORD` estén configuradas en Render
- Asegúrate de que la contraseña sea una "Contraseña de aplicación" de Gmail, no tu contraseña normal

**Error de conexión a base de datos**
- Verifica que `DATABASE_URL` esté en formato JDBC: `jdbc:postgresql://...`
- Comprueba que la URL incluya `?ssl=true&sslmode=require` si usas Supabase

**La aplicación no arranca**
- Revisa los logs en Render: Settings → Logs
- Busca mensajes como "Failed to configure a DataSource"
