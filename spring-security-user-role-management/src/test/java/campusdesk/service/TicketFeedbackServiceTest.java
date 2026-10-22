package campusdesk.service;

import campusdesk.dto.FeedbackResponseDTO;
import campusdesk.dto.TechnicianRatingResponseDTO;
import campusdesk.dto.TicketFeedbackDTO;
import campusdesk.entity.Role;
import campusdesk.entity.Ticket;
import campusdesk.entity.TicketCategory;
import campusdesk.entity.TicketFeedback;
import campusdesk.entity.TicketPriority;
import campusdesk.entity.TicketStatus;
import campusdesk.entity.User;
import campusdesk.exception.FeedbackAlreadyExistsException;
import campusdesk.exception.InvalidStateTransitionException;
import campusdesk.exception.UnauthorizedTicketActionException;
import campusdesk.repository.TicketFeedbackRepository;
import campusdesk.repository.TicketRepository;
import campusdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketFeedbackServiceTest {

    @Mock
    private TicketFeedbackRepository ticketFeedbackRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketFeedbackServiceImpl ticketFeedbackService;

    private User requester;
    private User otherUser;
    private User technician;
    private User admin;
    private Ticket ticketCerrada;
    private Ticket ticketAbierta;

    @BeforeEach
    void setUp() {
        requester = User.builder().id(1L).email("user@campusdesk.com").name("Carlos User").role(Role.USER).build();
        otherUser = User.builder().id(2L).email("other@campusdesk.com").name("Otro User").role(Role.USER).build();
        technician = User.builder().id(3L).email("tech@campusdesk.com").name("Ana Tech").role(Role.TECHNICIAN).build();
        admin = User.builder().id(4L).email("admin@campusdesk.com").name("Admin").role(Role.ADMIN).build();

        ticketCerrada = Ticket.builder()
                .id(100L)
                .title("Problema con proyector")
                .description("No enciende el proyector")
                .solicitante(requester)
                .tecnico(technician)
                .status(TicketStatus.CERRADA)
                .priority(TicketPriority.MEDIA)
                .category(TicketCategory.HARDWARE)
                .build();

        ticketAbierta = Ticket.builder()
                .id(101L)
                .title("Falla de red")
                .description("Sin acceso a WiFi")
                .solicitante(requester)
                .tecnico(technician)
                .status(TicketStatus.ABIERTA)
                .priority(TicketPriority.ALTA)
                .category(TicketCategory.REDES)
                .build();
    }

    @Test
    @DisplayName("Calificar ticket cerrado exitosamente por el usuario solicitante")
    void testCreateFeedbackSuccess() {
        TicketFeedbackDTO dto = TicketFeedbackDTO.builder()
                .puntuacion(5)
                .comentario("Excelente atención y rapidez")
                .build();

        TicketFeedback saved = TicketFeedback.builder()
                .id(1L)
                .ticket(ticketCerrada)
                .puntuacion(5)
                .comentario("Excelente atención y rapidez")
                .fechaCreacion(Instant.now())
                .build();

        when(userRepository.findByEmail(requester.getEmail())).thenReturn(Optional.of(requester));
        when(ticketRepository.findById(ticketCerrada.getId())).thenReturn(Optional.of(ticketCerrada));
        when(ticketFeedbackRepository.existsByTicketId(ticketCerrada.getId())).thenReturn(false);
        when(ticketFeedbackRepository.save(any(TicketFeedback.class))).thenReturn(saved);

        FeedbackResponseDTO result = ticketFeedbackService.createFeedback(ticketCerrada.getId(), dto, requester.getEmail());

        assertNotNull(result);
        assertEquals(5, result.getPuntuacion());
        assertEquals("Excelente atención y rapidez", result.getComentario());
        assertEquals(ticketCerrada.getId(), result.getTicketId());
        verify(ticketFeedbackRepository).save(any(TicketFeedback.class));
    }

    @Test
    @DisplayName("Rechazar calificación si el usuario no es el creador del ticket")
    void testCreateFeedbackNonCreatorFails() {
        TicketFeedbackDTO dto = TicketFeedbackDTO.builder().puntuacion(4).build();

        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(ticketRepository.findById(ticketCerrada.getId())).thenReturn(Optional.of(ticketCerrada));

        assertThrows(UnauthorizedTicketActionException.class, () ->
                ticketFeedbackService.createFeedback(ticketCerrada.getId(), dto, otherUser.getEmail()));
    }

    @Test
    @DisplayName("Rechazar calificación si el usuario no posee rol USER")
    void testCreateFeedbackNonRoleUserFails() {
        TicketFeedbackDTO dto = TicketFeedbackDTO.builder().puntuacion(4).build();

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(ticketCerrada.getId())).thenReturn(Optional.of(ticketCerrada));

        assertThrows(UnauthorizedTicketActionException.class, () ->
                ticketFeedbackService.createFeedback(ticketCerrada.getId(), dto, admin.getEmail()));
    }

    @Test
    @DisplayName("Rechazar calificación si el estado del ticket no es estrictamente CERRADA")
    void testCreateFeedbackTicketNotClosedFails() {
        TicketFeedbackDTO dto = TicketFeedbackDTO.builder().puntuacion(5).build();

        when(userRepository.findByEmail(requester.getEmail())).thenReturn(Optional.of(requester));
        when(ticketRepository.findById(ticketAbierta.getId())).thenReturn(Optional.of(ticketAbierta));

        assertThrows(InvalidStateTransitionException.class, () ->
                ticketFeedbackService.createFeedback(ticketAbierta.getId(), dto, requester.getEmail()));
    }

    @Test
    @DisplayName("Rechazar calificación si el ticket ya fue calificado previamente")
    void testCreateFeedbackAlreadyExistsFails() {
        TicketFeedbackDTO dto = TicketFeedbackDTO.builder().puntuacion(4).build();

        when(userRepository.findByEmail(requester.getEmail())).thenReturn(Optional.of(requester));
        when(ticketRepository.findById(ticketCerrada.getId())).thenReturn(Optional.of(ticketCerrada));
        when(ticketFeedbackRepository.existsByTicketId(ticketCerrada.getId())).thenReturn(true);

        assertThrows(FeedbackAlreadyExistsException.class, () ->
                ticketFeedbackService.createFeedback(ticketCerrada.getId(), dto, requester.getEmail()));
    }

    @Test
    @DisplayName("Calcular promedio y total de calificaciones para un técnico")
    void testGetTechnicianRatingAverageSuccess() {
        when(userRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(ticketFeedbackRepository.findAverageRatingByTechnicianId(technician.getId())).thenReturn(4.5);
        when(ticketFeedbackRepository.countByTechnicianId(technician.getId())).thenReturn(6L);

        TechnicianRatingResponseDTO response = ticketFeedbackService.getTechnicianRatingAverage(technician.getId());

        assertNotNull(response);
        assertEquals(technician.getId(), response.getTecnicoId());
        assertEquals(4.5, response.getPromedio());
        assertEquals(6L, response.getTotalTicketsCalificados());
    }

    @Test
    @DisplayName("Retornar promedio 0.0 y total 0 cuando el técnico no tiene calificaciones")
    void testGetTechnicianRatingAverageNoRatings() {
        when(userRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(ticketFeedbackRepository.findAverageRatingByTechnicianId(technician.getId())).thenReturn(null);
        when(ticketFeedbackRepository.countByTechnicianId(technician.getId())).thenReturn(0L);

        TechnicianRatingResponseDTO response = ticketFeedbackService.getTechnicianRatingAverage(technician.getId());

        assertNotNull(response);
        assertEquals(0.0, response.getPromedio());
        assertEquals(0L, response.getTotalTicketsCalificados());
    }

    @Test
    @DisplayName("Lanzar excepción al consultar promedio si el usuario no existe o no es técnico")
    void testGetTechnicianRatingAverageInvalidTechnician() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));

        assertThrows(IllegalArgumentException.class, () ->
                ticketFeedbackService.getTechnicianRatingAverage(requester.getId()));
    }
}
