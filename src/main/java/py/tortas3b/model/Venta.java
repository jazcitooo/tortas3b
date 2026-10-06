package py.tortas3b.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tabla ventas. metodoPago: EFECTIVO, TRANSFERENCIA, OTRO. */
public class Venta {

    private Integer id;
    private Integer pedidoId;
    private Integer usuarioId;
    private LocalDateTime fecha;
    private String metodoPago;
    private BigDecimal total;

    /** No es columna: cliente del pedido, para mostrar. */
    private String clienteNombre;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPedidoId() { return pedidoId; }
    public void setPedidoId(Integer pedidoId) { this.pedidoId = pedidoId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
}
