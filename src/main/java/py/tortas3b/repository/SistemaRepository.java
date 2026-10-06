package py.tortas3b.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas generales para verificar que la base de datos está cargada.
 */
@Repository
public class SistemaRepository {

    // Los nombres de tabla no se pueden pasar como parámetro "?" en SQL,
    // por eso se usa una lista fija escrita en el código (nunca texto del usuario).
    private static final List<String> TABLAS = List.of(
            "usuarios", "clientes", "productos", "proveedores", "ingredientes",
            "pedidos", "detalle_pedido", "delivery", "ventas",
            "compras", "detalle_compra", "movimientos_stock");

    private final JdbcTemplate jdbc;

    public SistemaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Devuelve la cantidad de registros de cada tabla, en orden. */
    public Map<String, Integer> contarRegistros() {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (String tabla : TABLAS) {
            Integer cantidad = jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
            resultado.put(tabla, cantidad);
        }
        return resultado;
    }
}
