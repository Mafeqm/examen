package campusdesk.repository;

import campusdesk.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para operaciones sobre etiquetas (Tag).
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);
}
