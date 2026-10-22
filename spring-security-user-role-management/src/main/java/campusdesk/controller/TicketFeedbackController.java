package campusdesk.controller;

import campusdesk.dto.FeedbackResponseDTO;
import campusdesk.dto.TechnicianRatingResponseDTO;
import campusdesk.dto.TicketFeedbackDTO;
import campusdesk.service.TicketFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedback", description = "Calificaciones y encuestas de satisfacción CSAT")
@SecurityRequirement(name = "bearerAuth")
public class TicketFeedbackController {

    private final TicketFeedbackService ticketFeedbackService;

    @Operation(summary = "Calificar la atención de un ticket", description = "Solo permitido para el USER creador del ticket en estado CERRADA")
    @PostMapping({"/api/tickets/{id}/feedback", "/api/ticket/{id}/feedback", "/tickets/{id}/feedback", "/ticket/{id}/feedback"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FeedbackResponseDTO> createFeedback(
            @PathVariable Long id,
            @Valid @RequestBody TicketFeedbackDTO dto,
            Principal principal
    ) {
        log.info("Petición para calificar ticket #{} recibida de [{}]", id, principal.getName());
        FeedbackResponseDTO response = ticketFeedbackService.createFeedback(id, dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener promedio de calificación de un técnico", description = "Accesible exclusivamente por administradores")
    @GetMapping({"/api/technicians/{id}/promedio-calificacion", "/api/technician/{id}/promedio-calificacion", "/technicians/{id}/promedio-calificacion", "/technician/{id}/promedio-calificacion"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechnicianRatingResponseDTO> getTechnicianRatingAverage(
            @PathVariable Long id
    ) {
        log.info("Consulta de promedio de calificación para el técnico #{}", id);
        TechnicianRatingResponseDTO response = ticketFeedbackService.getTechnicianRatingAverage(id);
        return ResponseEntity.ok(response);
    }
}
