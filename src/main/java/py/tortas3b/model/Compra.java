package py.tortas3b.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tabla compras (pedido de reposición a un proveedor). estado: PENDIENTE, RECIBIDA, CANCELADA. */
public class Compra {

    private Integer id;
    private Integer proveedorId;
    private Integer usuarioId;
    private LocalDateTime fecha;
    private LocalDateTime fechaRecepcion;
    private String estado = "PENDIENTE";
    private BigDecimal total = BigDecimal.ZERO;
    private String observaciones;

    /** No son columnas: datos relacionados para mostrar. */
    private String proveedorNombre;
    private List<DetalleCompra> detalles = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }
}
