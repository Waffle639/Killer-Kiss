# Killer Kiss

Killer Kiss es un juego donde cada jugador recibe por email el nombre de su víctima. El objetivo es eliminar a tu víctima con un beso sin que nadie te vea.

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
- Vista pública con ranking en tiempo real

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

<p align="center">
  <img src="https://github.com/user-attachments/assets/5dd10b3d-4e93-4c22-878b-cad7cb393c75" width="600"/>
  <br/>
  <br/>
  <img src="https://github.com/user-attachments/assets/0856c668-3c26-4ff6-a2eb-8b0e2a18b8b7" width="600"/>
  <br/>
  <br/>
  <img src="https://github.com/user-attachments/assets/9a6bf5af-c0e3-4699-aa26-e195fc81d20b" width="600"/>
</p>

