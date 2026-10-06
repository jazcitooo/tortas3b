package py.tortas3b.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas analíticas para los reportes de entregas, ganancias y productos más vendidos.
 */
@Repository
public class ReporteRepository {

    private final JdbcTemplate jdbc;

    public ReporteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Las consultas usan SQL estándar (CAST, CURRENT_DATE, EXTRACT) para funcionar
    // igual en MySQL (local) y en PostgreSQL (Supabase).

    public List<Map<String, Object>> obtenerEntregas(LocalDate fechaDesde, LocalDate fechaHasta, String estado) {
        StringBuilder filtro = new StringBuilder("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fechaDesde != null) {
            filtro.append("AND p.fecha_entrega >= ? ");
            params.add(Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null) {
            filtro.append("AND p.fecha_entrega <= ? ");
            params.add(Date.valueOf(fechaHasta));
        }
        if (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado)) {
            filtro.append("AND p.estado = ? ");
            params.add(estado);
        }

        List<Map<String, Object>> entregas = jdbc.queryForList(
                "SELECT p.id, p.fecha_entrega, p.hora_entrega, c.nombre AS cliente_nombre, c.telefono AS cliente_telefono, " +
                "p.tipo_entrega, p.estado, p.total " +
                "FROM pedidos p JOIN clientes c ON p.cliente_id = c.id " +
                filtro + "ORDER BY p.fecha_entrega ASC, p.hora_entrega ASC",
                params.toArray());

        // Resumen "Producto (xN), ..." armado en Java (GROUP_CONCAT no existe en PostgreSQL)
        Map<Integer, StringBuilder> resumenes = new HashMap<>();
        jdbc.query(
                "SELECT dp.pedido_id, pr.nombre, dp.cantidad " +
                "FROM detalle_pedido dp " +
                "JOIN productos pr ON dp.producto_id = pr.id " +
                "JOIN pedidos p ON dp.pedido_id = p.id " +
                filtro + "ORDER BY dp.pedido_id, dp.id",
                rs -> {
                    StringBuilder sb = resumenes.computeIfAbsent(rs.getInt("pedido_id"), k -> new StringBuilder());
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString("nombre")).append(" (x").append(rs.getInt("cantidad")).append(')');
                },
                params.toArray());

        for (Map<String, Object> fila : entregas) {
            StringBuilder sb = resumenes.get(((Number) fila.get("id")).intValue());
            fila.put("resumen_productos", sb != null ? sb.toString() : null);
        }
        return entregas;
    }

    public List<Map<String, Object>> obtenerGananciasDiarias(LocalDate fechaDesde, LocalDate fechaHasta) {
        // Los kilos se suman por pedido en una subconsulta: así cada venta cuenta una sola vez
        // aunque su pedido tenga varios productos.
        StringBuilder sql = new StringBuilder(
                "SELECT CAST(v.fecha AS DATE) AS fecha, " +
                "COUNT(v.id) AS cantidad_ventas, " +
                "SUM(v.total) AS total_ingresos, " +
                "COALESCE(SUM(k.kilos), 0) AS total_kilos " +
                "FROM ventas v " +
                "LEFT JOIN (SELECT dp.pedido_id, SUM(dp.cantidad * pr.peso_kg) AS kilos " +
                "           FROM detalle_pedido dp JOIN productos pr ON dp.producto_id = pr.id " +
                "           GROUP BY dp.pedido_id) k ON k.pedido_id = v.pedido_id " +
                "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fechaDesde != null) {
            sql.append("AND CAST(v.fecha AS DATE) >= ? ");
            params.add(Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null) {
            sql.append("AND CAST(v.fecha AS DATE) <= ? ");
            params.add(Date.valueOf(fechaHasta));
        }

        sql.append("GROUP BY CAST(v.fecha AS DATE) ORDER BY 1 DESC");
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> obtenerProductosMasVendidos(LocalDate fechaDesde, LocalDate fechaHasta, int limite) {
        StringBuilder sql = new StringBuilder(
                "SELECT pr.nombre, pr.categoria, " +
                "SUM(dp.cantidad) AS cantidad_vendida, " +
                "SUM(dp.subtotal) AS total_recaudado " +
                "FROM detalle_pedido dp " +
                "JOIN productos pr ON dp.producto_id = pr.id " +
                "JOIN pedidos p ON dp.pedido_id = p.id " +
                "JOIN ventas v ON p.id = v.pedido_id " +
                "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fechaDesde != null) {
            sql.append("AND CAST(v.fecha AS DATE) >= ? ");
            params.add(Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null) {
            sql.append("AND CAST(v.fecha AS DATE) <= ? ");
            params.add(Date.valueOf(fechaHasta));
        }

        sql.append("GROUP BY pr.id, pr.nombre, pr.categoria " +
                   "ORDER BY cantidad_vendida DESC LIMIT ?");
        params.add(limite);

        return jdbc.queryForList(sql.toString(), params.toArray());
    }
}
