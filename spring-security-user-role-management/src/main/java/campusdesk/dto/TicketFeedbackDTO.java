package campusdesk.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFeedbackDTO {

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación debe ser como mínimo 1")
    @Max(value = 5, message = "La puntuación no puede ser mayor a 5")
    private Integer puntuacion;

    @Size(max = 500, message = "El comentario no puede exceder los 500 caracteres")
    private String comentario;
}
