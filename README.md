# PortalCapacitaciones

# Portal de Capacitaciones – Arquitectura y Entidades

##  Arquitectura General



- **Frontend (React + Vite, puerto 3000)**
  - Consume API vía JWT.
- **API Gateway (Spring Cloud Gateway, puerto 8080)**
  - Rutea requests a microservicios.
  - Valida JWT y puede propagar identidad vía headers.
- **Usuarios Service (Spring Boot, puerto 8081)**
  - Maneja autenticación (local + OAuth2 Keycloak).
  - Gestiona usuarios, inscripciones y progreso.
  - Persistencia en Postgres `usuarios_db`.
- **Cursos Service (Spring Boot, puerto 8082)**
  - Gestiona cursos, módulos y capítulos.
  - Controla permisos por roles.
  - Persistencia en Postgres `cursos_db`.
- **Keycloak**
  - Identity Provider (OAuth2/OpenID).
  - Persistencia en `keycloak_db`.

---

## Entidades principales

### Usuarios Service



- **UserEntity**
  - `id: UUID (PK)`
  - `email`, `fullName`, `role`, `provider`, `enabled`
  - timestamps
- **CourseEnrollmentEntity**
  - `id: UUID (PK)`
  - `user_id (FK -> User)`
  - `courseId (UUID, referencia a Cursos)`
  - `status (ENROLLED|COMPLETED)`
- **ModuleProgressEntity**
  - `id: UUID (PK)`
  - `enrollment_id (FK -> Enrollment)`
  - `moduleId (UUID, referencia a Cursos)`
  - `status (NOT_STARTED|IN_PROGRESS|DONE)`
  - `completedChapters`

---

### Cursos Service


- **CourseEntity**
  - `id: UUID (PK)`
  - `title`, `description`, `status`
- **ModuleEntity**
  - `id: UUID (PK)`
  - `course_id (FK -> Course)`
  - `title`, `orderIndex`
- **ChapterEntity**
  - `id: UUID (PK)`
  - `module_id (FK -> Module)`
  - `title`, `orderIndex`, `durationMin`, `resourceUrl`

---

## Documentación de la API

Cada microservicio expone **Swagger UI** en su puerto:

- `http://localhost:8081/swagger-ui.html` → **Usuarios Service**
- `http://localhost:8082/swagger-ui.html` → **Cursos Service**

Desde ahí se puede navegar por todos los endpoints disponibles, probar requests y revisar los modelos DTO expuestos.

---

