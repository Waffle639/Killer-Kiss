# ✅ Configuración SendGrid en Render - Checklist Completo

## 📋 Resumen de lo que necesitas

Para que el envío de correos funcione en producción (Render) con SendGrid necesitas:

### 1️⃣ En SendGrid:
- ✅ Cuenta creada en SendGrid (Free Plan - 100 emails/día)
- ✅ Single Sender Verification completado
  - Email verificado: `jofre.cuerda@gmail.com` (según screenshot que mostraste)
- ✅ API Key generada

### 2️⃣ En Render:
Configurar estas **3 variables de entorno**:

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `SENDGRID_API_KEY` | `SG.xxxxx...` | Tu API Key de SendGrid (empieza con SG.) |
| `MAIL_FROM` | `jofre.cuerda@gmail.com` | Email verificado como Single Sender |
| `SPRING_PROFILES_ACTIVE` | `prod` | Ya está en render.yaml |

---

## 🔧 Pasos para configurar en Render

### Paso 1: Ir a tu servicio en Render
1. Ve a https://dashboard.render.com
2. Selecciona tu servicio `killer-kiss`
3. Ve a la pestaña **Environment**

### Paso 2: Añadir variables de entorno

#### Variable 1: SENDGRID_API_KEY
```
Key: SENDGRID_API_KEY
Value: SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```
**⚠️ IMPORTANTE:** 
- Copia la API Key COMPLETA desde SendGrid
- Debe empezar con `SG.`
- No añadas comillas ni espacios

#### Variable 2: MAIL_FROM
```
Key: MAIL_FROM
Value: jofre.cuerda@gmail.com
```
**⚠️ IMPORTANTE:**
- Usa el mismo email que verificaste en SendGrid como Single Sender
- SendGrid SOLO permite enviar desde emails verificados

#### Variable 3: Verificar SPRING_PROFILES_ACTIVE
```
Key: SPRING_PROFILES_ACTIVE
Value: prod
```
Esta ya debería estar configurada.

### Paso 3: Guardar y redeployar
1. Haz clic en **Save Changes**
2. Render redesplegará automáticamente tu app
3. Espera a que termine el deploy (5-10 minutos)

---

## 🧪 Cómo verificar que funciona

### En los logs de Render:
Busca estas líneas cuando se inicie la aplicación:

```
✓ Configuración de correo cargada correctamente
✓ Host: smtp.sendgrid.net
✓ Puerto: 587
✓ Usuario: apikey
```

### Al enviar un correo:
En los logs deberías ver:
```
✓ Correo enviado a xxx@ejemplo.com correctamente [1/100 emails hoy]
```

### Si hay error:
```
✗ Error al enviar correo a xxx@ejemplo.com: 535 Authentication failed
```
→ La API Key está mal o no está configurada

```
✗ Error al enviar correo: Sender not verified
```
→ El email en MAIL_FROM no está verificado en SendGrid

---

## 🔍 Verificaciones de la configuración actual

### ✅ Código Backend (KillerKissService.java)
```java
@Value("${spring.mail.from:${mail.remitente:}}")
private String mailRemitente;
```
- Lee `spring.mail.from` (que viene de MAIL_FROM)
- Fallback a `mail.remitente` para local

### ✅ Configuración Producción (application-prod.properties)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY}
spring.mail.from=${MAIL_FROM:${spring.mail.username}}
```
- Lee `SENDGRID_API_KEY` de las variables de entorno
- Lee `MAIL_FROM` de las variables de entorno

### ✅ Configuración Local (application-supabase.properties)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${mail.remitente:${MAIL_USERNAME:}}
spring.mail.password=${mail.contrasena:${MAIL_PASSWORD:}}
```
- Local sigue usando Gmail (no gasta emails de SendGrid)

---

## 🎯 Flujo completo

### En Producción (Render):
1. Spring Boot arranca con perfil `prod`
2. Lee `application-prod.properties`
3. Conecta a `smtp.sendgrid.net:587`
4. Usuario: `apikey`
5. Password: valor de `SENDGRID_API_KEY`
6. Remitente: valor de `MAIL_FROM` (jofre.cuerda@gmail.com)
7. SendGrid valida que el remitente esté verificado
8. SendGrid envía el correo
9. Backend incrementa el contador (1/100)

### En Local:
1. Spring Boot arranca con perfil `supabase`
2. Lee `application-supabase.properties`
3. Conecta a `smtp.gmail.com:587`
4. Usuario: tu email de `mail.config`
5. Password: tu contraseña de aplicación de `mail.config`
6. Gmail envía el correo

---

## 🐛 Problemas comunes y soluciones

### Problema: "Authentication failed"
**Causa:** API Key incorrecta o no configurada
**Solución:** 
1. Verifica que `SENDGRID_API_KEY` está configurada en Render
2. Genera una nueva API Key en SendGrid si es necesario
3. Copia la key completa (empieza con `SG.`)

### Problema: "Sender not verified"
**Causa:** El email remitente no está verificado en SendGrid
**Solución:**
1. Ve a SendGrid → Settings → Sender Authentication
2. Verifica que `jofre.cuerda@gmail.com` tiene status "Verified"
3. Si no, completa el proceso de verificación (revisa tu email)

### Problema: "Connection refused"
**Causa:** SendGrid bloqueado o credenciales incorrectas
**Solución:**
1. Verifica que tu cuenta SendGrid esté activa
2. Verifica que el puerto 587 no esté bloqueado
3. Verifica que `spring.mail.host=smtp.sendgrid.net`

---

## 📊 Límites de SendGrid Free

- **100 emails por día**
- **1 Single Sender verificado**
- **Sin dominio personalizado** (se verá "via sendgrid.net")

El contador en el frontend mostrará: `X/100` emails enviados hoy.

---

## ✅ Checklist final antes de hacer push

- [ ] `SENDGRID_API_KEY` configurada en Render
- [ ] `MAIL_FROM` configurada en Render con email verificado
- [ ] Single Sender verificado en SendGrid
- [ ] Código commiteado y pusheado a GitHub
- [ ] Deploy completado en Render
- [ ] Logs de Render sin errores
- [ ] Prueba de envío de correo exitosa

---

## 🚀 Siguiente paso

1. Ve a Render Dashboard
2. Configura las 2 variables de entorno (`SENDGRID_API_KEY` y `MAIL_FROM`)
3. Espera el redeploy automático
4. Prueba crear una partida y enviar correos
5. Verifica en los logs que dice "✓ Correo enviado"

**¿Algún error?** Copia el log de Render y te ayudo a diagnosticarlo.
