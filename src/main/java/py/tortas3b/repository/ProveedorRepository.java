package py.tortas3b.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Proveedor;

/**
 * Operaciones SQL para la tabla proveedores.
 */
@Repository
public class ProveedorRepository {

    private final JdbcTemplate jdbc;

    public ProveedorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Proveedor> rowMapper = (rs, rowNum) -> mapearProveedor(rs);

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setTelefono(rs.getString("telefono"));
        p.setDireccion(rs.getString("direccion"));
        p.setInsumos(rs.getString("insumos"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    public List<Proveedor> listarTodos() {
        return jdbc.query("SELECT * FROM proveedores ORDER BY nombre ASC", rowMapper);
    }

    public List<Proveedor> listarActivos() {
        return jdbc.query("SELECT * FROM proveedores WHERE activo = 1 ORDER BY nombre ASC", rowMapper);
    }

    public Optional<Proveedor> findById(Integer id) {
        String sql = "SELECT * FROM proveedores WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void insertar(Proveedor p) {
        String sql = "INSERT INTO proveedores (nombre, telefono, direccion, insumos, activo) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, p.getNombre(), p.getTelefono(), p.getDireccion(), p.getInsumos(), p.isActivo() ? 1 : 0);
    }

    public void actualizar(Proveedor p) {
        String sql = "UPDATE proveedores SET nombre = ?, telefono = ?, direccion = ?, insumos = ? WHERE id = ?";
        jdbc.update(sql, p.getNombre(), p.getTelefono(), p.getDireccion(), p.getInsumos(), p.getId());
    }

    public void cambiarEstado(Integer id, boolean activo) {
        jdbc.update("UPDATE proveedores SET activo = ? WHERE id = ?", activo ? 1 : 0, id);
    }

    public boolean existeNombre(String nombre, Integer excluirId) {
        // Sin id a excluir se usa 0 (los id empiezan en 1); "? IS NULL" falla en PostgreSQL
        String sql = "SELECT COUNT(*) FROM proveedores WHERE nombre = ? AND id <> ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, nombre, excluirId != null ? excluirId : 0);
        return count != null && count > 0;
    }
}
