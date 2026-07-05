# SIGEA — Plan de Desarrollo Backend

> Monolito modular · equipo de 3 personas · paquete base `com.institucion.sigea`
> Basado en el estado real del repositorio (auditado vía JetBrains MCP, 04-jul-2026).

---

## Convenciones

- **[HECHO]** ya está implementado y verificado en el repo — no se asigna a nadie.
- **[ESQUELETO]** el paquete/clase/interfaz ya existe pero vacío — sigue asignada, el trabajo es implementar, no diseñar el contrato.
- **[PENDIENTE]** no existe ningún archivo todavía.
- **[BLOQUEA →]** esta tarea debe completarse antes de que otra persona pueda avanzar.
- **[DEPENDE DE]** la tarea no puede iniciarse hasta que la indicada esté terminada.
- **[INTEGRACIÓN]** punto de encuentro entre personas. Agendar sesión conjunta.
- **[CACHÉ]** la tarea introduce o consume una entrada de Caffeine ya configurada en `config/CacheConfig` — revisar el contrato de invalidación antes de implementarla.
- **[AOP]** la tarea depende de que `auditoria/AuditoriaAspect` (P1-11) ya intercepte el método correspondiente.
- Los archivos listados en cada tarea son el **mínimo entregable**, con rutas relativas a `src/main/java/com/institucion/sigea/`.
- **Records vs Lombok**: DTOs de request/response son `record`; Lombok (`@Getter/@Setter`, `@Builder`) solo en entidades JPA.
- **Servicios con interfaz + implementación**: `service/NombreService.java` (interfaz) + `service/impl/NombreServiceImpl.java` (implementación) en todos los módulos, sin excepción — incluido `auth/` (`AuthService`/`AuthServiceImpl`, `TotpService`/`TotpServiceImpl`).

---

## Estado actual del proyecto

**Completo y funcionando (no requiere asignación):**
- Todo `core/`: `ErrorCode`, `BusinessException`, `GlobalExceptionHandler` (incluye manejo de `MethodArgumentNotValidException`, `ConstraintViolationException` y catch-all genérico), `ErrorResponse`/`ApiResponse`/`PageResponse`, `BaseEntity`, `AesConverter` (AES/GCM con IV aleatorio).
- `config/`: `CacheConfig` (3 cachés Caffeine), `CorsConfig`, `JpaAuditingConfig`, `OpenApiConfig`, `AesProperties`, `JwtProperties`, `TotpProperties`.
- `security/jwt/JwtUtil` y `security/filter/JwtAuthFilter`: generación/parseo de JWT con claim `2fa`, filtro stateless de autenticación.
- `security/config/SecurityConfig`: `PasswordEncoder` (BCrypt) y `SecurityFilterChain` configurados — queda un comentario `TODO(usuarios-roles)` (línea ~24) sin actualizar en el código, pero la decisión ya está tomada: se descarta el flujo estándar de Spring Security (`DaoAuthenticationProvider` + `AuthenticationManager` + `CustomUserDetailsService`) a favor del enfoque que ya asume P1-04 (`AuthServiceImpl` valida y emite el JWT directamente), porque ese flujo no modela el estado intermedio `PENDIENTE_2FA` y el rol no se resuelve vía `GrantedAuthority` en ningún punto del sistema. Documentar esto en el código es la tarea **P1-12** (ajuste de comentario, no una decisión pendiente).
- Infraestructura: `pom.xml` con las dependencias necesarias; `Dockerfile` y los tres `docker-compose.*.yml` en la raíz del repo; `.env.example` con las claves esperadas.

**Con esqueleto de paquete creado pero sin implementar:**
- `auditoria/`: `AuditoriaEntity`, `AuditoriaRepository`, `AuditoriaService`, `AuditoriaAspect` existen como clases vacías — implementarlas es **P1-11**, que a su vez depende de que exista la anotación `@Auditable` (**P1-10**, no existe todavía en ningún paquete del repo).
- `auth/service/`: existen `IAuthService`/`AuthService` e `ITotpService`/`TotpService`, todos vacíos y con nomenclatura a corregir (ver P1-03/P1-04 más abajo).
- `security/permission/PermisoEvaluator`: clase vacía.

**Sin ningún archivo (solo carpetas de paquete vacías):**
- `usuario/`, `aula/`, `alumno/`, `concepto/`, `matricula/`, `pago/`, `reporte/`.
- No existe ningún paquete `parametro/` — se crea como parte de **P2-10** (no de P1-09 — P1-09 es el seeder del Superusuario, un módulo distinto).
- `src/test/java/.../auth/`, `.../matricula/`, `.../pago/`, `.../integration/` — solo hay `SigeaApplicationTests.java`.

**Decisiones de diseño ya fijadas por el código existente (no son negociables, el equipo debe respetarlas):**
- Cachés de Caffeine (`config/CacheConfig`): `permisosPorRol` (sin TTL, invalidación manual), `sesion2faPendiente` (`expireAfterWrite` 5 min), `parametrosSistema` (`expireAfterWrite` 30 min).
- El subject del JWT es `idUsuario` (`Long`, `IDENTITY` en `BaseEntity`).
- El módulo de auditoría se llama `auditoria/`.
- La configuración de claves sensibles se maneja con tres records `@ConfigurationProperties`: `AesProperties`, `JwtProperties`, `TotpProperties`.

**Decisiones de diseño pendientes de definir por el equipo antes de avanzar:**
- `BaseEntity.estado` es `boolean` (no un enum). Se elimina el enum `core/enums/EstadoRegistro` por redundante — **ninguna entidad nueva debe usarlo**; la eliminación lógica se maneja con `estado = true/false`.
- La replicación streaming real entre `postgres-sigea-main` y `postgres-sigea-replica` (`docker-compose.dev.yml`) queda pendiente para el cierre del proyecto, una vez el resto del backend esté completo.

---

## PERSONA 1 — Seguridad (`security/`), Autenticación (`auth/`), Usuarios (`usuario/`), Auditoría (`auditoria/`) y Parámetros (`parametro/`)

---

### P1-01 · Modelos Usuario, Rol, Funcionalidad, Rol_Funcionalidad · [PENDIENTE]

**Directorio:** `usuario/entity/`

**Descripción**
Entidades del modelo de seguridad: Usuario (PK idUsuario, UK Usuario, FK idRol, `dosFactorHabilitado` Boolean, `totpSecret` cifrado con `AesConverter`), Rol (PK idRol, UK nombreRol), Funcionalidad (PK idFuncionalidad, UK nombre, FK padre a sí misma), Rol_Funcionalidad (PK idRolFuncionalidad, FK idRol, FK idFuncionalidad, flags Ver/Crear/Editar/Eliminar/Imprimir). Todas heredan de `core/persistence/BaseEntity` (`@Version`, `estado` booleano, timestamps).

**Criterios de aceptación:**

- Relación Usuario→Rol es muchos a uno
- `Funcionalidad.padre` referencia a la misma tabla para permitir árbol
- `Usuario.usuarioCreacion` es FK a Usuario (auditoría de quién lo creó)
- `Usuario` no expone el password en texto plano en ningún DTO de respuesta
- `Usuario.totpSecret` usa `@Convert(converter = AesConverter.class)` y tampoco se expone en ningún DTO de respuesta (ni cifrado ni plano) — solo se genera y se muestra una única vez, al activar 2FA (ver P1-05)
- `Usuario.dosFactorHabilitado` por defecto es `false`; lo consulta P1-04 en el paso 1 del login para decidir si exige `verify-2fa`
- **Cerrar los dos TODOs ya dejados en `security/jwt/`, ahora que la entidad existe:**
  - En `JwtAuthFilter` (comentario `TODO(usuarios-roles)`): decidir e implementar cómo se revoca el acceso de un usuario desactivado (`Usuario.estado = false`) antes de que expire su JWT — sin volver a un modelo con consulta a BD en cada request (ej. una caché de Caffeine con TTL corto que invalide por `idUsuario`, similar en espíritu a `sesion2faPendiente`/`permisosPorRol` de `config/CacheConfig`).
  - En `JwtPrincipal` (comentario `TODO(usuarios-roles)`): confirmar que ningún servicio quedó cargando el `Usuario` completo dentro del filtro; si algún endpoint necesita más que `userId`/`username`/`rol`, debe inyectar `UsuarioRepository` y buscarlo ahí, no en `JwtAuthFilter`.

**Archivos mínimos:**

```
usuario/entity/Usuario.java
usuario/entity/Rol.java
usuario/entity/Funcionalidad.java
usuario/entity/RolFuncionalidad.java
usuario/repository/*Repository.java
```

---

### P1-02 · UsuarioService — CRUD y protección de Superusuario · [PENDIENTE] · [DEPENDE DE P1-01] [AOP]

**Directorio:** `usuario/service/`, `usuario/controller/`

**Descripción**
El Superusuario crea usuarios y les asigna rol, sin poder eliminar nunca al propio Superusuario del sistema. `POST /api/usuarios` es la **única vía oficial** para dar de alta cuentas — nunca por INSERT directo a la tabla `Usuario` (un password sin pasar por `PasswordEncoder`, un rol inexistente o un flag mal puesto rompe el login o la autorización de forma difícil de diagnosticar).

**Criterios de aceptación:**

- Intento de eliminar al Superusuario lanza `BusinessException(USUARIO_NO_ELIMINABLE)`
- Creación de usuario exige rol válido existente
- Creación de usuario hashea el password con `PasswordEncoder` (ya configurado en `security/config/SecurityConfig`) — nunca lo recibe ni lo persiste en texto plano
- Los métodos de creación/edición/eliminación están anotados `@Auditable(modulo = "usuario", ...)` para que `AuditoriaAspect` (P1-11) los registre automáticamente

**Archivos mínimos:**

```
usuario/service/UsuarioService.java
usuario/service/impl/UsuarioServiceImpl.java
usuario/controller/UsuarioController.java
```

---

### P1-03 · TotpService — verificación TOTP · [ESQUELETO] · [DEPENDE DE P1-01] [BLOQUEA → P1-04]

**Directorio:** `auth/service/TotpService.java`, `auth/service/impl/TotpServiceImpl.java`

**Estado:** existen como `ITotpService`/`TotpService`, vacíos — renombrar a `TotpService` (interfaz) / `TotpServiceImpl` (implementación) para seguir la convención del resto del proyecto.

**Descripción**
Servicio de verificación **y generación** de código/secreto TOTP compatible con Google Authenticator (`dev.samstevens.totp`). La verificación (`verificarCodigo`) la usa exclusivamente el paso 2 del login (P1-04); la generación (`generarSecreto`) la usa la activación de 2FA (P1-05) — ambas viven en el mismo servicio porque son las dos caras de la misma librería TOTP, y evita que P1-04 y P1-05 dupliquen la dependencia a `dev.samstevens.totp`. La matrícula (P3-04) no vuelve a pedir el código, solo valida el claim `2fa` del JWT. `TotpService` vive en `auth/`, no en `security/`: el TOTP es parte del proceso de autenticación.

**Criterios de aceptación:**

- `TotpService.verificarCodigo(secret, codigo)` valida ventana de tiempo estándar TOTP
- Código incorrecto o expirado lanza `BusinessException(INVALID_TOTP)`
- `TotpService.generarSecreto()` crea un secreto nuevo (aleatorio, por biblioteca) y arma la URI `otpauth://` con el issuer de `TotpProperties` — no persiste nada; la persistencia (`Usuario.totpSecret`, `dosFactorHabilitado`) es responsabilidad de quien lo invoque (P1-05), vía `UsuarioRepository`
- El secreto por usuario se almacena cifrado con `core/crypto/AesConverter` (`@Convert` sobre el campo `Usuario.totpSecret`, definido en P1-01)
- El issuer se inyecta vía `TotpProperties`

**Archivos a completar:**

```
auth/service/TotpService.java
auth/service/impl/TotpServiceImpl.java
```

---

### P1-04 · AuthService — login (paso 1) y verify-2fa (paso 2) · [ESQUELETO] · [DEPENDE DE P1-01, P1-03, P1-11, PasswordEncoder, CacheConfig] [BLOQUEA → P1-05, P3-04] [CACHÉ]

**Directorio:** `auth/service/AuthService.java`, `auth/service/impl/AuthServiceImpl.java`

**Estado:** existen como `IAuthService`/`AuthService`, vacíos — renombrar a `AuthService` (interfaz) / `AuthServiceImpl` (implementación).

**Descripción**
Login con usuario y contraseña, verificando el hash con `PasswordEncoder` y respetando el límite de 5 intentos fallidos en 10 minutos. Si el usuario tiene 2FA habilitado, el paso 1 no emite el token final: guarda el estado `PENDIENTE_2FA` en la caché `sesion2faPendiente` (`config/CacheConfig`) y responde `requiere2FA: true`.

El paso 2 (`POST /api/auth/login/verify-2fa`) recibe el código TOTP, lo valida contra el estado pendiente y emite el JWT definitivo con claim `2fa: true` vía `JwtUtil.generateToken(userId, username, role, true)`.

**Criterios de aceptación:**

- Contraseña nunca se compara ni se almacena en texto plano (`passwordEncoder.matches()`)
- Antes de validar credenciales, consulta `AuditoriaService.contarIntentosFallidos()` (P1-11); si son ≥ 5 en los últimos 10 minutos, lanza `BusinessException(LOGIN_BLOCKED)` con `metadata.minutosRestantes`
- Login incorrecto invoca `AuditoriaService.registrarIntentoFallido()` y lanza `BusinessException(INVALID_CREDENTIALS)`
- Login correcto sin 2FA devuelve el token final directamente e invoca `AuditoriaService.registrarLogin()`
- Login correcto con 2FA habilitado escribe en `sesion2faPendiente` sin devolver token `[CACHÉ]`
- `verify-2fa`: si no existe estado pendiente para el usuario (expiró o nunca se creó), lanza `BusinessException(TWOFA_SESSION_EXPIRED)`
- `verify-2fa`: código inválido delega en `TotpService` (P1-03) y propaga `BusinessException(INVALID_TOTP)`
- `verify-2fa`: código válido invalida la entrada de `sesion2faPendiente` y emite el JWT definitivo
- Test cubre: login exitoso sin 2FA, login con 2FA pendiente, código correcto, código incorrecto, sesión expirada, y bloqueo por intentos fallidos

**Archivos a completar:**

```
auth/service/AuthService.java
auth/service/impl/AuthServiceImpl.java
auth/controller/AuthController.java     (no existe, crear)
auth/dto/request/LoginRequest.java      (no existe, crear)
auth/dto/response/LoginResponse.java    (no existe, crear)
```

---

### P1-05 · Cambio de contraseña y activación de 2FA · [PENDIENTE] · [DEPENDE DE P1-01, P1-03, P1-04]

**Directorio:** `auth/service/`, `auth/controller/`

**Descripción**
Dos operaciones de autoservicio sobre la cuenta ya autenticada, agrupadas porque comparten patrón (reverificar la contraseña actual antes de ejecutar la acción) y controller: cambio de contraseña, y activación de 2FA (`POST /api/auth/2fa/enable`, que faltaba en versiones previas de este plan — el endpoint ya estaba documentado en la especificación pero sin tarea que lo genere).

**Cambio de contraseña:**

- Requiere la contraseña actual correcta antes de permitir el cambio (`passwordEncoder.matches()`)
- Nueva contraseña se re-hashea con `passwordEncoder.encode()`
- La nueva contraseña se valida con Bean Validation (`@Pattern`/`@Size` en el `record CambioPasswordRequest`)

**Activación de 2FA:**

- Requiere reverificar la contraseña actual (`passwordEncoder.matches()`) antes de generar nada — evita que una sesión ya autenticada pero comprometida active 2FA sin volver a pedir el password
- Invoca `TotpService.generarSecreto()` (P1-03), persiste el secreto cifrado en `Usuario.totpSecret` y marca `Usuario.dosFactorHabilitado = true` en la misma operación
- Devuelve la URI `otpauth://` **una sola vez**, en la respuesta de este endpoint — no hay otro momento en que el secreto en texto plano vuelva a estar disponible
- No existe endpoint para deshabilitar 2FA en el alcance de este proyecto — si se necesita, es una extensión, no un requisito del enunciado
- Métodos anotados `@Auditable(modulo = "auth", ...)` para que quede registro de cuándo un usuario activó 2FA

**Archivos mínimos:**

```
auth/service/CambioPasswordService.java
auth/service/impl/CambioPasswordServiceImpl.java
auth/service/TwoFaEnrollmentService.java
auth/service/impl/TwoFaEnrollmentServiceImpl.java
auth/controller/CuentaController.java           (agrupa change-password y 2fa/enable)
auth/dto/request/CambioPasswordRequest.java
auth/dto/request/Habilitar2FaRequest.java
auth/dto/response/Habilitar2FaResponse.java
```

---

### P1-06 · Gestión de Permisos por Rol + caché de permisos · [PENDIENTE] · [DEPENDE DE P1-01, CacheConfig] [BLOQUEA → P1-08] [CACHÉ]

**Directorio:** `usuario/service/`, `usuario/controller/`

**Descripción**
El Superusuario asigna permisos (Ver, Crear, Editar, Eliminar, Imprimir) a cada rol mediante checkboxes, y ese cambio se refleja de inmediato para las sesiones activas de ese rol.

**Criterios de aceptación:**

- `PermisoService.aplicar(idRol, listaPermisos)` reemplaza los permisos vigentes del rol de forma atómica en BD
- Antes de responder, invalida explícitamente la entrada `idRol` de la caché `permisosPorRol` (`config/CacheConfig`) — criterio de aceptación obligatorio, no opcional
- `PermisoService.obtenerPermisos(idRol)` consulta primero la caché; si no está, consulta BD y la puebla — este es el método que `PermisoEvaluator` (P1-08) consume

**Archivos mínimos:**

```
usuario/service/PermisoService.java
usuario/service/impl/PermisoServiceImpl.java
usuario/controller/PermisoController.java
```

---

### P1-07 · Árbol de Funcionalidades · [PENDIENTE] · [DEPENDE DE P1-01]

**Directorio:** `usuario/service/`

**Descripción**
El menú del sistema se construye como un árbol a partir del campo `padre` de Funcionalidad.

**Criterios de aceptación:**

- Soporta al menos 3 niveles de anidamiento (módulo → submódulo → página)
- Nodos sin `padre` son raíces del árbol

**Archivos mínimos:**

```
usuario/service/FuncionalidadService.java
usuario/service/impl/FuncionalidadServiceImpl.java
```

---

### P1-08 · PermisoEvaluator (`hasPermission`) · [ESQUELETO] · [DEPENDE DE P1-06] [BLOQUEA → P2-08, P3-07] [CACHÉ]

**Directorio:** `security/permission/PermisoEvaluator.java` (existe, vacío)

**Descripción**
Cada endpoint protegido valida si el rol del usuario tiene el permiso requerido (Ver/Crear/Editar/Eliminar/Imprimir), implementando `PermissionEvaluator` de Spring Security e invocándolo desde los controllers con `@PreAuthorize("hasPermission(#id, 'ALUMNO', 'CREAR')")`.

Es el punto que `AulaController`, `AlumnoController`, `ConceptoController` (P2) y `MatriculaController`, `PagoController` (P3) deben declarar en sus endpoints vía anotación.

**Criterios de aceptación:**

- `PermisoEvaluator.hasPermission(authentication, targetId, targetType, permission)` rechaza con `BusinessException(PERMISO_DENEGADO)` si el rol no tiene el permiso exigido
- Lee siempre desde `PermisoService.obtenerPermisos()` (P1-06, respaldado por caché) — nunca consulta `Rol_Funcionalidad` directamente
- Registrado como bean en `security/config/SecurityConfig` para que `@PreAuthorize` lo resuelva automáticamente

**Archivos a completar:**

```
security/permission/PermisoEvaluator.java
```

---

### P1-09 · Seeder idempotente del Superusuario inicial · [PENDIENTE] · [DEPENDE DE P1-01 (UsuarioRepository, RolRepository), PasswordEncoder]

**Directorio:** `usuario/config/` (o `config/`, a definir por quien lo implemente)

**Descripción**
Cada integrante del equipo levanta su propia instancia local con su propia base de datos vacía, y necesita poder crear el primer Superusuario sin depender de un INSERT manual (fuente típica de errores: password sin hashear, rol inexistente, flags mal puestos). Como esto se ejecuta muchas veces — cada dev, cada vez que resetea su BD — la solución es un seeder que corre al arrancar la aplicación y que puede ejecutarse repetidamente sin duplicar nada ni fallar.

Un `CommandLineRunner` (o `ApplicationRunner`) verifica al arrancar si ya existe el Rol y el Usuario Superusuario; si no existen, los crea. Si ya existen, no hace nada — así es seguro reiniciar la aplicación cuantas veces haga falta.

**Criterios de aceptación:**

- Al arrancar, si no existe el `Rol` con nombre reservado (ej. `SUPERUSUARIO`), se crea primero — el seeder resuelve el mismo problema de arranque también para el rol, no solo para el usuario
- Si no existe ya un `Usuario` con ese rol, se crea uno usando `PasswordEncoder` para el hash — nunca un password en texto plano ni un hash calculado a mano
- **Idempotente de verdad:** ejecutar la aplicación 10 veces seguidas contra la misma BD no crea 10 Superusuarios ni lanza error de unique key — la segunda vuelta en adelante el seeder simplemente no hace nada
- Usuario y password iniciales se leen de variables de entorno (ej. `SUPERUSER_USERNAME`, `SUPERUSER_PASSWORD`) con un valor por defecto solo para desarrollo local, documentadas en `.env.example` — nunca hardcodeadas en el código
- Al crear el Superusuario, deja un log (`INFO` o `WARN`) indicando que se generó uno nuevo, para que quede claro en la consola que no es un usuario preexistente
- No corre como parte de ningún test de integración que use una BD compartida entre corridas de forma no controlada — si hace falta, se puede acotar con un `@Profile` (ej. no ejecutar en `prod` sin la variable de entorno explícita)

**Archivos mínimos:**

```
usuario/config/SuperusuarioSeeder.java
```

---

### P1-10 · Anotación `@Auditable` — contrato de auditoría declarativa · [PENDIENTE] · [BLOQUEA → P1-11]

**Directorio:** `auditoria/`

**Descripción**
Antes de que `AuditoriaAspect` pueda interceptar nada, tiene que existir la anotación que el resto del equipo ya da por hecha: `@Auditable(modulo = "usuario", ...)` (P1-02), `@Auditable(modulo = "aula", ...)` (P2-02), `@Auditable(modulo = "matricula", operacion = TipoOperacionAuditoria.MATRICULA)` (P3-03), `@Auditable(modulo = "pago", operacion = TipoOperacionAuditoria.PAGO)` (P3-06). Verificado en el repo: la anotación no existe todavía en ningún paquete — ninguno de esos usos compila hasta que Persona 1 la cree. Esta tarea fija su forma final; nadie más debe redefinirla en su propio módulo.

**Criterios de aceptación:**

- `@Auditable` es una anotación de método: `@Target(ElementType.METHOD)`, `@Retention(RetentionPolicy.RUNTIME)`
- Expone al menos `modulo` (`String`) y `operacion` (`TipoOperacionAuditoria`, enum ya existente en `core/`)
- El paquete y el nombre quedan fijados aquí (`auditoria/Auditable.java`) — el resto del equipo la importa tal cual, sin crear una copia local

**Archivos mínimos:**

```
auditoria/Auditable.java
```

---

### P1-11 · `AuditoriaService` y `AuditoriaAspect` — registro real vía AOP · [ESQUELETO] · [DEPENDE DE P1-10] [BLOQUEA → P2-09, P3-03, P3-06, P3-08]

**Directorio:** `auditoria/`

**Estado:** `AuditoriaEntity`, `AuditoriaRepository`, `AuditoriaService` y `AuditoriaAspect` ya existen como archivos con el `package` declarado y ningún método — confirmado en el repo vía MCP.

**Descripción**
Implementar el aspecto que intercepta cualquier método anotado `@Auditable` (P1-10) y persiste una fila en `AuditoriaEntity` con la estructura completa exigida por el enunciado: `codUsuario`, `modulo`, `tablaAfectada`, `operacion`, `codigoRegistro`, `valorAnterior` (JSON, antes de modificar), `valorNuevo` (JSON, después de modificar), `fechaHora`, `ipOrigen`, `equipo` (opcional), `navegador` (opcional) — más los métodos explícitos que `AuthServiceImpl` (P1-04) invoca para login/logout, que no pasan por el aspecto porque login no es una operación CRUD estándar sobre una entidad de negocio.

**Orden respecto a `@Transactional` (crítico):** el enunciado exige la secuencia "... → Registrar Auditoría → Commit" con rollback total si algo falla — la fila de auditoría debe escribirse **dentro** de la misma transacción del método de negocio, no después de que esta ya confirmó. `AuditoriaAspect` debe configurarse con `@Order` para ejecutarse **dentro** del proxy de `@Transactional` (es decir, la escritura de auditoría ocurre antes del commit real y se revierte junto con el resto si algo falla más adelante en el método). Si se implementa al revés (el aspecto de auditoría envolviendo al de transacción), la fila se escribiría después del commit, fuera del alcance del rollback — esto contradice el enunciado y debe evitarse.

**Criterios de aceptación:**

- `AuditoriaAspect` intercepta `@Auditable` sin que ningún `ServiceImpl` llame manualmente a `AuditoriaService.registrar()` (ver también INT-2)
- La fila de auditoría persiste los 11 campos de `Auditoria` según el enunciado: `codUsuario`, `modulo`, `tablaAfectada`, `operacion`, `codigoRegistro`, `valorAnterior`, `valorNuevo`, `fechaHora`, `ipOrigen`, `equipo`, `navegador` — no solo un subconjunto genérico
- `valorAnterior`/`valorNuevo` se serializan como JSON del estado de la entidad antes/después de la operación (en `INSERT`, `valorAnterior` es `null`; en `DELETE` lógico, `valorNuevo` refleja `estado = false`)
- `AuditoriaAspect` está anotado con `@Order` de forma que su ejecución quede **dentro** del límite transaccional del método interceptado — la escritura de auditoría participa del mismo commit/rollback que la operación de negocio, cumpliendo la secuencia "Registrar Auditoría → Commit" del enunciado
- `AuditoriaService.registrarLogin()` y `registrarIntentoFallido()` son invocados explícitamente por `AuthServiceImpl` (P1-04), no vía el aspecto
- `AuditoriaService.contarIntentosFallidos(idUsuario, minutos)` soporta la consulta que P1-04 necesita para el bloqueo de 5 intentos en 10 minutos
- Un error dentro del método interceptado se sigue propagando tal cual — el registro de auditoría nunca "traga" una excepción de negocio, y al propagarse el error, la fila de auditoría tampoco debe quedar persistida (rollback conjunto)

**Archivos a completar:**

```
auditoria/AuditoriaEntity.java
auditoria/AuditoriaRepository.java
auditoria/AuditoriaService.java
auditoria/AuditoriaAspect.java
```

---

### P1-12 · Documentar en `SecurityConfig` la decisión ya tomada sobre el flujo de autenticación · [PENDIENTE] · (decisión de equipo ya cerrada — descartado el flujo estándar de Spring Security; esta tarea es solo el ajuste puntual en el código) · [BLOQUEA → P1-04]

**Directorio:** `security/config/SecurityConfig.java` (ya existe y funciona — esta tarea es un ajuste puntual de comentario, no una decisión pendiente)

**Descripción**
La auditoría del repo encontró un tercer comentario `TODO(usuarios-roles)` en `SecurityConfig` (línea ~24), además de los dos ya conocidos en `JwtAuthFilter` y `JwtPrincipal` (que cierra P1-01). Dejaba abierta la posibilidad de volver al flujo estándar de Spring Security para el login — `DaoAuthenticationProvider` + `AuthenticationManager` + un `CustomUserDetailsService` respaldado por `UsuarioRepository` — en vez del enfoque que ya diseña P1-04 (`AuthServiceImpl` valida credenciales y emite el JWT directamente).

**Decisión (cerrada):** se descarta `DaoAuthenticationProvider`. Motivo: su contrato es todo-o-nada (`Authentication` completo o `AuthenticationException`), y no modela limpiamente el estado intermedio `PENDIENTE_2FA` que necesita el login en dos pasos (P1-04). Forzarlo ahí solo agregaría código para terminar en el mismo lugar: `AuthServiceImpl` decidiendo explícitamente cuándo emitir el token. Además, el rol del usuario no se resuelve vía `GrantedAuthority` en ningún punto del sistema — las autorizaciones pasan por `PermisoEvaluator` contra `Rol_Funcionalidad` (P1-08), así que un `UserDetailsService` que mapee `Rol` a `GrantedAuthority` no aportaría nada a esa ruta. El rol viaja únicamente como claim informativo del JWT.

**Criterios de aceptación:**

- Se reemplaza el comentario `TODO(usuarios-roles)` en `SecurityConfig` por uno que documente la decisión tomada y su motivo (no debe quedar una duda de arquitectura sin resolver en el código)
- No se crea ningún `CustomUserDetailsService` ni `DaoAuthenticationProvider` en el proyecto
- P1-04 puede empezar sin depender de una conversación de equipo — la decisión ya está tomada, esta tarea es solo dejarla escrita en el código

**Archivos a completar:**

```
security/config/SecurityConfig.java   (edición puntual del comentario)
```

---

## PERSONA 2 — Módulos Académicos (`aula/`, `alumno/`, `concepto/`)

---

### P2-01 · Modelos y catálogos: Aula, Alumno, Concepto, TipoConcepto · [PENDIENTE] · [DEPENDE DE BaseEntity] [BLOQUEA → P2-02, P2-03, P2-05]

**Directorio:** `aula/entity/`, `alumno/entity/`, `concepto/entity/`

**Descripción**
`aula/entity/` agrupa Aula (UK codAnioAcademico+codNivel+codGrado+seccion) junto con los catálogos AnioAcademico, Nivel y Grado (UK codNivel+nombreGrado); `alumno/entity/` agrupa Alumno (UK codTipoDocumento+numeroDocumento) junto con TipoDocumento; `concepto/entity/` agrupa Concepto (UK codAnioAcademico+nombreConcepto, campo `version`) y TipoConcepto. Todas heredan de `core/persistence/BaseEntity`.

**Criterios de aceptación:**

- Cada entidad hereda `estado` (booleano) de `BaseEntity` para eliminación lógica
- `Concepto` hereda `version` de `BaseEntity` para optimistic lock
- `Alumno.fechaNacimiento` y `Alumno.numeroDocumento` usan `@Convert(converter = AesConverter.class)`
- Los catálogos son CRUD simples, sin lógica de negocio propia

**Archivos mínimos:**

```
aula/entity/Aula.java
aula/entity/AnioAcademico.java
aula/entity/Nivel.java
aula/entity/Grado.java
aula/repository/*Repository.java
alumno/entity/Alumno.java
alumno/entity/TipoDocumento.java
alumno/repository/*Repository.java
concepto/entity/Concepto.java
concepto/entity/TipoConcepto.java
concepto/repository/*Repository.java
```

---

### P2-02 · AulaService — CRUD con capacidad y Unique Key · [PENDIENTE] · [DEPENDE DE P2-01, P2-10] [AOP]

**Directorio:** `aula/service/`, `aula/controller/`

**Descripción**
La Secretaria administra aulas controlando capacidad máxima y evitando aulas duplicadas para el mismo año/nivel/grado/sección.

**Criterios de aceptación:**

- Crear un aula duplicada (mismo año, nivel, grado, sección) lanza `BusinessException(AULA_DUPLICADA)`
- El valor por defecto de `capacidadMaxima` es configurable vía `Parametro` (`VACANTES_MAXIMAS_DEFAULT`, leído a través de `ParametroService`, P2-10); la capacidad ya asignada a un aula específica queda persistida en la fila de `Aula`
- Los campos del `record AulaRequest` usan Bean Validation (`@Positive`, `@NotBlank`, etc.)
- Create/update/delete están anotados `@Auditable(modulo = "aula", ...)`

**Archivos mínimos:**

```
aula/service/AulaService.java
aula/service/impl/AulaServiceImpl.java
aula/controller/AulaController.java
aula/dto/request/AulaRequest.java
```

---

### P2-03 · AlumnoService — CRUD con cifrado AES · [PENDIENTE] · [DEPENDE DE P2-01, AesConverter] [AOP]

**Directorio:** `alumno/service/`, `alumno/controller/`

**Descripción**
La Secretaria registra alumnos cuyos campos `fechaNacimiento` y `numeroDocumento` se cifran automáticamente vía `@Convert(converter = AesConverter.class)`, evitando duplicados por tipo+número de documento.

**Criterios de aceptación:**

- `fechaNacimiento` y `numeroDocumento` se almacenan cifrados en BD y se desencriptan solo al servir la respuesta
- Duplicado de (codTipoDocumento, numeroDocumento) lanza `BusinessException(ALUMNO_DUPLICADO)`
- `AlumnoServiceImpl` no contiene ninguna llamada manual a cifrado/descifrado — eso es responsabilidad exclusiva de `AesConverter`
- Create/update/delete están anotados `@Auditable(modulo = "alumno", ...)`

**Archivos mínimos:**

```
alumno/service/AlumnoService.java
alumno/service/impl/AlumnoServiceImpl.java
alumno/controller/AlumnoController.java
```

---

### P2-04 · Validación de formularios con Bean Validation · [PENDIENTE] · [BLOQUEA → P2-02, P2-03, P2-05]

**Directorio:** anotaciones en los `record` de cada módulo

**Descripción**
Aula, Alumno y Concepto validan texto, numérico, fecha y caracteres especiales usando `spring-boot-starter-validation` sobre los `record` de request (`@NotBlank`, `@Pattern`, `@Past`, `@Positive`, etc.).

**Criterios de aceptación:**

- Cada `record` de request de `aula/`, `alumno/` y `concepto/` declara sus restricciones con anotaciones estándar de `jakarta.validation.constraints`
- Los controllers marcan el parámetro con `@Valid`
- `GlobalExceptionHandler` (ya implementado en `core/`) traduce cualquier `MethodArgumentNotValidException` a `BusinessException(VALIDACION_FORMULARIO)` con el detalle de cada campo en `metadata`

---

### P2-05 · ConceptoService — CRUD con optimistic lock · [PENDIENTE] · [DEPENDE DE P2-01] [BLOQUEA → P3-02] [AOP]

**Directorio:** `concepto/service/`, `concepto/controller/`

**Descripción**
La Secretaria administra el tarifario de conceptos por año académico, detectando ediciones concurrentes mediante el campo `version` heredado de `BaseEntity`.

**Criterios de aceptación:**

- Guardar con una `version` desactualizada lanza `BusinessException(VERSION_CONFLICT)` con `metadata.versionActual` y `metadata.versionEnviada`
- Monto igual o menor a cero es rechazado por Bean Validation (`@Positive` en `ConceptoRequest`)
- Duplicado de (codAnioAcademico, nombreConcepto) lanza `BusinessException(CONCEPTO_DUPLICADO)`
- Create/update/delete están anotados `@Auditable(modulo = "concepto", ...)`

**Archivos mínimos:**

```
concepto/service/ConceptoService.java
concepto/service/impl/ConceptoServiceImpl.java
concepto/controller/ConceptoController.java
```

---

### P2-06 · TipoConceptoService · [PENDIENTE]

**Directorio:** `concepto/service/`

**Descripción**
La Secretaria administra los tipos de concepto que clasifican el tarifario.

**Criterios de aceptación:**

- CRUD estándar con eliminación lógica (`estado` de `BaseEntity`)

**Archivos mínimos:**

```
concepto/service/TipoConceptoService.java
concepto/service/impl/TipoConceptoServiceImpl.java
```

---

### P2-07 · Clonado de conceptos entre años académicos · [PENDIENTE] · [DEPENDE DE P2-05]

**Directorio:** `concepto/service/`

**Descripción**
La Secretaria clona el tarifario de un año académico a otro (`POST /api/conceptos/clonar`) sin repetirlo manualmente cada periodo.

**Criterios de aceptación:**

- Clonar no duplica registros si se ejecuta más de una vez sobre el mismo año destino
- Los conceptos clonados inician con `version = 0`
- Respuesta incluye `conceptosClonados` y `anioDestino`

**Archivos mínimos:**

```
concepto/service/ClonadorConceptoService.java
```

---

### P2-08 · Endpoints de búsqueda modal (Aula/Alumno) · [PENDIENTE] · [DEPENDE DE P2-02, P2-03, GlobalExceptionHandler, P1-08] [BLOQUEA → P3-02, P3-03]

**Directorio:** `aula/controller/`, `alumno/controller/`

**Descripción**
Endpoints de búsqueda de Aula y Alumno pensados para un modal, consumidos por el flujo de Matrícula sin acceder directamente a las tablas de estos módulos.

**Criterios de aceptación:**

- `GET /api/aulas?anioAcademico=&nivel=` y un endpoint equivalente de alumno responden con los campos mínimos necesarios para matricular (id, descripción, `vacantesDisponibles` / datos identificatorios)
- Ambos protegidos con `@PreAuthorize("hasPermission(...)")` resuelto por `PermisoEvaluator` (P1-08)

**Archivos mínimos:**

```
aula/controller/AulaBusquedaController.java
alumno/controller/AlumnoBusquedaController.java
```

---

### P2-09 · Verificación de eliminación lógica y de auditoría vía AOP · [PENDIENTE] · [DEPENDE DE P1-11]

**Directorio:** `aula/`, `alumno/`, `concepto/` (tests)

**Descripción**
Confirmar que Aula, Alumno y Concepto usan el mismo patrón de eliminación lógica (`estado` de `BaseEntity`) y que cada operación de escritura dispara `AuditoriaAspect` (P1-11).

**Criterios de aceptación:**

- Registro "eliminado" desaparece de listados activos pero persiste en BD con `estado = false`
- Cada create/update/delete de Aula, Alumno y Concepto genera una fila de auditoría sin que el test invoque `AuditoriaService` directamente

**Archivos mínimos:**

```
aula/EliminacionLogicaTest.java
alumno/EliminacionLogicaTest.java
concepto/EliminacionLogicaTest.java
auditoria/AuditoriaAspectIntegrationTest.java
```

---

### P2-10 · Módulo Parametro — modelo, servicio y caché · [PENDIENTE] · [DEPENDE DE BaseEntity, CacheConfig] [BLOQUEA → P2-02, P3-02]

**Directorio:** `parametro/` (paquete completo por crear — no existe en el repo)

**Descripción**
Tabla de configuración de negocio clave-valor, editable en caliente por el Superusuario sin redeployar (ej. `VACANTES_MAXIMAS_DEFAULT`, `VALIDAR_DEUDA_ANIO_ANTERIOR`). Es su propio módulo de primer nivel porque lo consumen tanto `aula/` (P2-02) como `matricula/` (P3-02), sin relación de dominio exclusiva con ninguno de los dos, por lo que se asigna a Persona 2 sin generar una dependencia nueva para Persona 3 (que ya depende de Persona 2 por P2-05 y P2-08). Debe completarse antes que `P2-02`.

**Criterios de aceptación:**

- Entidad `Parametro` (clave String única, valor, descripción), hereda de `BaseEntity`
- `ParametroService.obtener(clave)` lee primero de la caché `parametrosSistema` (`config/CacheConfig`, TTL 30 min); si no está, consulta BD y la puebla
- `ParametroService.actualizar(clave, valor)` (Superusuario) escribe en BD e invalida/actualiza la entrada correspondiente de la caché
- No expone ningún método para cachear el conteo de vacantes disponibles por aula — ese valor se calcula siempre en vivo dentro de `MatriculaService` (P3-03), contra BD y dentro de la transacción

**Archivos mínimos:**

```
parametro/entity/Parametro.java
parametro/repository/ParametroRepository.java
parametro/service/ParametroService.java
parametro/service/impl/ParametroServiceImpl.java
parametro/controller/ParametroController.java
```

---

## PERSONA 3 — Módulos Transaccionales (`matricula/`, `pago/`, `reporte/`)

---

### P3-01 · Modelos Matricula, Cuota, Pago, Recibo · [PENDIENTE] · [DEPENDE DE BaseEntity]

**Directorio:** `matricula/entity/`, `pago/entity/`

**Descripción**
`matricula/entity/` agrupa Matricula (UK codAlumno+codAnioAcademico) y Cuota (generada desde Concepto, con `estado` `PENDIENTE`/`BLOQUEADA`/`PAGADA`); `pago/entity/` agrupa Pago y Recibo (con correlativo único).

**Criterios de aceptación:**

- `Matricula` tiene unique key `(codAlumno, codAnioAcademico)` — un alumno no puede matricularse dos veces en el mismo año
- `Cuota` referencia a `Concepto` (módulo `concepto/`) y a `Matricula`, y copia `montoPagar`/`ordenPago` del concepto al momento de generarse
- `Recibo.numeroRecibo` mantiene un correlativo único e incremental (formato `R-<año>-<secuencial>`)

**Archivos mínimos:**

```
matricula/entity/Matricula.java
matricula/entity/Cuota.java
matricula/repository/*Repository.java
pago/entity/Pago.java
pago/entity/Recibo.java
pago/repository/*Repository.java
```

---

### P3-02 · MatriculaValidator · [PENDIENTE] · [DEPENDE DE P3-01, P2-10, P2-05, P2-08, ErrorCode] [BLOQUEA → P3-03]

**Directorio:** `matricula/service/`

**Descripción**
Valida antes de matricular que: el aula existe, el alumno existe, el alumno no está matriculado ese año, el aula tiene vacantes (según `Parametro`, vía P2-10), el alumno no tiene deudas previas y existen conceptos activos. El proyecto es un monolito modular: `MatriculaValidator` reutiliza la lógica pública de `AulaService`/`AlumnoService` (P2-08) y `ConceptoService` (P2-05) — nunca hace llamadas HTTP a `AulaController`/`AlumnoController` ni accede directamente a los repositorios de esos módulos. Los endpoints de búsqueda que expone P2-08 quedan disponibles para Postman como cliente externo mientras no exista frontend, pero eso no cambia la forma en que `matricula/` consume `aula/`, `alumno/` y `concepto/` internamente: siempre a través de sus Services.

**Criterios de aceptación:**

- Aula sin vacantes lanza `BusinessException(AULA_SIN_VACANTES)` con `metadata.codAula` y `metadata.capacidadMaxima`
- Alumno ya matriculado ese año lanza conflicto por la unique key de `Matricula` (P3-01)
- Alumno con deuda previa lanza `BusinessException(CUOTA_ANTERIOR_PENDIENTE)` si la regla está activa (parámetro `VALIDAR_DEUDA_ANIO_ANTERIOR`, vía P2-10)

**Archivos mínimos:**

```
matricula/service/MatriculaValidator.java
```

---

### P3-03 · MatriculaService — transacción completa · [PENDIENTE] · [DEPENDE DE P3-02, P1-11] [AOP]

**Directorio:** `matricula/service/`

**Descripción**
Registrar una matrícula es una transacción única: Alumno → Registrar Matrícula → Generar Cuotas → Actualizar Aula → Registrar Auditoría → Commit, con rollback total ante cualquier error — esta es la secuencia exacta del enunciado, y la fila de auditoría debe quedar dentro del mismo límite transaccional (ver el requisito de `@Order` en P1-11), no escrita después de que la transacción ya confirmó. El método queda anotado `@Auditable(modulo = "matricula", operacion = TipoOperacionAuditoria.MATRICULA)` y `AuditoriaAspect` (P1-11) genera la fila como parte de la misma transacción, antes del commit real.

**Criterios de aceptación:**

- Cuotas generadas corresponden exactamente a los conceptos activos del año académico
- El conteo de vacantes disponibles se recalcula contra BD dentro de esta misma transacción (no se lee de ningún caché)
- Un fallo en cualquier paso revierte todo (`@Transactional`): no queda matrícula, cuota ni fila de auditoría parcial en BD
- El aspecto de auditoría escribe su fila dentro de la misma transacción de negocio (antes del commit), de modo que un fallo posterior también revierte esa fila

**Archivos mínimos:**

```
matricula/service/MatriculaService.java
matricula/service/impl/MatriculaServiceImpl.java
```

---

### P3-04 · Validación del claim 2FA en la confirmación de matrícula · [PENDIENTE] · [DEPENDE DE P1-04, P3-03] [INTEGRACIÓN P1+P3]

**Directorio:** `matricula/security/`

**Descripción**
`MatriculaController` exige que el JWT de la sesión tenga el claim `2fa: true` (emitido en verify-2fa, P1-04) antes de ejecutar la transacción de P3-03. No se vuelve a pedir ni validar un código TOTP en este paso — el doble factor ya se resolvió por completo en el login. El chequeo lee el claim vía `security/jwt/JwtUtil`.

**Criterios de aceptación:**

- Token sin el claim `2fa: true` rechaza la petición con `BusinessException(TWOFA_REQUIRED)` sin ejecutar la transacción
- No importa `TotpService` (P1-03) ni duplica lógica TOTP — solo lee un claim del token ya emitido

**Archivos mínimos:**

```
matricula/security/TwoFaClaimInterceptor.java
```

---

### P3-05 · PagoService — deudas y orden de pago · [PENDIENTE] · [DEPENDE DE P3-01]

**Directorio:** `pago/service/`

**Descripción**
La Secretaria lista las cuotas pendientes de un alumno y bloquea el pago de una cuota si existen cuotas anteriores sin pagar.

**Criterios de aceptación:**

- Intentar pagar una cuota fuera de orden lanza `BusinessException(CUOTA_ANTERIOR_PENDIENTE)` con `metadata.codCuota` y `metadata.codCuotaPendienteAnterior`
- Listado de deudas ordenado por `ordenPago` del concepto asociado

**Archivos mínimos:**

```
pago/service/PagoService.java
pago/service/impl/PagoServiceImpl.java
```

---

### P3-06 · Transacción de pago · [PENDIENTE] · [DEPENDE DE P3-05, P1-11] [AOP]

**Directorio:** `pago/service/`

**Descripción**
Registrar un pago es una transacción única: Generar Recibo → Actualizar Correlativo → Marcar Cuota como Pagada → Registrar Auditoría → Commit, con rollback total ante error — la fila de auditoría queda dentro de la misma transacción, no escrita después del commit. El método queda anotado `@Auditable(modulo = "pago", operacion = TipoOperacionAuditoria.PAGO)` para que `AuditoriaAspect` (P1-11) registre automáticamente como parte de esa misma transacción.

**Criterios de aceptación:**

- Un fallo durante el proceso no deja recibo generado ni correlativo actualizado ni cuota marcada como pagada, ni fila de auditoría (`@Transactional`)
- El aspecto de auditoría escribe su fila dentro de la misma transacción, antes del commit — no después

**Archivos mínimos:**

```
pago/service/PagoTransaccionService.java
```

---

### P3-07 · MatriculaController + PagoController · [PENDIENTE] · [DEPENDE DE P3-03, P3-06, P1-08, GlobalExceptionHandler]

**Directorio:** `matricula/controller/`, `pago/controller/`

**Descripción**
Endpoints REST de matrícula y pago, protegidos con `@PreAuthorize` (resuelto por `PermisoEvaluator`, P1-08) y con manejo de errores vía `GlobalExceptionHandler`.

**Criterios de aceptación:**

- Endpoints requieren permiso correspondiente según rol
- `POST /api/matriculas` aplica además `TwoFaClaimInterceptor` (P3-04)
- Errores de negocio devuelven el código HTTP definido en `ErrorCode`

**Archivos mínimos:**

```
matricula/controller/MatriculaController.java
pago/controller/PagoController.java
```

---

### P3-08 · ReporteController — matrículas, pagos, deudas y auditoría · [PENDIENTE] · [DEPENDE DE P3-03, P3-06, P1-11, GlobalExceptionHandler]

**Directorio:** `reporte/service/`, `reporte/controller/`

**Descripción**
Cuatro reportes de solo lectura: matrículas filtrables por año/nivel/grado/aula, pagos recibidos en un rango de fechas con total recaudado, deudas consolidadas por alumno, y el historial de auditoría filtrado por usuario/módulo/fecha (incluyendo eventos automáticos vía `AuditoriaAspect` y los de login/logout registrados explícitamente por `AuthServiceImpl`). `reporte/` es un módulo propio que mantiene la misma estructura Controller/Service/ServiceImpl del resto del proyecto: `ReporteController` no consume Services de otros módulos directamente, sino a través de `ReporteService`, que internamente reutiliza los Services públicos de `alumno/`, `matricula/`, `pago/`, `concepto/` y `AuditoriaService` — sin capa de puertos/adaptadores, igual que el resto del backend.

**Criterios de aceptación:**

- `GET /api/reportes/matriculas` filtra por `anioAcademico`, `codNivel`, `codGrado`, `codAula`
- `GET /api/reportes/pagos` devuelve `totalRecaudado`, `cantidadPagos` y el detalle, coincidiendo con la suma de pagos registrados en BD para ese rango de fechas
- `GET /api/reportes/deudas` devuelve alumnos con cuotas `PENDIENTE` o `BLOQUEADA`, con `montoAdeudado` agregado
- `GET /api/reportes/auditoria` soporta filtros combinables por usuario, módulo y rango de fecha, e incluye los eventos `LOGIN_FAILED`
- Los cuatro son de solo lectura: no generan fila de auditoría de escritura
- `ReporteController` delega toda la lógica de agregación en `ReporteService`/`ReporteServiceImpl`, igual que el resto de los Controllers del proyecto
- `GlobalExceptionHandler` cubre los errores de los cuatro endpoints

**Archivos mínimos:**

```
reporte/service/ReporteService.java
reporte/service/impl/ReporteServiceImpl.java
reporte/controller/ReporteController.java
```

---

### P3-09 · Tests de integración de Matrícula, Pago y Rate Limiting end-to-end · [PENDIENTE] · [DEPENDE DE P3-04, P3-06, P3-07]

**Directorio:** `src/test/java/com/institucion/sigea/integration/`

**Descripción**
Tests de integración del flujo HTTP completo: login en dos pasos, crear matrícula con el claim 2FA y rollback ante error, registrar pago con orden de cuotas y rollback ante error, bloqueo por rate limiting y disparo del `AuditoriaAspect`.

**Criterios de aceptación:**

- `login_dos_pasos_exitoso`: login → `requiere2FA: true` → verify-2fa con código correcto → token con claim `2fa: true`
- `login_bloqueado_por_intentos`: 5 credenciales incorrectas en menos de 10 minutos → sexto intento devuelve `429 LOGIN_BLOCKED`
- `matricula_exitosa`: flujo completo genera matrícula, cuotas y una fila de auditoría
- `matricula_rollback`: error simulado a mitad de proceso no deja rastro parcial en BD ni fila de auditoría
- `matricula_sin_2fa_rechazada`: token sin claim `2fa` devuelve `403 TWOFA_REQUIRED`
- `pago_fuera_de_orden`: rechazado con `400 CUOTA_ANTERIOR_PENDIENTE`
- `pago_exitoso_rollback`: error simulado no deja recibo/correlativo/cuota a medio actualizar

**Archivos mínimos:**

```
src/test/java/com/institucion/sigea/auth/AuthIntegrationTest.java
src/test/java/com/institucion/sigea/matricula/MatriculaIntegrationTest.java
src/test/java/com/institucion/sigea/pago/PagoIntegrationTest.java
src/test/java/com/institucion/sigea/integration/EndToEndIntegrationTest.java
```

---

## INTEGRACIONES

---

### INT-1 · Contrato de búsqueda Aula/Alumno · P2 + P3

**Prerrequisitos:** P2-08 (borrador), P3-02 (borrador)

**Objetivo:** verificar que los campos que `MatriculaValidator` necesita están presentes en la respuesta de los endpoints de búsqueda. Ajustar antes de que ambas partes finalicen.

**Artefacto:** ejemplo de respuesta JSON aprobado por ambas partes.

---

### INT-2 · Auditoría vía AOP en todos los módulos · P1 + P2 + P3

**Prerrequisitos:** P1-10, P1-11

**Objetivo:** confirmar que los métodos de escritura de `UsuarioServiceImpl`, `AulaServiceImpl`, `AlumnoServiceImpl`, `ConceptoServiceImpl`, `MatriculaServiceImpl` y `PagoServiceImpl` están correctamente anotados `@Auditable` y que `AuditoriaAspect` los intercepta sin que ningún `ServiceImpl` invoque `AuditoriaService.registrar()` manualmente. Confirmar además que `AuthServiceImpl` (P1-04) sigue invocando explícitamente `registrarLogin()`/`registrarIntentoFallido()`.

**Artefacto:** checklist firmado por los tres, con una fila de auditoría de ejemplo por cada operación (incluyendo un `LOGIN_FAILED`).

---

### INT-3 · Contrato de caché — permisos y parámetros · P1

**Prerrequisitos:** CacheConfig, P1-06, P2-10

**Objetivo:** confirmar que la invalidación de la caché de permisos ocurre en cada `PUT /api/roles/{idRol}/permisos` sin excepciones, que `PermisoEvaluator` (P1-08) nunca consulta `Rol_Funcionalidad` directamente, y que el refresco de la caché de parámetros no introduce un retraso mayor al TTL configurado al cambiar `VACANTES_MAXIMAS_DEFAULT`.

**Artefacto:** prueba manual documentada: cambiar un permiso y confirmar que una sesión activa lo ve reflejado sin reiniciar la app.

---

### INT-4 · 2FA: del login al claim del JWT · P1 + P3

**Prerrequisitos:** P1-04, P3-04

**Objetivo:** confirmar que `MatriculaController` valida el claim `2fa` emitido por verify-2fa (P1-04) sin volver a invocar `TotpService`, y que un token de una sesión sin 2FA verificado es rechazado.

**Artefacto:** revisión de código conjunta.

---

### INT-5 · Flujo completo end-to-end · Todos

**Prerrequisitos:** P2-09, P3-08, P3-09

**Objetivo:** ejecutar con curl, Postman o Swagger UI (`/swagger-ui`) el flujo completo: login (2 pasos) → crear alumno/aula/concepto → matricular → pagar cuotas en orden → revisar auditoría y reportes → forzar 6 logins fallidos y confirmar el `429`.

**Artefacto:** colección Postman o script documentado.

---

## Resumen de conteo

| Bloque | Tareas totales | [HECHO] | [ESQUELETO] | [PENDIENTE] |
|---|---|---|---|---|
| Persona 1 — Seguridad, Auth, Usuarios y Auditoría | 12 | 0 | 3 (P1-03, P1-08, P1-11) | 9 |
| Persona 2 — Académico (aula/alumno/concepto) + Parámetros | 10 | 0 | 0 | 10 |
| Persona 3 — Transaccional y Reportes (incluye auditoría) | 9 | 0 | 0 | 9 |
| **Total** | **31** | **0** | **3** | **28** |

**Nota sobre el conteo como medida de carga:** 12/10/9 es un conteo de tarjetas, no de esfuerzo. Dentro de Persona 1, tareas como P1-07 (árbol de funcionalidades) o P1-09 (seeder) son de un par de horas, mientras que P1-01 (cuatro entidades + cierre de tres TODOs reales), P1-04 (login en dos pasos con caché, rate limiting y AOP), P1-08 (PermisoEvaluator, punto de convergencia con P2-08 y P3-07) o P1-11 (aspecto de auditoría que otras tareas del resto del equipo dan por hecho) son de varios días y concentran la mayor parte del riesgo técnico. Se recomienda no tocar el número de tareas, pero sí agregar una fila de "complejidad estimada" (alta/media/baja) junto al conteo, para que la planificación de sprints no reparta el tiempo por partes iguales asumiendo que las tareas de Persona 1 equivalen en esfuerzo a las de Persona 2 o Persona 3.

---

## Concentración de bloqueos en Persona 1

Persona 1 concentra casi todos los puntos de bloqueo del proyecto (autenticación, usuarios, permisos, auditoría, parámetros, JWT, 2FA) porque esos módulos son infraestructura transversal que el resto del backend consume: no hay forma de mover `PermisoEvaluator`, `AuditoriaAspect` o `ParametroService` a otro paquete sin romper la separación por dominio (`security/`, `auditoria/`, `parametro/` no pertenecen conceptualmente a Académico ni a Transaccional). Redistribuir estos módulos entre personas rompería la filosofía de monolito modular, así que no se recomienda.

Lo que sí puede reducirse es el tiempo muerto causado por el orden de trabajo, no por la distribución de módulos. Dos ajustes concretos:

- Separar, dentro de Persona 1, lo que es "contrato" de lo que es "implementación completa": `P1-10` (la anotación `@Auditable`) es una tarea de minutos y debe completarse primero que cualquier otra cosa, porque P1-02, P2-02, P2-03, P2-05, P3-03 y P3-06 solo necesitan que la anotación *exista* para compilar — no necesitan que `AuditoriaAspect` (P1-11) ya intercepte nada. Esto permite que Persona 2 y Persona 3 anoten sus métodos con `@Auditable` desde el primer sprint sin esperar a que Persona 1 termine la implementación completa del aspecto.
- `P1-12` (decisión sobre el flujo de autenticación) ya está cerrada como decisión de equipo (se descarta `DaoAuthenticationProvider`, ver motivo en el task card) — no genera archivos nuevos ni requiere una reunión: es solo reemplazar el comentario `TODO` en `SecurityConfig` por uno que documente la decisión, y puede hacerlo la misma persona que ejecuta P1-01/P1-04 en minutos, sin bloquear el sprint.

---

## Estrategia de pruebas de auditoría (P2-09 / P3-09)

Actualmente `P1-11` bloquea a `P2-09` y (transitivamente, vía `P3-03`/`P3-06`) a `P3-09`, porque ambas verifican que `AuditoriaAspect` efectivamente intercepta las operaciones de escritura. Mover esta verificación antes de que `AuditoriaAspect` esté implementado no tiene sentido — no hay nada que probar. Lo que sí conviene es dejar explícito que **P2-09 y P3-09 son tareas de cierre, no de flujo principal**: no deben programarse antes del sprint final, y su función real es la misma que la de `INT-2` (auditoría vía AOP en todos los módulos). En la práctica, `P2-09` y `P3-09` pueden fusionarse operativamente con `INT-2` en la misma sesión conjunta en vez de ejecutarse por separado — evita que Persona 2 y Persona 3 dupliquen la verificación que de todas formas van a repetir en la integración final. La planificación de sprints (más abajo) ya las ubica en el último bloque junto con las integraciones, que es la posición correcta.

---

## Resumen de dependencias críticas

| Tarea bloqueante | Estado | Desbloquea |
|---|---|---|
| `BaseEntity` (`core/persistence/`) | [HECHO] | P1-01, P2-01, P2-10, P3-01 |
| `ErrorCode` / `BusinessException` (`core/`) | [HECHO] | P3-02, P3-07 |
| `PasswordEncoder` (`security/config/SecurityConfig`) | [HECHO] | P1-02, P1-04, P1-09 |
| `AesConverter` (`core/crypto/`) | [HECHO] | P2-03 |
| `GlobalExceptionHandler` (`core/`) | [HECHO] | P2-04, P2-08, P3-07, P3-08 |
| `CacheConfig` (Caffeine) | [HECHO] | P1-04, P1-06, P2-10 |
| P1-10 Anotación `@Auditable` | [PENDIENTE] | P1-11 |
| P1-11 AuditoriaService + AuditoriaAspect | [ESQUELETO] | P1-04, P2-09, P3-03, P3-06, P3-08 |
| P1-12 Comentario de `SecurityConfig` documentando la decisión ya tomada (no `DaoAuthenticationProvider`) | [PENDIENTE] | P1-04 (ajuste de minutos, no bloquea por decisión de equipo — ya está tomada) |
| P1-01 Entidades Usuario/Rol | [PENDIENTE] | P1-02, P1-06, P1-07, P1-09 |
| P1-03 TotpService | [ESQUELETO] | P1-04 |
| P1-06 Permisos + caché | [PENDIENTE] | P1-08 |
| P1-08 PermisoEvaluator | [ESQUELETO] | P2-08, P3-07 |
| P2-10 Módulo Parametro | [PENDIENTE] | P2-02, P3-02 |
| P2-05 ConceptoService | [PENDIENTE] | P3-02 |
| P2-08 Búsqueda Aula/Alumno | [PENDIENTE] | P3-02, P3-03 |
| P3-02 MatriculaValidator | [PENDIENTE] | P3-03 |

**Ruta crítica recomendada para arrancar:** Persona 1 cierra `P1-10 → P1-11` (auditoría) cuanto antes, en paralelo con `P1-01` (entidades de usuario/rol), porque ambas bloquean tareas de las tres personas. `P1-12` ya no requiere una decisión de equipo — se descartó `DaoAuthenticationProvider` (el estado intermedio `PENDIENTE_2FA` no encaja en su contrato todo-o-nada, y las autorizaciones no pasan por `GrantedAuthority` sino por `PermisoEvaluator`) — así que Persona 1 solo debe dejar ese comentario documentado antes de tocar `P1-04`, sin necesitar una reunión. Persona 2 y Persona 3 pueden empezar sus modelos (`P2-01`, `P3-01`) de inmediato, sin depender de nada pendiente de Persona 1.

**Principales puntos de choque entre Persona 2 y Persona 3:** `P2-10` (Parametro) alimenta tanto a `P2-02` (capacidad de aula) como a `P3-02` (validación de vacantes y deuda) — si Persona 1 se atrasa en `P2-10`, ambas personas quedan bloqueadas al mismo tiempo. `P2-05` (Concepto) y `P2-08` (búsqueda Aula/Alumno) son prerrequisito directo de `P3-02`, así que Persona 3 no puede avanzar su validador hasta que Persona 2 termine su Sprint 2. `PermisoEvaluator` (P1-08) es un segundo punto de convergencia: bloquea tanto a `P2-08` como a `P3-07`. `AuditoriaAspect` (P1-11) es el tercero, y el más ancho: bloquea a `P3-08`, `P2-09` y `P3-03`/`P3-06` simultáneamente.

---

## Orden de implementación propuesto

La distribución por módulos se mantiene para lo que es infraestructura de seguridad (Persona 1 = Seguridad/Auth/Usuarios/Auditoría), Académico (Persona 2) y Transaccional (Persona 3), con el módulo Parametro reasignado a Persona 2 y el reporte de auditoría integrado en Persona 3, según el balance de carga descrito más abajo. Lo que sí puede mejorarse es el orden en que cada persona ejecuta sus propias tareas, respetando las dependencias reales entre ellas.

Persona 1, en orden:

1. P1-01 (entidades Usuario/Rol) — desbloquea P1-02, P1-03, P1-06, P1-07, P1-09
2. P1-10 (anotación @Auditable) — tarea corta, desbloquea P1-11 y permite que Persona 2 y Persona 3 empiecen a anotar sus métodos desde ya
3. P1-12 (documentar en el código la decisión ya tomada: se descarta `DaoAuthenticationProvider`) — no genera archivos nuevos, es un ajuste de comentario de minutos, sin reunión de equipo pendiente
4. P1-11 (AuditoriaService + AuditoriaAspect)
5. P1-03 (TotpService)
6. P1-06 (permisos + caché) — desbloquea P1-08
7. P1-02 (UsuarioService) — solo depende de P1-01
8. P1-07 (árbol de funcionalidades) — solo depende de P1-01
9. P1-09 (seeder) — solo depende de P1-01
10. P1-04 (AuthService, login + 2FA) — requiere P1-03, P1-11 y la decisión de P1-12 ya cerrada
11. P1-08 (PermisoEvaluator) — requiere P1-06; desbloquea P2-08 y P3-07
12. P1-05 (cambio de contraseña y activación de 2FA) — requiere P1-04 y P1-03 (`generarSecreto`, ya disponible desde el paso 5)

Persona 2, en orden:

1. P2-01 (modelos Aula/Alumno/Concepto) — no depende de otra persona
2. P2-04 (Bean Validation) — no depende de otra persona
3. P2-10 (módulo Parametro) — no depende de otra persona; debe quedar lista antes de llegar a P2-02
4. P2-03 (AlumnoService) — solo requiere P2-01
5. P2-02 (AulaService) — requiere P2-01 y P2-10
6. P2-05 (ConceptoService) — requiere P2-01; desbloquea P2-07 y, junto con P2-08, a P3-02
7. P2-06 (TipoConceptoService) — solo depende de P2-01
8. P2-07 (clonado de conceptos) — requiere P2-05
9. P2-08 (búsqueda Aula/Alumno) — requiere P2-02, P2-03 y que Persona 1 entregue P1-08; es el último prerrequisito real para que Persona 3 pueda empezar P3-02
10. P2-09 (verificación de eliminación lógica y auditoría) — requiere que Persona 1 entregue P1-11; se ejecuta al final, junto con las integraciones

Persona 3, en orden:

1. P3-01 (modelos Matricula/Cuota/Pago/Recibo) — no depende de nadie más, arranca de inmediato
2. P3-05 (PagoService, deudas y orden de pago) — solo depende de P3-01; conviene adelantarla aquí mismo, mientras se espera a que Persona 2 entregue P2-05 y P2-08, para no dejar a Persona 3 sin trabajo
3. P3-02 (MatriculaValidator) — requiere P3-01, y de Persona 1 y Persona 2: P2-10, P2-05 y P2-08. Este es el punto real donde Persona 3 queda a la espera, no antes
4. P3-03 (MatriculaService, transacción completa) — requiere P3-02 y que Persona 1 entregue P1-11
5. P3-06 (transacción de pago) — requiere P3-05 y P1-11
6. P3-04 (validación del claim 2FA) — requiere P3-03 y que Persona 1 entregue P1-04
7. P3-07 (MatriculaController + PagoController) — requiere P3-03, P3-06 y que Persona 1 entregue P1-08
8. P3-08 (ReporteController + ReporteService, incluye el reporte de auditoría) — requiere P3-03, P3-06 y que Persona 1 entregue P1-11
9. P3-09 (tests de integración) — requiere P3-04, P3-06 y P3-07; se ejecuta al final, junto con las integraciones

Cierre común, después de que las tres personas terminen lo anterior: P2-09, P3-09, INT-1, INT-2, INT-3, INT-4, INT-5.

Nota: P3-02 depende directamente de P2-08, así que Persona 3 no puede empezar su validador hasta que Persona 2 entregue la búsqueda de Aula/Alumno, no simultáneamente. P3-05 no depende de P3-02 ni P3-03, lo que le da a Persona 3 una tarea real para adelantar mientras espera a Persona 1 y Persona 2, en lugar de quedar completamente bloqueada.
