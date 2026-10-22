package campusdesk.service;

import campusdesk.dto.FeedbackResponseDTO;
import campusdesk.dto.TechnicianRatingResponseDTO;
import campusdesk.dto.TicketFeedbackDTO;
import campusdesk.entity.Role;
import campusdesk.entity.Ticket;
import campusdesk.entity.TicketFeedback;
import campusdesk.entity.TicketStatus;
import campusdesk.entity.User;
import campusdesk.exception.FeedbackAlreadyExistsException;
import campusdesk.exception.InvalidStateTransitionException;
import campusdesk.exception.TicketNotFoundException;
import campusdesk.exception.UnauthorizedTicketActionException;
import campusdesk.repository.TicketFeedbackRepository;
import campusdesk.repository.TicketRepository;
import campusdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketFeedbackServiceImpl implements TicketFeedbackService {

    private final TicketFeedbackRepository ticketFeedbackRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(Long ticketId, TicketFeedbackDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con correo: " + userEmail));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Incidencia no encontrada con ID: " + ticketId));

        if (user.getRole() != Role.USER || ticket.getSolicitante() == null || !ticket.getSolicitante().getId().equals(user.getId())) {
            throw new UnauthorizedTicketActionException("Solo el usuario creador del ticket puede calificar la atención");
        }

        if (ticket.getStatus() != TicketStatus.CERRADA) {
            throw new InvalidStateTransitionException("Solo se permite calificar un ticket cuando su estado es estrictamente CERRADA");
        }

        if (ticketFeedbackRepository.existsByTicketId(ticketId)) {
            throw new FeedbackAlreadyExistsException("El ticket ya ha sido calificado previamente");
        }

        TicketFeedback feedback = TicketFeedback.builder()
                .ticket(ticket)
                .puntuacion(dto.getPuntuacion())
                .comentario(dto.getComentario())
                .build();

        TicketFeedback savedFeedback = ticketFeedbackRepository.save(feedback);
        log.info("Ticket #{} calificado exitosamente con puntuación {} por [{}]", ticketId, savedFeedback.getPuntuacion(), userEmail);

        return FeedbackResponseDTO.builder()
                .id(savedFeedback.getId())
                .ticketId(ticket.getId())
                .puntuacion(savedFeedback.getPuntuacion())
                .comentario(savedFeedback.getComentario())
                .fechaCreacion(savedFeedback.getFechaCreacion())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianRatingResponseDTO getTechnicianRatingAverage(Long technicianId) {
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("El técnico especificado no existe con ID: " + technicianId));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new IllegalArgumentException("El usuario seleccionado no posee el rol TECHNICIAN");
        }

        Double promedio = ticketFeedbackRepository.findAverageRatingByTechnicianId(technicianId);
        Long total = ticketFeedbackRepository.countByTechnicianId(technicianId);

        return TechnicianRatingResponseDTO.builder()
                .tecnicoId(technicianId)
                .promedio(promedio != null ? promedio : 0.0)
                .totalTicketsCalificados(total != null ? total : 0L)
                .build();
    }
}
