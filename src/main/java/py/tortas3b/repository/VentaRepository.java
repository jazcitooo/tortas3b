package py.tortas3b.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Venta;

/**
 * Operaciones SQL para ventas y arqueo de caja.
 */
@Repository
public class VentaRepository {

    private final JdbcTemplate jdbc;

    public VentaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Venta> rowMapper = (rs, rowNum) -> mapearVenta(rs);

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getInt("id"));
        v.setPedidoId(rs.getInt("pedido_id"));
        v.setUsuarioId(rs.getInt("usuario_id"));
        if (rs.getTimestamp("fecha") != null) {
            v.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        }
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setTotal(rs.getBigDecimal("total"));
        try {
            v.setClienteNombre(rs.getString("cliente_nombre"));
        } catch (SQLException ignored) {}
        return v;
    }

    public List<Venta> listar(LocalDate fechaDesde, LocalDate fechaHasta) {
        StringBuilder sql = new StringBuilder(
                "SELECT v.*, c.nombre AS cliente_nombre " +
                "FROM ventas v " +
                "JOIN pedidos p ON v.pedido_id = p.id " +
                "JOIN clientes c ON p.cliente_id = c.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fechaDesde != null) {
            sql.append("AND CAST(v.fecha AS DATE) >= ? ");
            params.add(Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null) {
            sql.append("AND CAST(v.fecha AS DATE) <= ? ");
            params.add(Date.valueOf(fechaHasta));
        }

        sql.append("ORDER BY v.fecha DESC, v.id DESC");
        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Venta> findById(Integer id) {
        String sql = "SELECT v.*, c.nombre AS cliente_nombre " +
                     "FROM ventas v " +
                     "JOIN pedidos p ON v.pedido_id = p.id " +
                     "JOIN clientes c ON p.cliente_id = c.id WHERE v.id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Venta> findByPedidoId(Integer pedidoId) {
        String sql = "SELECT v.*, c.nombre AS cliente_nombre " +
                     "FROM ventas v " +
                     "JOIN pedidos p ON v.pedido_id = p.id " +
                     "JOIN clientes c ON p.cliente_id = c.id WHERE v.pedido_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, pedidoId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void insertar(Venta v) {
        String sql = "INSERT INTO ventas (pedido_id, usuario_id, metodo_pago, total) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, v.getPedidoId(), v.getUsuarioId(), v.getMetodoPago(), v.getTotal());
    }

    public void eliminar(Integer id) {
        jdbc.update("DELETE FROM ventas WHERE id = ?", id);
    }

    public BigDecimal obtenerTotalVentasHoy() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE CAST(fecha AS DATE) = CURRENT_DATE";
        return jdbc.queryForObject(sql, BigDecimal.class);
    }
}
