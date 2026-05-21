# Login API

API REST de autenticacion y gestion de usuarios construida con Spring Boot 3. Expone login con JWT, refresh tokens, CRUD de usuarios y documentacion OpenAPI con Swagger.

## Stack

- Java 21
- Spring Boot 3.4
- Spring Security
- Spring Data JPA
- MariaDB
- JWT (`jjwt`)
- MapStruct
- Swagger / OpenAPI
- Maven

## Arquitectura

El proyecto sigue una separacion por capas cercana a arquitectura hexagonal:

- `domain`: reglas de negocio y excepciones.
- `application`: puertos de entrada/salida y casos de uso.
- `infrastructure`: controladores, seguridad, persistencia JPA, configuracion y DTOs.

## Funcionalidades

- Registro de usuarios
- Inicio de sesion con `access token` y `refresh token`
- Renovacion de token
- Cierre de sesion revocando refresh token
- Consulta, actualizacion y eliminacion de usuarios
- Documentacion interactiva con Swagger UI

## Requisitos

- Java 21
- Maven 3.9+ o uso del wrapper `./mvnw`
- Docker opcional para levantar MariaDB

## Variables de entorno

La aplicacion toma toda la configuracion sensible desde variables de entorno:

```bash
export DB_URL=jdbc:mariadb://localhost:3306/colombia_db
export DB_USERNAME=root
export DB_PASSWORD=123456
export JWT_SECRET=una_clave_larga_y_segura_de_al_menos_32_bytes
export JWT_EXPIRATION=3600000
export JWT_REFRESH_TOKEN_EXPIRATION=604800000
```

Notas:

- `JWT_EXPIRATION` y `JWT_REFRESH_TOKEN_EXPIRATION` se manejan en milisegundos.
- `spring.jpa.hibernate.ddl-auto=create` recrea el esquema al iniciar. No es apropiado para produccion.

## Base de datos con Docker

El repositorio incluye [`mariadb.docker-compose.yml`](/Users/oospina/Documents/davivienda/login/mariadb.docker-compose.yml) para levantar MariaDB localmente:

```bash
docker compose -f mariadb.docker-compose.yml up -d
```

Configuracion actual del contenedor:

- Motor: `mariadb:latest`
- Puerto: `3306`
- Base de datos: `colombia_db`
- Usuario: `root`
- Password: `123456`

## Ejecucion local

1. Exporta las variables de entorno.
2. Levanta MariaDB si aun no esta disponible.
3. Inicia la aplicacion:

```bash
./mvnw spring-boot:run
```

Alternativamente:

```bash
./mvnw clean package
java -jar target/login-0.0.1-SNAPSHOT.jar
```

## Pruebas

```bash
./mvnw test
```

## Documentacion API

Con la aplicacion encendida:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints principales

Base path: `http://localhost:8080/api/auth`

### Autenticacion

- `POST /login`
- `POST /logout`
- `POST /refresh-token`

### Usuarios

- `POST /create`
- `GET /users/{id}`
- `PUT /update`
- `DELETE /users/{id}`

## Ejemplos de requests

### Crear usuario

```http
POST /api/auth/create
Content-Type: application/json

{
  "name": "Oscar Ivan Ospina",
  "email": "oscar@correo.com",
  "password": "Admin123!",
  "isActive": true
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "oscar@correo.com",
  "password": "Admin123!"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": {
    "token": "jwt-access-token",
    "refreshToken": "refresh-token"
  },
  "message": "Login successful",
  "timestamp": "2026-05-21T10:00:00"
}
```

### Refrescar token

```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "refresh-token"
}
```

### Consultar usuario

```http
GET /api/auth/users/1
Authorization: Bearer jwt-access-token
```

## Seguridad

- Las rutas de autenticacion `login`, `logout`, `refresh-token` y `create` son publicas.
- El resto de endpoints requiere encabezado `Authorization: Bearer <token>`.
- La contrasena se almacena con BCrypt.
- El refresh token se persiste y se revoca en logout o rotacion.

## Estructura del proyecto

```text
src/main/java/com/co/oscar/login
|-- application
|-- domain
|-- infrastructure
|   |-- config
|   |-- entrypoints
|   |-- mapper
|   |-- persistence
|   `-- security
`-- LoginApplication.java
```

## Observaciones

- El DTO de usuario recibe `email`, pero tambien acepta el alias JSON `username`.
- La validacion de la contraseña en dominio exige mayuscula, numero y caracter especial.
- Para entornos reales conviene mover `ddl-auto=create` a una configuracion mas segura como `validate` o migraciones con Flyway/Liquibase.
