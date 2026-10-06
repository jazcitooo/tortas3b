package py.tortas3b.model;

import java.math.BigDecimal;

/** Tabla ingredientes. Unidades: KG, G, L, ML, UNIDAD. */
public class Ingrediente {

    private Integer id;
    private String nombre;
    private String unidad;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private Integer proveedorId;
    private boolean activo = true;

    /** No es columna: nombre del proveedor, para mostrar en listas. */
    private String proveedorNombre;

    /** Regla del negocio: stock bajo cuando la cantidad es igual o menor al mínimo. */
    public boolean isStockBajo() {
        return stockActual != null && stockMinimo != null
                && stockActual.compareTo(stockMinimo) <= 0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
}
