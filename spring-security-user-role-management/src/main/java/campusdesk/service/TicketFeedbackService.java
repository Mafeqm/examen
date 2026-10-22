package campusdesk.service;

import campusdesk.dto.FeedbackResponseDTO;
import campusdesk.dto.TechnicianRatingResponseDTO;
import campusdesk.dto.TicketFeedbackDTO;

public interface TicketFeedbackService {

    FeedbackResponseDTO createFeedback(Long ticketId, TicketFeedbackDTO dto, String userEmail);

    TechnicianRatingResponseDTO getTechnicianRatingAverage(Long technicianId);
}
