package py.tortas3b.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import py.tortas3b.model.Pedido;

/**
 * Operaciones SQL para pedidos, incluyendo consulta con bloqueo transaccional
 * para el límite de 5 entregas por hora.
 */
@Repository
public class PedidoRepository {

    private final JdbcTemplate jdbc;

    public PedidoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Pedido> rowMapper = (rs, rowNum) -> mapearPedido(rs);

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
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
        try {
            p.setClienteNombre(rs.getString("cliente_nombre"));
            p.setClienteTelefono(rs.getString("cliente_telefono"));
        } catch (SQLException ignored) {}
        return p;
    }

    public List<Pedido> listar(LocalDate fecha, String estado, String tipoEntrega) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.nombre AS cliente_nombre, c.telefono AS cliente_telefono " +
                "FROM pedidos p JOIN clientes c ON p.cliente_id = c.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fecha != null) {
            sql.append("AND p.fecha_entrega = ? ");
            params.add(Date.valueOf(fecha));
        }
        if (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado)) {
            sql.append("AND p.estado = ? ");
            params.add(estado);
        }
        if (tipoEntrega != null && !tipoEntrega.trim().isEmpty() && !"TODOS".equalsIgnoreCase(tipoEntrega)) {
            sql.append("AND p.tipo_entrega = ? ");
            params.add(tipoEntrega);
        }

        sql.append("ORDER BY p.fecha_entrega DESC, p.hora_entrega ASC, p.id DESC");
        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Pedido> listarProximasEntregas(int limite) {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.telefono AS cliente_telefono " +
                     "FROM pedidos p JOIN clientes c ON p.cliente_id = c.id " +
                     "WHERE p.fecha_entrega >= CURRENT_DATE AND p.estado NOT IN ('ENTREGADO', 'CANCELADO') " +
                     "ORDER BY p.fecha_entrega ASC, p.hora_entrega ASC LIMIT ?";
        return jdbc.query(sql, rowMapper, limite);
    }

    public int contarPendientes() {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE estado IN ('PENDIENTE', 'CONFIRMADO', 'EN_PREPARACION')";
        Integer count = jdbc.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int contarHoy() {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE fecha_entrega = CURRENT_DATE AND estado <> 'CANCELADO'";
        Integer count = jdbc.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public Optional<Pedido> findById(Integer id) {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.telefono AS cliente_telefono " +
                     "FROM pedidos p JOIN clientes c ON p.cliente_id = c.id WHERE p.id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Cuenta los pedidos existentes en esa franja horaria usando SELECT ... FOR UPDATE
     * para asegurar aislamiento concurrente. Se bloquean las filas y se cuentan en Java,
     * porque PostgreSQL no admite FOR UPDATE junto con COUNT(*).
     */
    public int contarEntregasPorHora(LocalDate fecha, int hora, Integer excluirPedidoId) {
        StringBuilder sql = new StringBuilder(
                "SELECT id FROM pedidos " +
                "WHERE fecha_entrega = ? AND EXTRACT(HOUR FROM hora_entrega) = ? AND estado <> 'CANCELADO' ");
        List<Object> params = new ArrayList<>(List.of(Date.valueOf(fecha), hora));
        if (excluirPedidoId != null) {
            sql.append("AND id <> ? ");
            params.add(excluirPedidoId);
        }
        sql.append("FOR UPDATE");
        return jdbc.queryForList(sql.toString(), Integer.class, params.toArray()).size();
    }

    public Integer insertar(Pedido p) {
        String sql = "INSERT INTO pedidos (cliente_id, usuario_id, fecha_entrega, hora_entrega, tipo_entrega, estado, total, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setInt(1, p.getClienteId());
            ps.setInt(2, p.getUsuarioId());
            ps.setDate(3, Date.valueOf(p.getFechaEntrega()));
            ps.setTime(4, Time.valueOf(p.getHoraEntrega()));
            ps.setString(5, p.getTipoEntrega());
            ps.setString(6, p.getEstado());
            ps.setBigDecimal(7, p.getTotal());
            ps.setString(8, p.getObservaciones());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            p.setId(keyHolder.getKey().intValue());
            return p.getId();
        }
        return null;
    }

    public void actualizar(Pedido p) {
        String sql = "UPDATE pedidos SET cliente_id = ?, fecha_entrega = ?, hora_entrega = ?, " +
                     "tipo_entrega = ?, observaciones = ? WHERE id = ?";
        jdbc.update(sql, p.getClienteId(), Date.valueOf(p.getFechaEntrega()),
                Time.valueOf(p.getHoraEntrega()), p.getTipoEntrega(), p.getObservaciones(), p.getId());
    }

    public void actualizarEstado(Integer id, String estado) {
        jdbc.update("UPDATE pedidos SET estado = ? WHERE id = ?", estado, id);
    }

    public void actualizarTotal(Integer id, java.math.BigDecimal total) {
        jdbc.update("UPDATE pedidos SET total = ? WHERE id = ?", total, id);
    }
}
