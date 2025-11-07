# ✅ CONFIGURACIÓN DEFINITIVA - Gmail + SendGrid

## 🎯 Objetivo

- **LOCAL (desarrollo):** Enviar correos con Gmail
- **PRODUCCIÓN (Render):** Enviar correos con SendGrid

---

## 📧 PARTE 1: Configurar y probar LOCAL con Gmail

### ✅ Paso 1: Verificar `mail.config`

Ya lo tienes en: `src/main/resources/mail.config`

Verifica que tenga:
```properties
mail.remitente=jofre.cuerda@gmail.com
mail.contrasena=msezzjyvjhrviaoa
```

✅ Ya está configurado correctamente.

### ✅ Paso 2: Ejecutar la aplicación en local

**Opción A - Desde el script:**
```
Doble clic en: run-local.bat
```

**Opción B - Desde tu IDE:**
1. Abre `Main.java`
2. Run Configuration → VM options: `-Dspring.profiles.active=supabase`
3. Run

### ✅ Paso 3: Probar envío de correos

1. Abre: http://localhost:8080
2. Pestaña **Personas** → Crea 2-3 personas con emails reales (mejor tuyos)
3. Pestaña **Partidas** → Crea una partida con esas personas
4. Los correos se envían automáticamente

### 👀 Verificar en los logs:

✅ **Funciona:**
```
✓ Correo enviado a xxx@gmail.com correctamente [1/100 emails hoy]
✓ Correo enviado a yyy@gmail.com correctamente [2/100 emails hoy]
```

❌ **Error:**
```
✗ Error al enviar correo a xxx@gmail.com: 535 Authentication failed
```
→ Revisa la contraseña de aplicación en `mail.config`

### 📧 Revisa Gmail

Los correos deberían llegar con asunto: `Partida Killer Kiss: [nombre]`

---

## 🚀 PARTE 2: Configurar PRODUCCIÓN con SendGrid en Render

Una vez que funcione en local, configura Render:

### ✅ Paso 1: Variables de entorno en Render

Ve a: https://dashboard.render.com → Tu servicio → **Environment**

Añade estas **2 variables**:

| Variable | Valor |
|----------|-------|
| `SENDGRID_API_KEY` | `SG.xxxxxxxxxxxxxxxxxxxxxxxxxx` (tu API Key) |
| `MAIL_FROM` | `jofre.cuerda@gmail.com` (email verificado en SendGrid) |

**IMPORTANTE:**
- `SENDGRID_API_KEY` debe empezar con `SG.`
- `MAIL_FROM` debe ser el email que verificaste como Single Sender en SendGrid

### ✅ Paso 2: Verificar Single Sender en SendGrid

1. Ve a: https://app.sendgrid.com/
2. Settings → Sender Authentication → Single Sender Verification
3. Verifica que `jofre.cuerda@gmail.com` esté **Verified** ✅
4. Si no está verificado, completa el proceso (revisa tu email)

### ✅ Paso 3: Obtener API Key de SendGrid

Si no tienes la API Key:

1. Ve a: https://app.sendgrid.com/
2. Settings → API Keys
3. Create API Key
4. Nombre: `Killer Kiss Render`
5. Permisos: **Full Access** (o al menos Mail Send)
6. Create & View
7. **Copia la key completa** (empieza con `SG.`)
8. Pégala en Render como `SENDGRID_API_KEY`

⚠️ **IMPORTANTE:** Solo la verás una vez, guárdala bien.

### ✅ Paso 4: Guardar y redeployar

1. En Render, haz clic en **Save Changes**
2. Render redesplegará automáticamente tu app
3. Espera 5-10 minutos a que termine el deploy

### ✅ Paso 5: Verificar en los logs de Render

Ve a: Tu servicio en Render → **Logs**

Busca:
```
✓ Correo enviado a xxx@ejemplo.com correctamente [1/100 emails hoy]
```

Si ves errores:
```
✗ Error: 535 Authentication failed
```
→ La API Key está mal o no está configurada

```
✗ Error: Sender not verified
```
→ El email en `MAIL_FROM` no está verificado en SendGrid

---

## 📊 Resumen de configuración

### LOCAL (Gmail)
```
Perfil: supabase
SMTP: smtp.gmail.com:587
Usuario: jofre.cuerda@gmail.com (desde mail.config)
Password: contraseña de aplicación (desde mail.config)
Remitente: jofre.cuerda@gmail.com
```

### PRODUCCIÓN (SendGrid)
```
Perfil: prod
SMTP: smtp.sendgrid.net:587
Usuario: apikey (fijo)
Password: ${SENDGRID_API_KEY} (variable de entorno)
Remitente: ${MAIL_FROM} (variable de entorno)
```

---

## ✅ Variables de entorno necesarias en Render

| Variable | ¿Obligatoria? | Valor |
|----------|---------------|-------|
| `SENDGRID_API_KEY` | ✅ SÍ | Tu API Key de SendGrid (SG.xxx...) |
| `MAIL_FROM` | ✅ SÍ | Email verificado (jofre.cuerda@gmail.com) |
| `SPRING_PROFILES_ACTIVE` | ✅ SÍ | `prod` (ya en render.yaml) |
| `DATABASE_URL` | ✅ SÍ | Automático desde BD |
| `DB_USERNAME` | ✅ SÍ | Automático desde BD |
| `DB_PASSWORD` | ✅ SÍ | Automático desde BD |

---

## 🎯 Checklist final

### Para LOCAL:
- [x] `mail.config` existe y tiene credenciales
- [ ] Ejecutar app con perfil `supabase`
- [ ] Probar envío de correos
- [ ] Ver `✓ Correo enviado` en logs
- [ ] Recibir correos en Gmail

### Para PRODUCCIÓN:
- [ ] Single Sender verificado en SendGrid
- [ ] API Key generada en SendGrid
- [ ] `SENDGRID_API_KEY` configurada en Render
- [ ] `MAIL_FROM` configurada en Render
- [ ] Deploy completado en Render
- [ ] Verificar logs sin errores
- [ ] Probar envío de correos en producción

---

## 🚨 Si algo falla

**Local no envía:**
1. Verifica `mail.config` existe en `src/main/resources/`
2. Verifica contraseña de aplicación de Gmail
3. Prueba generar nueva contraseña de aplicación

**Producción no envía:**
1. Verifica `SENDGRID_API_KEY` en Render
2. Verifica `MAIL_FROM` en Render
3. Verifica Single Sender verificado en SendGrid
4. Revisa logs de Render para ver el error exacto

---

## 🎉 Siguiente paso

1. **Prueba en LOCAL primero** para asegurarte de que el código funciona
2. Una vez funcione, configura Render con SendGrid
3. ¡Listo! Tendrás correos funcionando en ambos entornos

**¿Dudas?** Copia el error de los logs y te ayudo.
