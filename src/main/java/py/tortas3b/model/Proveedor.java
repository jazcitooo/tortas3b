package py.tortas3b.model;

/** Tabla proveedores. insumos = descripción de lo que suministra. */
public class Proveedor {

    private Integer id;
    private String nombre;
    private String telefono;
    private String direccion;
    private String insumos;
    private boolean activo = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getInsumos() { return insumos; }
    public void setInsumos(String insumos) { this.insumos = insumos; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
