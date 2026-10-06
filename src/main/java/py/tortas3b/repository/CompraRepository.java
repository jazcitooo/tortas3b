package py.tortas3b.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Compra;

/**
 * Operaciones SQL para pedidos de reposición / compras a proveedores.
 */
@Repository
public class CompraRepository {

    private final JdbcTemplate jdbc;

    public CompraRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Compra> rowMapper = (rs, rowNum) -> mapearCompra(rs);

    private Compra mapearCompra(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setId(rs.getInt("id"));
        c.setProveedorId(rs.getInt("proveedor_id"));
        c.setUsuarioId(rs.getInt("usuario_id"));
        if (rs.getTimestamp("fecha") != null) {
            c.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        }
        if (rs.getTimestamp("fecha_recepcion") != null) {
            c.setFechaRecepcion(rs.getTimestamp("fecha_recepcion").toLocalDateTime());
        }
        c.setEstado(rs.getString("estado"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setObservaciones(rs.getString("observaciones"));
        try {
            c.setProveedorNombre(rs.getString("proveedor_nombre"));
        } catch (SQLException ignored) {}
        return c;
    }

    public List<Compra> listar(String estado) {
        if (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado)) {
            String sql = "SELECT c.*, p.nombre AS proveedor_nombre FROM compras c " +
                         "JOIN proveedores p ON c.proveedor_id = p.id " +
                         "WHERE c.estado = ? ORDER BY c.fecha DESC, c.id DESC";
            return jdbc.query(sql, rowMapper, estado);
        }
        String sql = "SELECT c.*, p.nombre AS proveedor_nombre FROM compras c " +
                     "JOIN proveedores p ON c.proveedor_id = p.id " +
                     "ORDER BY c.fecha DESC, c.id DESC";
        return jdbc.query(sql, rowMapper);
    }

    public Optional<Compra> findById(Integer id) {
        String sql = "SELECT c.*, p.nombre AS proveedor_nombre FROM compras c " +
                     "JOIN proveedores p ON c.proveedor_id = p.id WHERE c.id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer insertar(Compra c) {
        String sql = "INSERT INTO compras (proveedor_id, usuario_id, estado, total, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setInt(1, c.getProveedorId());
            ps.setInt(2, c.getUsuarioId());
            ps.setString(3, c.getEstado());
            ps.setBigDecimal(4, c.getTotal());
            ps.setString(5, c.getObservaciones());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            c.setId(keyHolder.getKey().intValue());
            return c.getId();
        }
        return null;
    }

    public void actualizarEstado(Integer id, String estado, LocalDateTime fechaRecepcion) {
        String sql = "UPDATE compras SET estado = ?, fecha_recepcion = ? WHERE id = ?";
        jdbc.update(sql, estado, fechaRecepcion != null ? Timestamp.valueOf(fechaRecepcion) : null, id);
    }

    public void actualizarTotal(Integer id, BigDecimal total) {
        jdbc.update("UPDATE compras SET total = ? WHERE id = ?", total, id);
    }
}
