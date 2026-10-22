package campusdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianRatingResponseDTO {

    private Long tecnicoId;
    private Double promedio;
    private Long totalTicketsCalificados;

    public Double getPromedioCalificacion() {
        return this.promedio;
    }

    public Long getTotalCalificados() {
        return this.totalTicketsCalificados;
    }
}
