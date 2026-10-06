package py.tortas3b.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.DetalleCompra;

/**
 * Operaciones SQL para los ítems de compras de reposición.
 */
@Repository
public class DetalleCompraRepository {

    private final JdbcTemplate jdbc;

    public DetalleCompraRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<DetalleCompra> rowMapper = (rs, rowNum) -> mapearDetalle(rs);

    private DetalleCompra mapearDetalle(ResultSet rs) throws SQLException {
        DetalleCompra d = new DetalleCompra();
        d.setId(rs.getInt("id"));
        d.setCompraId(rs.getInt("compra_id"));
        d.setIngredienteId(rs.getInt("ingrediente_id"));
        d.setCantidad(rs.getBigDecimal("cantidad"));
        d.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        d.setSubtotal(rs.getBigDecimal("subtotal"));
        try {
            d.setIngredienteNombre(rs.getString("ingrediente_nombre"));
            d.setUnidad(rs.getString("unidad"));
        } catch (SQLException ignored) {}
        return d;
    }

    public List<DetalleCompra> listarPorCompra(Integer compraId) {
        String sql = "SELECT dc.*, i.nombre AS ingrediente_nombre, i.unidad AS unidad " +
                     "FROM detalle_compra dc " +
                     "JOIN ingredientes i ON dc.ingrediente_id = i.id " +
                     "WHERE dc.compra_id = ? ORDER BY dc.id ASC";
        return jdbc.query(sql, rowMapper, compraId);
    }

    public void insertar(DetalleCompra d) {
        String sql = "INSERT INTO detalle_compra (compra_id, ingrediente_id, cantidad, costo_unitario, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, d.getCompraId(), d.getIngredienteId(), d.getCantidad(), d.getCostoUnitario(), d.getSubtotal());
    }

    public void eliminarPorCompra(Integer compraId) {
        jdbc.update("DELETE FROM detalle_compra WHERE compra_id = ?", compraId);
    }
}
