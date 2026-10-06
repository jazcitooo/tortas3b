package py.tortas3b.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ruta pública /health para UptimeRobot. Cada consulta toca la base de datos:
 * así Render no duerme el servidor y Supabase no pausa el proyecto por inactividad.
 */
@RestController
public class SaludController {

    private final JdbcTemplate jdbc;

    public SaludController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/health")
    public ResponseEntity<String> salud() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(503).body("sin base de datos");
        }
    }
}
