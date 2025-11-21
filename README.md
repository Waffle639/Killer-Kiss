# Killer Kiss

Killer Kiss es un juego donde cada jugador recibe por email el nombre de su víctima. El objetivo es eliminar a tu víctima con un beso sin que nadie te vea.

**Visita la aplicación:** [https://killer-kiss.onrender.com/public.html](https://killer-kiss.onrender.com/public.html)

> **Nota:** Si la aplicación no está activa, puede tardar unos segundos en arrancar los servicios (plan gratuito de Render).

## Cómo funciona

1. Se crea una partida con una lista de jugadores
2. Cada jugador recibe un email con el nombre de su víctima
3. Cuando eliminas a alguien, heredas su objetivo
4. Gana el último jugador que quede vivo

## Tecnologías

**Backend:**
- Java 17
- Spring Boot 3.2.0 (REST API)
- Hibernate (JPA)
- PostgreSQL (Supabase)
- SendGrid API (envío de emails)

**Frontend:**
- HTML/CSS/JavaScript
- Sistema i18n (catalán/español)

**Deploy:**
- Render.com (hosting)
- Docker (containerización)

## Funcionalidades

- Panel de administración para gestionar jugadores y partidas
- Envío automático de emails en HTML
- Ranking de jugadores
- Estadísticas de partidas
- Soporte multiidioma (CA/ES)

## Configuración

### Base de datos

El proyecto usa Supabase (PostgreSQL) en producción. Configurar las variables de entorno:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://tu-host:5432/tu-db
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=contraseña
```

### Email (SendGrid)

Para enviar emails necesitas una API Key de SendGrid:

```properties
SENDGRID_API_KEY=tu-api-key
MAIL_REMITENTE=tu-email@gmail.com
```

Si usas SMTP (Gmail), copia `mail.config.example` a `mail.config` y configura:

```properties
mail.remitente=tu-correo@gmail.com
mail.contrasena=tu-contraseña-de-aplicación
```

## Deploy

El proyecto está desplegado en Render.com con:
- PostgreSQL en Supabase
- SendGrid para envío de emails
- Docker para containerización

## Capturas

### Panel admin - Personas
Pestaña de gestión de jugadores donde se pueden añadir nuevos participantes con nombre y email, editar su información o eliminarlos. La tabla muestra el número de victorias y partidas jugadas de cada persona, permitiendo tener un historial completo de todos los jugadores.

<img width="800" alt="admin personas" src="https://github.com/user-attachments/assets/d8cf8ea5-3e04-4dfe-9fcb-e42ed43164dd" />

---

### Resultado del envío de correos
Modal que muestra las estadísticas después de enviar los emails: total de envíos, exitosos y fallidos. Para los emails que no se pudieron enviar, se muestra el nombre del jugador y su dirección de email, permitiendo identificar rápidamente a quién reenviar el objetivo manualmente.

<img width="800" alt="resultado correos" src="https://github.com/user-attachments/assets/577cd3e1-c07a-4b5c-9beb-84d9bcbd1409" />

---

### Correo recibido por los jugadores
Ejemplo del email HTML que reciben los participantes al inicio de cada partida. Incluye un diseño moderno con gradiente, el nombre de su objetivo y el mensaje de inicio del juego. Los correos se envían en catalán o español según el idioma seleccionado en el panel de administración.

<img width="800" alt="correo enviado" src="https://github.com/user-attachments/assets/6f39e10f-5038-4b33-81c2-4d12e2870226" />

---

### Panel admin - Ranking
Vista del ranking global de todos los jugadores registrados. Muestra nombre, victorias, partidas jugadas y porcentaje de victorias. La tabla está ordenada por número de victorias de mayor a menor. Este mismo ranking está disponible públicamente para que cualquiera pueda consultarlo.

<img width="800" alt="ranking" src="https://github.com/user-attachments/assets/d14461b8-8689-42a7-a114-d2898ad0e37d" />

---

### Panel admin - Estadísticas
Pestaña con estadísticas generales del juego: número total de partidas (activas y finalizadas), total de jugadores registrados y un desglose de las partidas según su estado. Permite tener una visión general de la actividad del juego.

<img width="800" alt="estadisticas" src="https://github.com/user-attachments/assets/e725c45c-3ce5-494e-9e59-3dd0ba2e53da" />

---

### Vista pública
Página accesible sin autenticación donde cualquiera puede consultar el ranking de jugadores. Incluye selector de idioma (catalán/español) y un botón para acceder al panel de administración. Perfecta para que los jugadores consulten su posición sin necesidad de credenciales.

<img width="800" alt="via publica" src="https://github.com/user-attachments/assets/70ada065-6489-4443-9126-04f7f5d20aa3" />










