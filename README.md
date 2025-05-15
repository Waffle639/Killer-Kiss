# 💋 Killer Kiss

**Killer Kiss** es un juego en el que cada jugador recibe por correo electrónico el nombre de su víctima. El objetivo: Eliminar a todos los participantes con un beso simbólico, ¡sin que ningún otro jugador vivo te vea! 😏

---

## 🕹️ Características

- 📧 Envío automático de correos con la víctima asignada.
- 🔄 Generación de un círculo cerrado de asesinos y víctimas.
- 📊 Gestión y almacenamiento de datos: jugadores, partidas y resultados.
- 🏆 Sistema de ranking según las partidas ganadas y jugadas.
- 🔐 Validación y control de errores (duplicados, listas vacías, etc).
- 💾 Persistencia de los datos usando archivos **JSON**.

---

## 📝 Cómo jugar

1. El juego empieza con una lista de jugadores (nombres + correos).
2. El sistema genera aleatoriamente un círculo cerrado de asesinatos.
3. Cada jugador recibe por correo el nombre de su víctima.
4. Los jugadores deben matar a su víctima con un beso simbólico sin ser vistos por otro jugador vivo.
5. Al matar a un jugador passas a matar al objetivo de la persona que has matado.
6. El ranking muestra quiénes han ganado más partidas.

---

## 📧 Cómo configurar el envío de correos (Gmail)

Para que el programa pueda enviar automáticamente los correos con las víctimas asignadas, necesitas configurar tu cuenta de Gmail con una **contraseña de aplicación**.

### 🔒 Paso 1: Activa la verificación en dos pasos

1. Accede a tu cuenta de Google:  
   👉 [https://myaccount.google.com/security](https://myaccount.google.com/security)
2. Activa la **verificación en dos pasos** si aún no lo has hecho.

### 🔐 Paso 2: Genera una contraseña de aplicación

1. Ve a 👉 [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. Si no te deja entrar, asegúrate de tener activada la verificación en dos pasos.
3. En el menú:
   - Selecciona **Correo** como aplicación.
   - Elige **Otro** y ponle un nombre como `killer-kiss`.
4. Google te generará una contraseña de 16 caracteres (como `abcd efgh ijkl mnop`).  
   ⚠️ **Guárdala bien**, ya que no se vuelve a mostrar.

### 🛠️ Paso 3: Configura el correo en el código

Abre el archivo Main el cual tiene la funcion enviarCorreu i modifica las siguientes lineas:

```java
final String remitente = "tu-correo@gmail.com"; // Tu cuenta de Gmail
final String contrasenaApp = "abcd efgh ijkl mnop"; // Contraseña de aplicación
```


## 📷 Capturas de pantalla

<p align="center">
  <img src="https://github.com/user-attachments/assets/5dd10b3d-4e93-4c22-878b-cad7cb393c75" width="600"/>
  <br/>
  <br/>
  <img src="https://github.com/user-attachments/assets/0856c668-3c26-4ff6-a2eb-8b0e2a18b8b7" width="600"/>
  <br/>
  <br/>
  <img src="https://github.com/user-attachments/assets/9a6bf5af-c0e3-4699-aa26-e195fc81d20b" width="600"/>
  <br/>
 
</p>

---


## 🚀 Cómo ejecutarlo

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/killer-kiss.git

