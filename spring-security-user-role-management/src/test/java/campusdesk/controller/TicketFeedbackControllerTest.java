package campusdesk.controller;

import campusdesk.dto.FeedbackResponseDTO;
import campusdesk.dto.TechnicianRatingResponseDTO;
import campusdesk.dto.TicketFeedbackDTO;
import campusdesk.exception.ApiExceptionHandler;
import campusdesk.exception.FeedbackAlreadyExistsException;
import campusdesk.exception.InvalidStateTransitionException;
import campusdesk.exception.UnauthorizedTicketActionException;
import campusdesk.service.TicketFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TicketFeedbackControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TicketFeedbackService ticketFeedbackService;

    @InjectMocks
    private TicketFeedbackController ticketFeedbackController;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ticketFeedbackController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        principal = () -> "user@campusdesk.com";
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback con datos válidos retorna 201 CREATED")
    void testCreateFeedbackSuccess() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .puntuacion(4)
                .comentario("Buena atención")
                .build();

        FeedbackResponseDTO responseDto = FeedbackResponseDTO.builder()
                .id(1L)
                .ticketId(10L)
                .puntuacion(4)
                .comentario("Buena atención")
                .fechaCreacion(Instant.now())
                .build();

        when(ticketFeedbackService.createFeedback(eq(10L), any(TicketFeedbackDTO.class), eq("user@campusdesk.com")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.puntuacion").value(4))
                .andExpect(jsonPath("$.ticketId").value(10L))
                .andExpect(jsonPath("$.comentario").value("Buena atención"));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback con puntuación menor a 1 retorna 400 BAD REQUEST (@Min)")
    void testCreateFeedbackMinPuntuacionViolation() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .puntuacion(0)
                .comentario("Pésimo")
                .build();

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback con puntuación mayor a 5 retorna 400 BAD REQUEST (@Max)")
    void testCreateFeedbackMaxPuntuacionViolation() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .puntuacion(6)
                .comentario("Increíble")
                .build();

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback con puntuación nula retorna 400 BAD REQUEST (@NotNull)")
    void testCreateFeedbackNullPuntuacionViolation() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .comentario("Sin puntuación")
                .build();

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback en ticket ya calificado retorna 409 CONFLICT")
    void testCreateFeedbackAlreadyExistsReturnsConflict() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .puntuacion(5)
                .build();

        when(ticketFeedbackService.createFeedback(eq(10L), any(TicketFeedbackDTO.class), eq("user@campusdesk.com")))
                .thenThrow(new FeedbackAlreadyExistsException("El ticket ya ha sido calificado previamente"));

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El ticket ya ha sido calificado previamente"));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback en ticket no cerrado retorna 409 CONFLICT")
    void testCreateFeedbackTicketNotClosedReturnsConflict() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .puntuacion(4)
                .build();

        when(ticketFeedbackService.createFeedback(eq(10L), any(TicketFeedbackDTO.class), eq("user@campusdesk.com")))
                .thenThrow(new InvalidStateTransitionException("Solo se permite calificar un ticket cuando su estado es estrictamente CERRADA"));

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/feedback por usuario no creador retorna 403 FORBIDDEN")
    void testCreateFeedbackUnauthorizedReturnsForbidden() throws Exception {
        TicketFeedbackDTO requestDto = TicketFeedbackDTO.builder()
                .puntuacion(4)
                .build();

        when(ticketFeedbackService.createFeedback(eq(10L), any(TicketFeedbackDTO.class), eq("user@campusdesk.com")))
                .thenThrow(new UnauthorizedTicketActionException("Solo el usuario creador del ticket puede calificar la atención"));

        mockMvc.perform(post("/api/tickets/10/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/technicians/{id}/promedio-calificacion retorna 200 OK con promedio y total")
    void testGetTechnicianRatingAverageSuccess() throws Exception {
        TechnicianRatingResponseDTO responseDto = TechnicianRatingResponseDTO.builder()
                .tecnicoId(2L)
                .promedio(4.75)
                .totalTicketsCalificados(8L)
                .build();

        when(ticketFeedbackService.getTechnicianRatingAverage(2L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/technicians/2/promedio-calificacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoId").value(2L))
                .andExpect(jsonPath("$.promedio").value(4.75))
                .andExpect(jsonPath("$.totalTicketsCalificados").value(8L));
    }
}
