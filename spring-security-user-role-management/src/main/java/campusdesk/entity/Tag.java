package campusdesk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Tag para categorización dinámica de incidencias (Ejercicio 1 de Examen).
 * Los nombres de etiquetas se almacenan únicos y normalizados en mayúsculas.
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "tickets")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    @Size(max = 50, message = "El nombre de la etiqueta no puede superar 50 caracteres")
    @EqualsAndHashCode.Include
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    @Builder.Default
    private Set<Ticket> tickets = new HashSet<>();

    public void setName(String name) {
        this.name = (name != null) ? name.trim().toUpperCase() : null;
    }

    @PrePersist
    @PreUpdate
    protected void normalizeName() {
        if (this.name != null) {
            this.name = this.name.trim().toUpperCase();
        }
    }
}
