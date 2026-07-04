# App de Matrícula — Sistema de Control de Cuentas

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?logo=docker&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-85EA2D?logo=swagger&logoColor=black)
![Security](https://img.shields.io/badge/Security-JWT%20%7C%20BCrypt%20%7C%20AES-2E7D32?logo=letsencrypt&logoColor=white)

**Curso:** Criptografía 2  
**Grupo:** 05  
**Arquitectura:** Monolito modular + frontend web  
**Modalidad:** Evaluación individual sobre proyecto grupal

---

## Descripción

SIGEA es una plataforma de control de cuentas para una institución educativa. Administra usuarios con
roles y permisos granulares, matricula alumnos en aulas por año académico, genera cuotas a partir de un tarifario
configurable y registra pagos — todo bajo transacciones atómicas, control de concurrencia optimista y auditoría
completa de cada operación.

---

## Módulos del sistema

| Módulo                | Responsabilidad                                                            |
|-----------------------|----------------------------------------------------------------------------|
| Seguridad             | Login, cambio de contraseña, hash+salt de credenciales, 2FA (TOTP)         |
| Usuarios y roles      | Gestión de usuarios, roles y permisos (Ver/Crear/Editar/Eliminar/Imprimir) |
| Aulas                 | Registro de aulas por año académico, nivel, grado y sección                |
| Alumnos               | Registro de alumnos con datos sensibles cifrados (AES)                     |
| Conceptos (tarifario) | Definición de conceptos de pago por año académico                          |
| Matrícula             | Registro de matrícula con validaciones y doble factor                      |
| Pagos                 | Registro de pagos y generación de recibos                                  |
| Auditoría             | Trazabilidad de toda operación sobre el sistema                            |
| Reportes              | Reportes consultables/exportables de matrícula, pagos y auditoría          |

---

## Arquitectura

Monolito modular, con base de datos distribuido desplegado en 2 equipos conectados por LAN, cada uno con su propio
Docker:

```
Cliente (navegador)
  │  HTTP
  ▼
Equipo 1 — Servidor web
  └── sigea (:8080)
        Presentación · Seguridad · Servicios · Persistencia
  │  JDBC (IP LAN, no nombre de servicio Docker)
  ▼
Equipo 2 — Servidor BD
  ├── postgres-main (:5432)
  └── postgres-replica  (:5433)   ← streaming replication
```

En desarrollo (`docker-compose.dev.yml`), `sigea` y ambas bases de datos comparten la misma red interna de
Docker en una sola máquina. En el despliegue real de 2 equipos esa red ya no existe entre ambas máquinas, así que
`sigea` apunta a la **IP LAN** del Equipo 2 en vez del nombre de servicio.

---

## Estado de implementación

| Módulo           | Estado        | Detalle                                                                 |
|------------------|---------------|-------------------------------------------------------------------------|
| Seguridad        | 🔧 En Proceso | Login, JWT, TOTP y caché de sesión 2FA pendiente (Caffeine)             |
| Usuarios y roles | 🔧 En Proceso | Permisos dinámicos contra `Rol_Funcionalidad`, árbol de funcionalidades |
| Aulas            | 🔧 En Proceso | CRUD con unique key año+nivel+grado+sección                             |
| Alumnos          | 🔧 En Proceso | Cifrado AES de `fechaNacimiento` y `numeroDocumento`                    |
| Conceptos        | 🔧 En Proceso | Tarifario + clonado entre años académicos, optimistic lock              |
| Matrícula        | 🔧 En Proceso | Transacción atómica + validación de 2FA vía claim del JWT               |
| Pagos            | 🔧 En Proceso | Registro de pagos y generación de recibos                               |
| Auditoría        | 🔧 En Proceso | Interceptor AOP sobre todas las operaciones                             |
| Reportes         | 🔧 En Proceso | Exportables de matrícula, pagos y auditoría                             |

---

## Stack tecnológico

| Capa                | Tecnología                                                                |
|---------------------|---------------------------------------------------------------------------|
| Lenguaje            | Java 21                                                                   |
| Framework           | Spring Boot 3.5.16                                                        |
| Seguridad           | Spring Security + BCrypt + TOTP (Google Authenticator)                    |
| Cifrado de datos    | AES (`javax.crypto` + `AttributeConverter` de JPA)                        |
| Persistencia        | Spring Data JPA + Hibernate, `@Version` (optimistic lock)                 |
| Base de datos       | PostgreSQL 15, con réplica (streaming replication)                        |
| Auditoría           | Spring AOP                                                                |
| Caché de aplicación | Caffeine (sesión 2FA pendiente, permisos por rol, parámetros del sistema) |
| Documentación       | Swagger UI / OpenAPI 3.0                                                  |
| Testing             | JUnit 5 + Mockito                                                         |
| Mapeo               | MapStruct                                                                 |
| Utilidades          | Lombok                                                                    |
| Contenerización     | Docker (multi-stage) + Docker Compose                                     |
| Build               | Maven                                                                     |

[![Tecnologias](https://skillicons.dev/icons?i=java,spring,postgresql,docker,maven)](https://skillicons.dev)
---

## Variables de entorno

Copiar `.env.example` a `.env` y completar los valores antes de levantar los servicios:

```bash
cp .env.example .env
```

| Variable            | Descripción                                                           |
|---------------------|-----------------------------------------------------------------------|
| `POSTGRES_DB`       | Nombre de la base de datos                                            |
| `POSTGRES_USER`     | Usuario de la base de datos                                           |
| `POSTGRES_PASSWORD` | Contraseña de la base de datos                                        |
| `AES_SECRET_KEY`    | Clave AES para cifrar datos sensibles del alumno                      |
| `JWT_SECRET`        | Clave secreta para firmar los JWT                                     |
| `JWT_EXPIRATION`    | Expiración del token en segundos (ej. `3600` = 1h)                    |
| `TOTP_ISSUER`       | Nombre del emisor mostrado en Google Authenticator                    |
| `DB_HOST`           | Solo en `.env` del Equipo 1: IP LAN del Equipo 2 (ej. `192.168.1.20`) |

> **Generar `JWT_SECRET` / `AES_SECRET_KEY`:** `openssl rand -base64 32` — puede usarse git bash.

---

## Puesta en marcha

### Requisitos previos

- Docker y Docker Compose instalados
- Archivo `.env` configurado (ver sección anterior)

### Escenario 1 — Todo en un equipo (desarrollo)

```bash
docker compose -f docker-compose.dev.yml up -d --build
```

Levanta `sigea`, `postgres-sigea-main` y `postgres-sigea-replica` en una sola máquina, comunicados por la red
interna de Docker.

### Escenario 2 — Despliegue real de 2 equipos

**Equipo 1 (Servidor web):**

```bash
docker compose -f docker-compose.app.yml up -d --build
```

**Equipo 2 (Servidor BD):**

```bash
docker compose -f docker-compose.db.yml up -d --build
```

> El puerto `5432` de `postgres-main` debe quedar accesible desde la LAN (firewall del Equipo 2 permitiendo el
> rango de IPs del Equipo 1).

### Parar y limpiar

```bash
docker compose down        # parar contenedores
docker compose down -v     # parar y eliminar volúmenes (borra datos de BD)
```

---

## Endpoints disponibles

La app escucha en `http://localhost:8080`. Todas las rutas protegidas requieren el header
`Authorization: Bearer <token>`.

### Seguridad (públicas / autenticado)

| Método | Ruta                         | Descripción                                   |
|--------|------------------------------|-----------------------------------------------|
| POST   | `/api/auth/login`            | Login → token directo o `requiere2FA: true`   |
| POST   | `/api/auth/login/verify-2fa` | Verifica código TOTP → emite token definitivo |
| PUT    | `/api/auth/change-password`  | Cambio de contraseña (usuario autenticado)    |

---

### Usuarios y permisos `🔒` — Superusuario

| Método | Ruta                          | Descripción                                             |
|--------|-------------------------------|---------------------------------------------------------|
| POST   | `/api/usuarios`               | Crea un usuario y le asigna un rol                      |
| GET    | `/api/funcionalidades/tree`   | Árbol de funcionalidades para menú y matriz de permisos |
| PUT    | `/api/roles/{idRol}/permisos` | Aplica permisos por funcionalidad (invalida caché)      |

---

### Aulas `🔒`

| Método | Ruta         | Params                     | Descripción                                |
|--------|--------------|----------------------------|--------------------------------------------|
| POST   | `/api/aulas` | —                          | Registra un aula (Secretaria/Superusuario) |
| GET    | `/api/aulas` | `anioAcademico?`, `nivel?` | Lista de aulas (todos los roles)           |

---

### Alumnos `🔒`

| Método | Ruta           | Descripción                                                             |
|--------|----------------|-------------------------------------------------------------------------|
| POST   | `/api/alumnos` | Registra un alumno; cifra `numeroDocumento` y `fechaNacimiento` con AES |

---

### Conceptos (tarifario) `🔒`

| Método | Ruta                    | Descripción                                                   |
|--------|-------------------------|---------------------------------------------------------------|
| POST   | `/api/conceptos`        | Crea un concepto de pago (con optimistic lock)                |
| POST   | `/api/conceptos/clonar` | Clona los conceptos de un año académico a otro (Superusuario) |

---

### Matrícula `🔒` — requiere sesión con 2FA verificado

| Método | Ruta              | Descripción                                             |
|--------|-------------------|---------------------------------------------------------|
| POST   | `/api/matriculas` | Registra matrícula; valida el claim `2fa: true` del JWT |

---

### Pagos `🔒`

| Método | Ruta         | Descripción                         |
|--------|--------------|-------------------------------------|
| POST   | `/api/pagos` | Registra un pago y genera el recibo |

---

### Auditoría / Reportes `🔒`

| Método | Ruta                | Descripción                                                       |
|--------|---------------------|-------------------------------------------------------------------|
| GET    | `/api/auditoria/**` | Consulta de trazabilidad de operaciones                           |
| GET    | `/api/reportes/**`  | Reportes consultables/exportables de matrícula, pagos y auditoría |

---

## Modelo de seguridad

| Rol          | Permisos                          |
|--------------|-----------------------------------|
| Superusuario | Acceso total. No puede eliminarse |
| Director     | Solo consulta de registros        |
| Secretaria   | Todas las operaciones del sistema |

Los permisos no están fijados por rol en código: se resuelven dinámicamente contra la tabla `Rol_Funcionalidad`, que
define por cada rol y funcionalidad qué acciones (`Ver`, `Crear`, `Editar`, `Eliminar`, `Imprimir`) están habilitadas.
Se cachean en Caffeine por `idRol` y se invalidan manualmente al actualizarse.

### Reglas de cifrado

| Dato                                            | Mecanismo                                |
|-------------------------------------------------|------------------------------------------|
| Contraseña de usuario                           | Hash + salt (`BCryptPasswordEncoder`)    |
| `fechaNacimiento`, `numeroDocumento` del alumno | Cifrado AES (JPA `AttributeConverter`)   |
| Segundo factor en matrícula                     | TOTP compatible con Google Authenticator |

---

## Contrato estándar de errores

Todos los endpoints devuelven los errores con la misma forma:

```java
public record ErrorResponse(
        String error,
        String message,
        Map<String, Object> metadata,
        Instant timestamp
) {
}
```

---

## Entregables

- ☐ Código fuente en repositorio Git del equipo
- ☐ Documentación Swagger/OpenAPI accesible en `/swagger-ui`
- ☐ Colección Postman con todos los endpoints del sistema
- ☐ `Dockerfile` + los tres `docker-compose.*.yml` funcionales: `dev`, `app` (Equipo 1) y `db` (Equipo 2)
- ☐ Pruebas unitarias/integración cubriendo login, matrícula (transacción + rollback) y pagos
- ☐ Evidencia de eliminación lógica, auditoría y optimistic lock funcionando
- ☐ Demo en vivo mostrando el flujo completo: login → matrícula con 2FA → pago → auditoría → reportes

---

## Equipo de desarrollo

| Integrante | GitHub                                       |
|------------|----------------------------------------------|
| BeckanG728 | [@BeckanG728](https://github.com/BeckanG728) |

<br>

---

<div align="center">

![Criptografía II](https://img.shields.io/badge/Criptograf%C3%ADa_II-SIGEA-4A90D9?style=for-the-badge&logo=letsencrypt&logoColor=white)
</div>
