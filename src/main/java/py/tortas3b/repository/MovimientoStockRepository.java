package py.tortas3b.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.MovimientoStock;

/**
 * Operaciones SQL para la tabla movimientos_stock (auditoría de inventario).
 */
@Repository
public class MovimientoStockRepository {

    private final JdbcTemplate jdbc;

    public MovimientoStockRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<MovimientoStock> rowMapper = (rs, rowNum) -> mapearMovimiento(rs);

    private MovimientoStock mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoStock m = new MovimientoStock();
        m.setId(rs.getInt("id"));
        m.setIngredienteId(rs.getInt("ingrediente_id"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        int compId = rs.getInt("compra_id");
        if (!rs.wasNull()) {
            m.setCompraId(compId);
        }
        if (rs.getTimestamp("fecha") != null) {
            m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        }
        m.setTipo(rs.getString("tipo"));
        m.setCantidad(rs.getBigDecimal("cantidad"));
        m.setStockResultante(rs.getBigDecimal("stock_resultante"));
        m.setMotivo(rs.getString("motivo"));
        try {
            m.setIngredienteNombre(rs.getString("ingrediente_nombre"));
            m.setUsuarioNombre(rs.getString("usuario_nombre"));
        } catch (SQLException ignored) {}
        return m;
    }

    public List<MovimientoStock> listarUltimos(int limite) {
        String sql = "SELECT m.*, i.nombre AS ingrediente_nombre, u.nombre AS usuario_nombre "
                   + "FROM movimientos_stock m "
                   + "JOIN ingredientes i ON m.ingrediente_id = i.id "
                   + "JOIN usuarios u ON m.usuario_id = u.id "
                   + "ORDER BY m.fecha DESC, m.id DESC LIMIT ?";
        return jdbc.query(sql, rowMapper, limite);
    }

    public List<MovimientoStock> listarPorIngrediente(Integer ingredienteId) {
        String sql = "SELECT m.*, i.nombre AS ingrediente_nombre, u.nombre AS usuario_nombre "
                   + "FROM movimientos_stock m "
                   + "JOIN ingredientes i ON m.ingrediente_id = i.id "
                   + "JOIN usuarios u ON m.usuario_id = u.id "
                   + "WHERE m.ingrediente_id = ? "
                   + "ORDER BY m.fecha DESC, m.id DESC";
        return jdbc.query(sql, rowMapper, ingredienteId);
    }

    public void insertar(MovimientoStock m) {
        String sql = "INSERT INTO movimientos_stock (ingrediente_id, usuario_id, compra_id, tipo, cantidad, stock_resultante, motivo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, m.getIngredienteId(), m.getUsuarioId(), m.getCompraId(),
                m.getTipo(), m.getCantidad(), m.getStockResultante(), m.getMotivo());
    }
}
