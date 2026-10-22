package campusdesk.repository;

import campusdesk.entity.TicketFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketFeedbackRepository extends JpaRepository<TicketFeedback, Long> {

    Optional<TicketFeedback> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    @Query("SELECT AVG(f.puntuacion) FROM TicketFeedback f WHERE f.ticket.tecnico.id = :technicianId")
    Double findAverageRatingByTechnicianId(@Param("technicianId") Long technicianId);

    @Query("SELECT COUNT(f) FROM TicketFeedback f WHERE f.ticket.tecnico.id = :technicianId")
    Long countByTechnicianId(@Param("technicianId") Long technicianId);
}
