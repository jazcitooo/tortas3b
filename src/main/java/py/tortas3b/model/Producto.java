package py.tortas3b.model;

import java.math.BigDecimal;

/**
 * Tabla productos. Categorías: TORTA, CUPCAKE, CHOCOLATE, OTRO.
 * pesoKg = peso aproximado de una unidad (se usa para calcular kilos vendidos).
 */
public class Producto {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private BigDecimal precio;
    private BigDecimal costoKilo;
    private BigDecimal pesoKg;
    private boolean activo = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getCostoKilo() { return costoKilo; }
    public void setCostoKilo(BigDecimal costoKilo) { this.costoKilo = costoKilo; }

    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
