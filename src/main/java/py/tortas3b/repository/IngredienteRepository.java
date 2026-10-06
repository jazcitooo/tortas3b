package py.tortas3b.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Ingrediente;

/**
 * Operaciones SQL para la tabla ingredientes e inventario.
 */
@Repository
public class IngredienteRepository {

    private final JdbcTemplate jdbc;

    public IngredienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Ingrediente> rowMapper = (rs, rowNum) -> mapearIngrediente(rs);

    private Ingrediente mapearIngrediente(ResultSet rs) throws SQLException {
        Ingrediente i = new Ingrediente();
        i.setId(rs.getInt("id"));
        i.setNombre(rs.getString("nombre"));
        i.setUnidad(rs.getString("unidad"));
        i.setStockActual(rs.getBigDecimal("stock_actual"));
        i.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        int provId = rs.getInt("proveedor_id");
        if (!rs.wasNull()) {
            i.setProveedorId(provId);
        }
        i.setActivo(rs.getBoolean("activo"));
        try {
            i.setProveedorNombre(rs.getString("proveedor_nombre"));
        } catch (SQLException ignored) {}
        return i;
    }

    public List<Ingrediente> listarTodos() {
        String sql = "SELECT i.*, p.nombre AS proveedor_nombre "
                   + "FROM ingredientes i "
                   + "LEFT JOIN proveedores p ON i.proveedor_id = p.id "
                   + "ORDER BY i.nombre ASC";
        return jdbc.query(sql, rowMapper);
    }

    public List<Ingrediente> listarActivos() {
        String sql = "SELECT i.*, p.nombre AS proveedor_nombre "
                   + "FROM ingredientes i "
                   + "LEFT JOIN proveedores p ON i.proveedor_id = p.id "
                   + "WHERE i.activo = 1 "
                   + "ORDER BY i.nombre ASC";
        return jdbc.query(sql, rowMapper);
    }

    public List<Ingrediente> listarConStockBajo() {
        String sql = "SELECT i.*, p.nombre AS proveedor_nombre "
                   + "FROM ingredientes i "
                   + "LEFT JOIN proveedores p ON i.proveedor_id = p.id "
                   + "WHERE i.activo = 1 AND i.stock_actual <= i.stock_minimo "
                   + "ORDER BY (i.stock_actual / NULLIF(i.stock_minimo, 0)) ASC, i.nombre ASC";
        return jdbc.query(sql, rowMapper);
    }

    public int contarStockBajo() {
        String sql = "SELECT COUNT(*) FROM ingredientes WHERE activo = 1 AND stock_actual <= stock_minimo";
        Integer count = jdbc.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public Optional<Ingrediente> findById(Integer id) {
        String sql = "SELECT i.*, p.nombre AS proveedor_nombre "
                   + "FROM ingredientes i "
                   + "LEFT JOIN proveedores p ON i.proveedor_id = p.id "
                   + "WHERE i.id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void insertar(Ingrediente i) {
        String sql = "INSERT INTO ingredientes (nombre, unidad, stock_actual, stock_minimo, proveedor_id, activo) VALUES (?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, i.getNombre(), i.getUnidad(),
                i.getStockActual() != null ? i.getStockActual() : BigDecimal.ZERO,
                i.getStockMinimo() != null ? i.getStockMinimo() : BigDecimal.ZERO,
                i.getProveedorId(), i.isActivo() ? 1 : 0);
    }

    public void actualizar(Ingrediente i) {
        String sql = "UPDATE ingredientes SET nombre = ?, unidad = ?, stock_minimo = ?, proveedor_id = ? WHERE id = ?";
        jdbc.update(sql, i.getNombre(), i.getUnidad(), i.getStockMinimo(), i.getProveedorId(), i.getId());
    }

    public void actualizarStock(Integer id, BigDecimal nuevoStock) {
        String sql = "UPDATE ingredientes SET stock_actual = ? WHERE id = ?";
        jdbc.update(sql, nuevoStock, id);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        jdbc.update("UPDATE ingredientes SET activo = ? WHERE id = ?", activo ? 1 : 0, id);
    }

    public boolean existeNombre(String nombre, Integer excluirId) {
        // Sin id a excluir se usa 0 (los id empiezan en 1); "? IS NULL" falla en PostgreSQL
        String sql = "SELECT COUNT(*) FROM ingredientes WHERE nombre = ? AND id <> ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, nombre, excluirId != null ? excluirId : 0);
        return count != null && count > 0;
    }
}
