package py.tortas3b.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security con autenticación basada en formulario,
 * cifrado BCrypt y control estricto de roles (ADMIN vs OPERADOR).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos públicos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Páginas públicas
                .requestMatchers("/login", "/error", "/health").permitAll()
                // Rutas exclusivas para Administradores
                .requestMatchers("/admin/**", "/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/ingredientes/**", "/proveedores/**", "/compras/**").hasRole("ADMIN")
                .requestMatchers("/ventas/anular/**", "/clientes/desactivar/**").hasRole("ADMIN")
                .requestMatchers("/reportes/ganancias", "/reportes/productos-mas-vendidos").hasRole("ADMIN")
                // Todas las demás rutas requieren autenticación (ADMIN u OPERADOR)
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            );

        return http.build();
    }
}
