# 🚀 Desplegar Killer-Kiss en Render.com con Docker (GRATIS)

## 📋 Pre-requisitos
- Cuenta de GitHub (tu código debe estar en GitHub)
- Cuenta de Render.com (gratis, no necesita tarjeta de crédito)
- Tu contraseña de aplicación de Gmail para enviar correos

---

## 🎯 Paso a Paso - Deploy en Render.com

### **1️⃣ Preparar el repositorio**

✅ Ya tienes todos los archivos necesarios:
- `Dockerfile` - Para construir la imagen Docker
- `.dockerignore` - Optimiza el build
- `application-prod.properties` - Configuración PostgreSQL
- `pom.xml` - Con dependencia PostgreSQL

**Sube los cambios a GitHub:**
```bash
git add .
git commit -m "Configuración Docker para Render con PostgreSQL"
git push origin master
```

---

### **2️⃣ Crear cuenta en Render**

1. Ve a: https://render.com
2. Clic en **"Get Started"**
3. Registrate con tu cuenta de GitHub (recomendado)
4. Autoriza a Render para acceder a tus repos

---

### **3️⃣ Crear nuevo Web Service**

1. En el Dashboard de Render, clic en **"New +"**
2. Selecciona **"Web Service"**
3. Conecta tu repositorio **"Killer-Kiss"**
4. Render detectará automáticamente `render.yaml`

---

### **4️⃣ Configurar el servicio**

Render leerá tu `render.yaml` automáticamente, pero verifica:

**Settings básicos:**
- **Name:** `killer-kiss` (o el que quieras)
- **Environment:** `Java`
- **Build Command:** Ya está en render.yaml
- **Start Command:** Ya está en render.yaml
- **Plan:** Selecciona **"Free"** ✅

---

### **5️⃣ Configurar Base de Datos**

Render creará automáticamente la base de datos PostgreSQL desde el `render.yaml`.

**Verifica:**
- Database Name: `killer-kiss-db`
- Será PostgreSQL (compatible con tu app)
- Plan: **Free** (750 horas/mes)

---

### **6️⃣ Configurar Variables de Entorno**

En el dashboard de tu Web Service, ve a **"Environment"** y agrega:

| Variable | Valor | Ejemplo |
|----------|-------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Ya configurado en render.yaml |
| `MAIL_USERNAME` | Tu email de Gmail | `tuemail@gmail.com` |
| `MAIL_PASSWORD` | Contraseña de app de Gmail | `abcd efgh ijkl mnop` |

**⚠️ IMPORTANTE: Contraseña de aplicación de Gmail**
1. Ve a: https://myaccount.google.com/security
2. Activa verificación en 2 pasos
3. Genera una "Contraseña de aplicación"
4. Usa esos 16 caracteres en `MAIL_PASSWORD`

---

### **7️⃣ Deploy!**

1. Clic en **"Create Web Service"**
2. Render empezará a:
   - 📦 Clonar tu repo
   - 🔨 Ejecutar `render-build.sh`
   - ☕ Compilar con Maven
   - 🗄️ Crear la base de datos PostgreSQL
   - 🚀 Iniciar tu aplicación

**Tiempo estimado:** 3-5 minutos

---

### **8️⃣ ¡Tu app está ONLINE!**

Cuando termine, verás:
- ✅ Estado: **"Live"**
- 🌐 URL pública: `https://killer-kiss.onrender.com`

**Accede a tu app:**
```
https://killer-kiss.onrender.com
```

---

## 🔧 Mantenimiento

### **Ver logs:**
En el dashboard → Pestaña **"Logs"**

### **Reiniciar:**
En el dashboard → **"Manual Deploy"** → **"Deploy latest commit"**

### **Actualizar código:**
```bash
git add .
git commit -m "Nuevos cambios"
git push
```
Render desplegará automáticamente los cambios.

---

## ⚠️ Limitaciones del Plan Free

- 💤 Se duerme después de 15 min sin actividad
- ⏰ Primera carga después de dormir: ~30-60 segundos
- ⏱️ 750 horas de servidor/mes
- 💾 Base de datos: 1 GB de almacenamiento

**Suficiente para:**
- ✅ Proyectos personales
- ✅ Demos
- ✅ Uso con amigos
- ✅ Aprendizaje

---

## 🆙 Upgrade (si necesitas más adelante)

**Plan Starter ($7/mes):**
- ✅ No se duerme
- ✅ Más memoria RAM
- ✅ Más CPU
- ✅ Soporte prioritario

---

## 🐛 Solución de Problemas

### **Error en build:**
- Verifica que `render-build.sh` tenga permisos de ejecución
- Revisa los logs en Render

### **Error de conexión a BD:**
- Verifica que la variable `DATABASE_URL` esté configurada
- Render la configura automáticamente desde render.yaml

### **Correos no se envían:**
- Verifica `MAIL_USERNAME` y `MAIL_PASSWORD`
- Usa contraseña de aplicación de Gmail (no tu contraseña normal)

### **App se queda en "Deploying...":**
- Espera 5-10 minutos (primera vez puede tardar)
- Revisa logs para ver errores

---

## 📱 URLs útiles

- **Dashboard Render:** https://dashboard.render.com
- **Docs Render:** https://render.com/docs
- **Tu app:** https://[tu-app].onrender.com

---

## ✅ Checklist Final

- [ ] Código subido a GitHub
- [ ] Cuenta de Render creada
- [ ] Web Service creado
- [ ] Base de datos PostgreSQL configurada
- [ ] Variables de entorno configuradas
- [ ] Deploy exitoso
- [ ] App accesible por URL pública
- [ ] Correos funcionando

---

**🎉 ¡Felicidades! Tu app está en la nube GRATIS para siempre!**
