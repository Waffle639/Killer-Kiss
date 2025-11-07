# 🚀 PROBAR SENDGRID EN LOCAL

## 🎯 Objetivo

Probar el envío de correos con SendGrid desde tu ordenador (sin desplegar en Render).

---

## ✅ Paso 1: Obtener API Key de SendGrid

### 1.1 Crear cuenta (si no la tienes)
1. Ve a: https://signup.sendgrid.com/
2. Regístrate con el plan **Free** (100 emails/día)
3. Verifica tu email

### 1.2 Verificar Single Sender
1. Ve a: https://app.sendgrid.com/
2. Settings → Sender Authentication → **Single Sender Verification**
3. Clic en **Create New Sender**
4. Rellena con tu email: `jofre.cuerda@gmail.com`
5. Revisa tu Gmail y verifica el email
6. Estado debe ser: **Verified** ✅

### 1.3 Generar API Key
1. Ve a: https://app.sendgrid.com/
2. Settings → **API Keys**
3. Clic en **Create API Key**
4. Nombre: `Killer Kiss Local Test`
5. Permisos: **Full Access**
6. Clic en **Create & View**
7. **COPIA LA KEY COMPLETA** (empieza con `SG.`)
   - ⚠️ Solo la verás una vez, guárdala bien

---

## ✅ Paso 2: Configurar `mail.config`

Abre: `src/main/resources/mail.config`

Edita estas líneas:

```properties
# Email remitente (debe estar verificado en SendGrid)
mail.remitente=jofre.cuerda@gmail.com

# API Key de SendGrid (la que copiaste)
sendgrid.api.key=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**Ejemplo:**
```properties
mail.remitente=jofre.cuerda@gmail.com
sendgrid.api.key=SG.1A2B3C4D5E6F7G8H9I0J
```

---

## ✅ Paso 3: Ejecutar la aplicación

### Opción A - Doble clic en el script (Más fácil)
```
run-sendgrid-local.bat
```

### Opción B - Desde tu IDE
1. Abre `Main.java`
2. Run Configuration
3. VM options: `-Dspring.profiles.active=sendgrid-local`
4. Run

### Opción C - Desde PowerShell
```powershell
# Compilar
mvn clean package -DskipTests

# Ejecutar con SendGrid
java -Dspring.profiles.active=sendgrid-local -jar target/KillerKiss-1.0-SNAPSHOT.jar
```

---

## ✅ Paso 4: Probar envío de correos

1. Abre: http://localhost:8080
2. Pestaña **Personas**
   - Crea 2-3 personas con **emails reales** (mejor tuyos)
3. Pestaña **Partidas**
   - Crea una partida con esas personas
4. Los correos se enviarán automáticamente vía SendGrid

---

## 👀 Verificar en los logs

### ✅ Si funciona verás:
```
✓ Correo enviado a xxx@gmail.com correctamente [1/100 emails hoy]
✓ Correo enviado a yyy@gmail.com correctamente [2/100 emails hoy]
```

### ❌ Si falla verás:
```
✗ Error al enviar correo: 535 Authentication failed
```
**Solución:** La API Key está mal o no está configurada

```
✗ Error: Sender not verified
```
**Solución:** El email `mail.remitente` no está verificado en SendGrid

```
✗ Error: Connection refused
```
**Solución:** Puerto 587 bloqueado, revisa firewall

---

## 📧 Verificar correos recibidos

Los correos llegarán a las bandejas de entrada de los emails que pusiste.

**Asunto:** `Partida Killer Kiss: [nombre de la partida]`

**Nota:** Los correos pueden llegar a spam la primera vez. Márcalos como "No es spam".

---

## 🎯 Contador de emails

En la esquina superior derecha verás:
```
📧 Emails: 2/100
```

Se actualiza automáticamente después de cada envío.

---

## 🔄 Cambiar entre Gmail y SendGrid

### Para usar Gmail:
1. Edita `mail.config`:
   ```properties
   mail.remitente=jofre.cuerda@gmail.com
   mail.contrasena=msezzjyvjhrviaoa
   ```
2. Ejecuta: `run-local.bat` (perfil: `supabase`)

### Para usar SendGrid:
1. Edita `mail.config`:
   ```properties
   mail.remitente=jofre.cuerda@gmail.com
   sendgrid.api.key=SG.tu_api_key
   ```
2. Ejecuta: `run-sendgrid-local.bat` (perfil: `sendgrid-local`)

---

## 🚨 Problemas comunes

### "Could not resolve placeholder 'sendgrid.api.key'"
❌ No hay `sendgrid.api.key` en `mail.config`
✅ Añade la línea: `sendgrid.api.key=SG.tu_key`

### "535 Authentication failed"
❌ API Key incorrecta
✅ Verifica que la copiaste completa y empieza con `SG.`

### "Sender not verified"
❌ El email no está verificado en SendGrid
✅ Ve a SendGrid → Sender Authentication y verifica el email

### "Connection refused"
❌ Puerto 587 bloqueado
✅ Desactiva temporalmente el firewall/antivirus

---

## ✅ Una vez funcione en LOCAL

Cuando veas que los correos se envían correctamente desde local, entonces:

1. **Commit y push de los cambios** (sin `mail.config`)
2. **Configura las variables en Render:**
   - `SENDGRID_API_KEY` = tu API Key
   - `MAIL_FROM` = jofre.cuerda@gmail.com
3. **Redespliega en Render**

¡Y listo! Funcionará igual en producción.

---

## 📊 Comparación

| Característica | Gmail (local) | SendGrid (local) | SendGrid (Render) |
|----------------|---------------|------------------|-------------------|
| Script | `run-local.bat` | `run-sendgrid-local.bat` | Automático |
| Perfil | `supabase` | `sendgrid-local` | `prod` |
| SMTP | smtp.gmail.com | smtp.sendgrid.net | smtp.sendgrid.net |
| Credenciales | mail.config | mail.config | Variables de entorno |
| Límite | ~500/día | 100/día | 100/día |

---

## 🎉 ¿Listo para probar?

1. Obtén API Key de SendGrid
2. Edita `mail.config` con la API Key
3. Ejecuta `run-sendgrid-local.bat`
4. Abre http://localhost:8080
5. Crea partida y envía correos
6. ¡Verifica que lleguen!

**¿Algún error?** Copia el mensaje de error de los logs y te ayudo.
