package campusdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponseDTO {

    private Long id;
    private Long ticketId;
    private Integer puntuacion;
    private String comentario;
    private Instant fechaCreacion;
}
