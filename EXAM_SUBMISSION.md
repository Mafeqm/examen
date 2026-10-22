# EXAM SUBMISSION: Sistema de Calificación y Encuesta de Satisfacción (CSAT)

## Descripción y Objetivo
Implementación del módulo de calificaciones y encuesta de satisfacción (CSAT) para tickets cerrados en CAMPUSDESK. El sistema permite al usuario creador del ticket evaluar la atención recibida una vez concluido el ciclo de vida del ticket, y a los administradores consultar el promedio y total de calificaciones asignadas a los técnicos.

---

## Endpoints Implementados

### 1. Registrar Calificación de Ticket
- **Método y Ruta**: `POST /api/tickets/{id}/feedback`
- **Seguridad**: Requiere autenticación con rol `USER`. Solo el usuario creador (solicitante) del ticket puede calificar.
- **Reglas de Negocio**:
  - El ticket debe encontrarse estrictamente en estado `CERRADA`. Si se encuentra en otro estado, responde `409 CONFLICT`.
  - Si un usuario distinto al solicitante intenta calificar, responde `403 FORBIDDEN`.
  - No se permite calificar un ticket más de una vez. Si ya existe calificación, responde `409 CONFLICT`.
  - La puntuación es obligatoria y debe ser un número entero entre 1 y 5 (validado con `@Min(1)` y `@Max(5)`). Si no cumple, responde `400 BAD REQUEST`.
  - El comentario es opcional con un límite máximo de 500 caracteres (`@Size(max = 500)`).
- **Ejemplo de Request Body**:
  ```json
  {
    "puntuacion": 5,
    "comentario": "Excelente soporte y atención rápida."
  }
  ```
- **Respuesta Exitosa (`201 CREATED`)**:
  ```json
  {
    "id": 1,
    "ticketId": 10,
    "puntuacion": 5,
    "comentario": "Excelente soporte y atención rápida.",
    "fechaCreacion": "2026-10-22T17:10:00Z"
  }
  ```

---

### 2. Consultar Promedio de Calificación de un Técnico
- **Método y Ruta**: `GET /api/technicians/{id}/promedio-calificacion`
- **Seguridad**: Exclusivo para administradores (`ROLE_ADMIN`). Usuarios con otros roles o sin autenticación reciben `403 FORBIDDEN`.
- **Reglas de Negocio**:
  - Valida que el ID pertenezca a un usuario registrado con rol `TECHNICIAN`. Si no existe o no es técnico, responde `400 BAD REQUEST`.
  - Calcula el promedio de calificaciones como `Double` y el conteo total de tickets calificados.
  - Si el técnico no registra tickets calificados, retorna promedio `0.0` y total `0`.
- **Respuesta Exitosa (`200 OK`)**:
  ```json
  {
    "tecnicoId": 2,
    "promedio": 4.75,
    "totalTicketsCalificados": 8,
    "promedioCalificacion": 4.75,
    "totalCalificados": 8
  }
  ```

---

## Archivos Creados y Modificados

### Archivos Creados
1. `spring-security-user-role-management/src/main/java/campusdesk/entity/TicketFeedback.java`: Entidad JPA con atributos `id`, `puntuacion` (1 a 5), `comentario` (máx. 500), `fechaCreacion` y relación `@OneToOne` referenciando `Ticket` con restricción de unicidad (`unique = true`).
2. `spring-security-user-role-management/src/main/java/campusdesk/repository/TicketFeedbackRepository.java`: Repositorio Spring Data JPA con métodos de consulta para validación de existencia (`existsByTicketId`), cálculo del promedio (`AVG`) y conteo (`COUNT`) por ID de técnico.
3. `spring-security-user-role-management/src/main/java/campusdesk/dto/TicketFeedbackDTO.java`: DTO de entrada con validaciones `@NotNull`, `@Min(1)`, `@Max(5)` y `@Size(max = 500)`.
4. `spring-security-user-role-management/src/main/java/campusdesk/dto/FeedbackResponseDTO.java`: DTO de salida para la confirmación de la calificación registrada.
5. `spring-security-user-role-management/src/main/java/campusdesk/dto/TechnicianRatingResponseDTO.java`: DTO de salida con el promedio (`Double`) y total de calificaciones del técnico.
6. `spring-security-user-role-management/src/main/java/campusdesk/exception/FeedbackAlreadyExistsException.java`: Excepción de negocio para evitar duplicidad de calificaciones.
7. `spring-security-user-role-management/src/main/java/campusdesk/service/TicketFeedbackService.java`: Interfaz del servicio de feedback.
8. `spring-security-user-role-management/src/main/java/campusdesk/service/TicketFeedbackServiceImpl.java`: Implementación de la lógica de negocio, validación de estado `CERRADA`, propiedad del ticket y cálculos estadísticos.
9. `spring-security-user-role-management/src/main/java/campusdesk/controller/TicketFeedbackController.java`: Controlador REST para los endpoints de feedback y promedio con anotaciones `@PreAuthorize`.
10. `spring-security-user-role-management/src/test/java/campusdesk/service/TicketFeedbackServiceTest.java`: Pruebas unitarias de la lógica de negocio y reglas de validación.
11. `spring-security-user-role-management/src/test/java/campusdesk/controller/TicketFeedbackControllerTest.java`: Pruebas de integración MockMvc para validar códigos de respuesta HTTP y Bean Validation.
12. `EXAM_SUBMISSION.md`: Documento de entrega en la raíz del proyecto.

### Archivos Modificados
1. `spring-security-user-role-management/src/main/java/campusdesk/entity/Ticket.java`: Mapeo bidireccional `@OneToOne(mappedBy = "ticket")` hacia `TicketFeedback` y exclusión en `@ToString`.
2. `spring-security-user-role-management/src/main/java/campusdesk/exception/ApiExceptionHandler.java`: Manejador `@ExceptionHandler` para `FeedbackAlreadyExistsException` retornando HTTP 409 con el formato estándar `ApiErrorResponse`.
3. `spring-security-user-role-management/src/main/java/campusdesk/security/SecurityConfig.java`: Ajuste de compatibilidad de orígenes explícitos en la configuración CORS.

---

## Verificación y Pruebas
Para ejecutar las pruebas del proyecto:
```bash
./gradlew test
```
Todas las pruebas unitarias y de integración ejecutan con resultado exitoso (100% aprobadas).
