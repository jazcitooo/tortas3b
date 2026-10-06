package py.tortas3b.repository;

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

import py.tortas3b.model.Delivery;

/**
 * Operaciones SQL para la tabla delivery.
 */
@Repository
public class DeliveryRepository {

    private final JdbcTemplate jdbc;

    public DeliveryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Delivery> rowMapper = (rs, rowNum) -> mapearDelivery(rs);

    private Delivery mapearDelivery(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setId(rs.getInt("id"));
        d.setPedidoId(rs.getInt("pedido_id"));
        d.setDireccion(rs.getString("direccion"));
        d.setTelefonoContacto(rs.getString("telefono_contacto"));
        d.setEstado(rs.getString("estado"));
        d.setObservaciones(rs.getString("observaciones"));
        if (rs.getTimestamp("actualizado_en") != null) {
            d.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
        }
        return d;
    }

    public Optional<Delivery> findByPedidoId(Integer pedidoId) {
        String sql = "SELECT * FROM delivery WHERE pedido_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, rowMapper, pedidoId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Delivery> listar(LocalDate fecha, String estado) {
        StringBuilder sql = new StringBuilder(
                "SELECT d.* FROM delivery d " +
                "JOIN pedidos p ON d.pedido_id = p.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fecha != null) {
            sql.append("AND p.fecha_entrega = ? ");
            params.add(Date.valueOf(fecha));
        }
        if (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado)) {
            sql.append("AND d.estado = ? ");
            params.add(estado);
        }

        sql.append("ORDER BY p.fecha_entrega DESC, p.hora_entrega ASC, d.id DESC");
        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public void insertar(Delivery d) {
        String sql = "INSERT INTO delivery (pedido_id, direccion, telefono_contacto, estado, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, d.getPedidoId(), d.getDireccion(), d.getTelefonoContacto(),
                d.getEstado() != null ? d.getEstado() : "PENDIENTE", d.getObservaciones());
    }

    public void actualizar(Delivery d) {
        // actualizado_en se fija aquí: PostgreSQL no tiene "ON UPDATE CURRENT_TIMESTAMP"
        String sql = "UPDATE delivery SET direccion = ?, telefono_contacto = ?, observaciones = ?, actualizado_en = CURRENT_TIMESTAMP WHERE pedido_id = ?";
        jdbc.update(sql, d.getDireccion(), d.getTelefonoContacto(), d.getObservaciones(), d.getPedidoId());
    }

    public void actualizarEstado(Integer pedidoId, String estado) {
        String sql = "UPDATE delivery SET estado = ?, actualizado_en = CURRENT_TIMESTAMP WHERE pedido_id = ?";
        jdbc.update(sql, estado, pedidoId);
    }
}
