package campusdesk.config;

import campusdesk.entity.Role;
import campusdesk.entity.User;
import campusdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador automático de datos base (Cuentas predeterminadas).
 * Asegura que al levantar la aplicación con ddl-auto=update existan las credenciales
 * requeridas para el acceso de Administrador, Técnico y Usuario.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Inicializando cuentas de usuario predeterminadas en CAMPUSDESK...");

            User admin = User.builder()
                    .name("Administrador del Sistema")
                    .email("superadmin@campusdesk.com")
                    .password(passwordEncoder.encode("SuperAdmin123!"))
                    .role(Role.ADMIN)
                    .build();

            User tech = User.builder()
                    .name("Técnico de Soporte")
                    .email("tecnico@campusdesk.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role(Role.TECHNICIAN)
                    .build();

            User user = User.builder()
                    .name("Carlos Solicitante")
                    .email("carlos@campusdesk.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role(Role.USER)
                    .build();

            userRepository.save(admin);
            userRepository.save(tech);
            userRepository.save(user);

            log.info("Cuentas inicializadas exitosamente: superadmin, tecnico, carlos.");
        }
    }
}
