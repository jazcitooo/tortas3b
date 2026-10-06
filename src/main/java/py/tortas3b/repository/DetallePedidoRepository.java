package py.tortas3b.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.DetallePedido;

/**
 * Operaciones SQL para los ítems o detalles de cada pedido.
 */
@Repository
public class DetallePedidoRepository {

    private final JdbcTemplate jdbc;

    public DetallePedidoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<DetallePedido> rowMapper = (rs, rowNum) -> mapearDetalle(rs);

    private DetallePedido mapearDetalle(ResultSet rs) throws SQLException {
        DetallePedido d = new DetallePedido();
        d.setId(rs.getInt("id"));
        d.setPedidoId(rs.getInt("pedido_id"));
        d.setProductoId(rs.getInt("producto_id"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        d.setSubtotal(rs.getBigDecimal("subtotal"));
        try {
            d.setProductoNombre(rs.getString("producto_nombre"));
        } catch (SQLException ignored) {}
        return d;
    }

    public List<DetallePedido> listarPorPedido(Integer pedidoId) {
        String sql = "SELECT dp.*, pr.nombre AS producto_nombre " +
                     "FROM detalle_pedido dp " +
                     "JOIN productos pr ON dp.producto_id = pr.id " +
                     "WHERE dp.pedido_id = ? " +
                     "ORDER BY dp.id ASC";
        return jdbc.query(sql, rowMapper, pedidoId);
    }

    public void insertar(DetallePedido d) {
        String sql = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio_unitario, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, d.getPedidoId(), d.getProductoId(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
    }

    public void eliminarPorPedido(Integer pedidoId) {
        jdbc.update("DELETE FROM detalle_pedido WHERE pedido_id = ?", pedidoId);
    }
}
