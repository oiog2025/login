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
- Terraform (para emulación de AWS)
- Floci.io (emulador local de AWS)

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
- Soporte para despliegue emulado en **AWS ECS Fargate** local

## Requisitos

- Java 21
- Maven 3.9+ o uso del wrapper `./mvnw`
- Docker Desktop (Windows/macOS) o Podman/Docker Nativo (Linux) para emulación
- Terraform 1.5+ (para el modo de emulación AWS)

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

---

## Modos de Ejecución

El proyecto está diseñado para ejecutarse de dos formas: el método tradicional local y la emulación de arquitectura de nube (AWS Fargate).

### MODO 1: Ejecución Local Tradicional

#### Base de datos con Docker

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

#### Ejecucion local

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

### MODO 2: Emulación Local de AWS (ECS Fargate + Floci.io + Terraform)

Este modo permite desplegar y validar el microservicio simulando exactamente el comportamiento y ciclo de vida de una tarea de **AWS ECS Fargate** gestionada mediante código de **Terraform**, sin generar costos de nube. 

Toda la configuración reside en la carpeta `infrastructure/`.

#### Paso 1: Levantar el entorno de emulación (Floci + MariaDB)
Navega a la carpeta de infraestructura y enciende los servicios base. Floci interceptará las llamadas de Terraform y orquestará los contenedores reales simulando la nube de AWS.
```bash
cd infrastructure
docker compose up -d
```

#### Paso 2: Construir la imagen Docker del microservicio
Regresa a la raíz del proyecto y compila la imagen:
```bash
cd ..
docker build -t login-api:latest .
```

#### Paso 3: Desplegar los recursos de AWS vía Terraform
Ingresa nuevamente a la carpeta de infraestructura para inicializar y aplicar el plan. Esto creará de forma emulada el repositorio ECR, el Clúster ECS, la Task Definition y el Servicio Fargate:
```bash
cd infrastructure
terraform init
terraform apply -auto-approve
```

#### Paso 4: Redes e Interconectividad en la Emulación
- **Conectividad con la BD:** Debido a que Fargate aísla el contenedor bajo el modo de red `awsvpc`, la Task Definition inyecta automáticamente `host.docker.internal` para que la API alcance la base de datos externa.
- **Validación interna:** La IP interna de la tarea de Fargate está aislada del navegador nativo en Windows/Mac. Verifica el microservicio desde la terminal:

1. Encuentra el ID o la IP interna del contenedor de Fargate emulado:
```bash
   docker inspect -f "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}" <CONTAINER_ID_FARGATE>
   ```
2. Realiza un test de salud descargando el JSON de OpenAPI:
```bash
   docker exec <CONTAINER_ID_FARGATE> wget -qO- http://localhost:8080/v3/api-docs
   ```
3. Ejecuta una petición HTTP POST real (Registro de usuario):
```bash
   docker exec <CONTAINER_ID_FARGATE> wget -qO- --header="Content-Type: application/json" --post-data='{"name": "Oscar Ivan Ospina", "email": "oscar@correo.com", "password": "Admin123!", "isActive": true}' http://localhost:8080/api/auth/create
   ```

#### 💡 Solución de Problemas Comunes en Linux (Podman)
Si utilizas GNU/Linux y Podman:
- **Permisos del Socket:** Ejecuta el compose con privilegios: `sudo docker compose up -d`.
- **Resolución de Imagen Base:** Si Podman no resuelve los "short-names" en el `Dockerfile`, configura Docker Hub por defecto:
```bash
  echo 'unqualified-search-registries = ["docker.io"]' | sudo tee /etc/containers/registries.conf.d/docker-hub.conf
  ```

---

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
````</CONTAINER_ID_FARGATE></CONTAINER_ID_FARGATE></CONTAINER_ID_FARGATE>