package py.tortas3b.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Producto;

/**
 * Operaciones SQL para la tabla productos.
 */
@Repository
public class ProductoRepository {

    private final JdbcTemplate jdbc;

    public ProductoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Producto> rowMapper = (rs, rowNum) -> mapearProducto(rs);

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setCostoKilo(rs.getBigDecimal("costo_kilo"));
        p.setPesoKg(rs.getBigDecimal("peso_kg"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    public List<Producto> listarTodos(String categoria) {
        if (categoria != null && !categoria.trim().isEmpty() && !"TODAS".equalsIgnoreCase(categoria)) {
            return jdbc.query("SELECT * FROM productos WHERE categoria = ? ORDER BY nombre ASC", rowMapper, categoria);
        }
        return jdbc.query("SELECT * FROM productos ORDER BY categoria ASC, nombre ASC", rowMapper);
    }

    public List<Producto> listarActivos() {
        return jdbc.query("SELECT * FROM productos WHERE activo = 1 ORDER BY categoria ASC, nombre ASC", rowMapper);
    }

    public Optional<Producto> findById(Integer id) {
        String sql = "SELECT * FROM productos WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void insertar(Producto p) {
        String sql = "INSERT INTO productos (nombre, descripcion, categoria, precio, costo_kilo, peso_kg, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, p.getNombre(), p.getDescripcion(), p.getCategoria(), p.getPrecio(),
                p.getCostoKilo() != null ? p.getCostoKilo() : 0, p.getPesoKg(), p.isActivo() ? 1 : 0);
    }

    public void actualizar(Producto p) {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, categoria = ?, precio = ?, costo_kilo = ?, peso_kg = ? WHERE id = ?";
        jdbc.update(sql, p.getNombre(), p.getDescripcion(), p.getCategoria(), p.getPrecio(),
                p.getCostoKilo() != null ? p.getCostoKilo() : 0, p.getPesoKg(), p.getId());
    }

    public void cambiarEstado(Integer id, boolean activo) {
        jdbc.update("UPDATE productos SET activo = ? WHERE id = ?", activo ? 1 : 0, id);
    }

    public boolean existeNombre(String nombre, Integer excluirId) {
        // Sin id a excluir se usa 0 (los id empiezan en 1); "? IS NULL" falla en PostgreSQL
        String sql = "SELECT COUNT(*) FROM productos WHERE nombre = ? AND id <> ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, nombre, excluirId != null ? excluirId : 0);
        return count != null && count > 0;
    }
}
