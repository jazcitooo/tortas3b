package py.tortas3b.model;

import java.time.LocalDateTime;

/**
 * Tabla delivery (1 a 1 con un pedido de tipo DELIVERY).
 * estado: PENDIENTE, PREPARANDO, EN_CAMINO, ENTREGADO, CANCELADO.
 */
public class Delivery {

    private Integer id;
    private Integer pedidoId;
    private String direccion;
    private String telefonoContacto;
    private String estado = "PENDIENTE";
    private String observaciones;
    private LocalDateTime actualizadoEn;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPedidoId() { return pedidoId; }
    public void setPedidoId(Integer pedidoId) { this.pedidoId = pedidoId; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
