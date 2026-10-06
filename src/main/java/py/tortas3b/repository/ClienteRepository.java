package py.tortas3b.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Cliente;
import py.tortas3b.model.Pedido;

/**
 * Operaciones SQL para la tabla clientes.
 */
@Repository
public class ClienteRepository {

    private final JdbcTemplate jdbc;

    public ClienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Cliente> rowMapper = (rs, rowNum) -> mapearCliente(rs);

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setEmail(rs.getString("email"));
        c.setObservaciones(rs.getString("observaciones"));
        c.setActivo(rs.getBoolean("activo"));
        if (rs.getTimestamp("creado_en") != null) {
            c.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        }
        return c;
    }

    public List<Cliente> listarTodos(String busqueda) {
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            String patron = "%" + busqueda.trim() + "%";
            // LOWER(): en PostgreSQL LIKE distingue mayúsculas; así "ana" encuentra "Ana"
            String sql = "SELECT * FROM clientes WHERE LOWER(nombre) LIKE LOWER(?) OR telefono LIKE ? ORDER BY nombre ASC";
            return jdbc.query(sql, rowMapper, patron, patron);
        }
        return jdbc.query("SELECT * FROM clientes ORDER BY nombre ASC", rowMapper);
    }

    public List<Cliente> listarActivos() {
        return jdbc.query("SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre ASC", rowMapper);
    }

    public Optional<Cliente> findById(Integer id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer insertar(Cliente c) {
        String sql = "INSERT INTO clientes (nombre, telefono, direccion, email, observaciones, activo) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTelefono());
            ps.setString(3, c.getDireccion());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getObservaciones());
            ps.setInt(6, c.isActivo() ? 1 : 0);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            c.setId(keyHolder.getKey().intValue());
            return c.getId();
        }
        return null;
    }

    public void actualizar(Cliente c) {
        String sql = "UPDATE clientes SET nombre = ?, telefono = ?, direccion = ?, email = ?, observaciones = ? WHERE id = ?";
        jdbc.update(sql, c.getNombre(), c.getTelefono(), c.getDireccion(), c.getEmail(), c.getObservaciones(), c.getId());
    }

    public void cambiarEstado(Integer id, boolean activo) {
        jdbc.update("UPDATE clientes SET activo = ? WHERE id = ?", activo ? 1 : 0, id);
    }

    public boolean existeTelefono(String telefono, Integer excluirId) {
        // Sin id a excluir se usa 0 (los id empiezan en 1); "? IS NULL" falla en PostgreSQL
        String sql = "SELECT COUNT(*) FROM clientes WHERE telefono = ? AND id <> ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, telefono, excluirId != null ? excluirId : 0);
        return count != null && count > 0;
    }

    public List<Pedido> obtenerHistorialPedidos(Integer clienteId) {
        String sql = "SELECT id, cliente_id, usuario_id, fecha_pedido, fecha_entrega, hora_entrega, tipo_entrega, estado, total, observaciones "
                   + "FROM pedidos WHERE cliente_id = ? ORDER BY fecha_entrega DESC, hora_entrega DESC";
        return jdbc.query(sql, (rs, rowNum) -> {
            Pedido p = new Pedido();
            p.setId(rs.getInt("id"));
            p.setClienteId(rs.getInt("cliente_id"));
            p.setUsuarioId(rs.getInt("usuario_id"));
            if (rs.getTimestamp("fecha_pedido") != null) {
                p.setFechaPedido(rs.getTimestamp("fecha_pedido").toLocalDateTime());
            }
            if (rs.getDate("fecha_entrega") != null) {
                p.setFechaEntrega(rs.getDate("fecha_entrega").toLocalDate());
            }
            if (rs.getTime("hora_entrega") != null) {
                p.setHoraEntrega(rs.getTime("hora_entrega").toLocalTime());
            }
            p.setTipoEntrega(rs.getString("tipo_entrega"));
            p.setEstado(rs.getString("estado"));
            p.setTotal(rs.getBigDecimal("total"));
            p.setObservaciones(rs.getString("observaciones"));
            return p;
        }, clienteId);
    }
}
