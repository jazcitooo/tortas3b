package py.tortas3b.model;

import java.math.BigDecimal;

/** Tabla detalle_compra: un ingrediente dentro de una compra. */
public class DetalleCompra {

    private Integer id;
    private Integer compraId;
    private Integer ingredienteId;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;

    /** No son columnas: datos del ingrediente, para mostrar. */
    private String ingredienteNombre;
    private String unidad;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCompraId() { return compraId; }
    public void setCompraId(Integer compraId) { this.compraId = compraId; }

    public Integer getIngredienteId() { return ingredienteId; }
    public void setIngredienteId(Integer ingredienteId) { this.ingredienteId = ingredienteId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getIngredienteNombre() { return ingredienteNombre; }
    public void setIngredienteNombre(String ingredienteNombre) { this.ingredienteNombre = ingredienteNombre; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}
