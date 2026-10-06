package py.tortas3b;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del sistema. Al ejecutarlo se levanta un servidor web
 * en http://localhost:8080
 */
@SpringBootApplication
public class Tortas3bApplication {

    public static void main(String[] args) {
        // Zona horaria de Paraguay: así "hoy" y las horas de entrega son correctas.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Asuncion"));
        SpringApplication.run(Tortas3bApplication.class, args);
    }
}
