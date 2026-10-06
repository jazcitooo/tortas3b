package py.tortas3b.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla movimientos_stock: historial de cambios de stock.
 * tipo: AJUSTE (manual) o COMPRA (recepción). cantidad > 0 entra, < 0 sale.
 */
public class MovimientoStock {

    private Integer id;
    private Integer ingredienteId;
    private Integer usuarioId;
    private Integer compraId;
    private LocalDateTime fecha;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal stockResultante;
    private String motivo;

    /** No son columnas: datos para mostrar. */
    private String ingredienteNombre;
    private String usuarioNombre;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIngredienteId() { return ingredienteId; }
    public void setIngredienteId(Integer ingredienteId) { this.ingredienteId = ingredienteId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Integer getCompraId() { return compraId; }
    public void setCompraId(Integer compraId) { this.compraId = compraId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getStockResultante() { return stockResultante; }
    public void setStockResultante(BigDecimal stockResultante) { this.stockResultante = stockResultante; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getIngredienteNombre() { return ingredienteNombre; }
    public void setIngredienteNombre(String ingredienteNombre) { this.ingredienteNombre = ingredienteNombre; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
}
