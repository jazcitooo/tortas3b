package py.tortas3b.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Usuario;

/**
 * Operaciones de base de datos para la tabla usuarios mediante JdbcTemplate parametrizado.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Usuario> rowMapper = (rs, rowNum) -> mapearUsuario(rs);

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        if (rs.getTimestamp("creado_en") != null) {
            u.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        }
        return u;
    }

    public Optional<Usuario> findByUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try {
            Usuario u = jdbc.queryForObject(sql, rowMapper, username);
            return Optional.ofNullable(u);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Usuario> findById(Integer id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try {
            Usuario u = jdbc.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(u);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuarios ORDER BY id ASC";
        return jdbc.query(sql, rowMapper);
    }

    public void insertar(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, username, password_hash, rol, activo) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, u.getNombre(), u.getUsername(), u.getPasswordHash(), u.getRol(), u.isActivo() ? 1 : 0);
    }

    public void actualizar(Usuario u) {
        String sql = "UPDATE usuarios SET nombre = ?, username = ?, rol = ? WHERE id = ?";
        jdbc.update(sql, u.getNombre(), u.getUsername(), u.getRol(), u.getId());
    }

    public void actualizarPassword(Integer id, String passwordHash) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        jdbc.update(sql, passwordHash, id);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        jdbc.update(sql, activo ? 1 : 0, id);
    }

    public boolean existeUsername(String username, Integer excluirId) {
        // Sin id a excluir se usa 0 (los id empiezan en 1); "? IS NULL" falla en PostgreSQL
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND id <> ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, username, excluirId != null ? excluirId : 0);
        return count != null && count > 0;
    }
}
