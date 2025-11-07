# 🚀 Guía Rápida - Ejecutar Killer Kiss en LOCAL

## ✅ Paso 1: Edita `mail.config`

Abre el archivo `mail.config` (está en la raíz del proyecto) y rellena:

```properties
# Password de Supabase
supabase.password=TU_PASSWORD_AQUI

# Tu email y contraseña de aplicación de Gmail
mail.remitente=jofre.cuerda@gmail.com
mail.contrasena=TU_CONTRASEÑA_DE_16_CARACTERES_AQUI
```

### 🔑 ¿Cómo obtener la contraseña de aplicación de Gmail?

1. Ve a: https://myaccount.google.com/
2. Clic en **Seguridad** (menú izquierdo)
3. Activa **Verificación en dos pasos** (si no la tienes)
4. Busca **Contraseñas de aplicaciones**
5. Selecciona:
   - Aplicación: **Correo**
   - Dispositivo: **Otro** → escribe "Killer Kiss"
6. Clic en **Generar**
7. Copia la contraseña de 16 caracteres **SIN ESPACIOS**

**Ejemplo:**
- Gmail te da: `abcd efgh ijkl mnop`
- Tú pones en mail.config: `abcdefghijklmnop`

---

## ✅ Paso 2: Ejecuta la aplicación

### Opción A - Doble clic en `run-local.bat`
El script se encarga de todo automáticamente.

### Opción B - Desde tu IDE
1. Abre el proyecto en tu IDE
2. Ve a Run Configuration
3. Añade VM option: `-Dspring.profiles.active=supabase`
4. Run/Debug

### Opción C - Desde PowerShell
```powershell
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -Dspring.profiles.active=supabase -jar target/KillerKiss-1.0-SNAPSHOT.jar
```

---

## ✅ Paso 3: Prueba el envío de correos

1. Abre el navegador: **http://localhost:8080**
2. Ve a la pestaña **Personas**
3. Crea 2-3 personas con **TUS emails** (para que te lleguen los correos de prueba)
4. Ve a la pestaña **Partidas**
5. Crea una nueva partida seleccionando esas personas
6. Los correos se enviarán automáticamente

---

## 👀 Verifica en los logs

### ✅ Si funciona verás:
```
✓ Correo enviado a xxx@gmail.com correctamente [1/100 emails hoy]
✓ Correo enviado a yyy@gmail.com correctamente [2/100 emails hoy]
```

### ❌ Si falla verás:
```
✗ Error al enviar correo a xxx@gmail.com: 535 Authentication failed
```
**Solución:** La contraseña de aplicación está mal, vuelve a generarla.

```
✗ Error al enviar correo: Connection refused
```
**Solución:** Puerto 587 bloqueado por firewall/antivirus.

---

## 📧 Revisa tu bandeja de entrada

Los correos llegarán a los emails que pusiste en las personas. El asunto será:
```
Partida Killer Kiss: [nombre de la partida]
```

---

## 🎯 Contador de emails

En la esquina superior derecha de la página verás:
```
📧 Emails: 2/100
```

Se actualiza automáticamente después de cada envío.

---

## 🔧 Configuración que usa LOCAL

- **Base de datos:** Supabase (desde `mail.config`)
- **Email SMTP:** Gmail (smtp.gmail.com:587)
- **Credenciales:** Desde `mail.config`
- **Perfil:** supabase
- **Puerto:** 8080

---

## ❓ Problemas comunes

### "Could not resolve placeholder 'mail.remitente'"
❌ No encuentra `mail.config`
✅ Asegúrate de que está en la raíz (mismo nivel que `pom.xml`)

### "535 Authentication failed"
❌ Contraseña de aplicación incorrecta
✅ Genera una nueva contraseña de aplicación en Google

### "Connection refused" al enviar correo
❌ Puerto 587 bloqueado
✅ Desactiva temporalmente el antivirus/firewall

### El servidor no arranca
❌ Puerto 8080 ocupado
✅ Cierra otras aplicaciones que usen el puerto 8080

---

## ✅ Una vez funcione en LOCAL

Cuando veas que los correos se envían correctamente, entonces:
1. Haz commit y push de los cambios
2. Configura SendGrid en Render (ver `CONFIGURACION_SENDGRID_RENDER.md`)
3. Redespliega en Render

**¡No subas `mail.config` a Git!** (ya está en `.gitignore`)

---

## 🎉 ¿Listo?

```bash
# 1. Edita mail.config
# 2. Ejecuta run-local.bat
# 3. Abre http://localhost:8080
# 4. Crea partida y envía correos
# 5. ¡Disfruta!
```
