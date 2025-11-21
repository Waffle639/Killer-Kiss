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

<img width="1152" height="743" alt="image" src="https://github.com/user-attachments/assets/d8cf8ea5-3e04-4dfe-9fcb-e42ed43164dd" />
<img width="1130" height="848" alt="image" src="https://github.com/user-attachments/assets/577cd3e1-c07a-4b5c-9beb-84d9bcbd1409" />

<img width="1166" height="665" alt="image" src="https://github.com/user-attachments/assets/d14461b8-8689-42a7-a114-d2898ad0e37d" />
<img width="1128" height="525" alt="image" src="https://github.com/user-attachments/assets/e725c45c-3ce5-494e-9e59-3dd0ba2e53da" />

<img width="1127" height="608" alt="image" src="https://github.com/user-attachments/assets/70ada065-6489-4443-9126-04f7f5d20aa3" />

<img width="393" height="493" alt="image" src="https://github.com/user-attachments/assets/925c7b80-29a5-4698-afd6-2d611321c836" />










