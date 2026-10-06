package py.tortas3b.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import py.tortas3b.model.Producto;
import py.tortas3b.repository.ProductoRepository;

/**
 * Reglas de negocio para la gestión del catálogo de productos.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar(String categoria) {
        return productoRepository.listarTodos(categoria);
    }

    public List<Producto> listarActivos() {
        return productoRepository.listarActivos();
    }

    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public void registrar(Producto p) {
        validarProducto(p, null);
        productoRepository.insertar(p);
    }

    public void actualizar(Producto p) {
        if (p.getId() == null) {
            throw new IllegalArgumentException("El producto debe tener un ID para actualizarse.");
        }
        validarProducto(p, p.getId());
        productoRepository.actualizar(p);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        productoRepository.cambiarEstado(id, activo);
    }

    private void validarProducto(Producto p, Integer excluirId) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (p.getCategoria() == null || p.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser un monto mayor a 0.");
        }
        if (p.getPesoKg() == null || p.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso aproximado en kg debe ser mayor a 0.");
        }
        if (p.getCostoKilo() != null && p.getCostoKilo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo por kilo no puede ser negativo.");
        }
        if (productoRepository.existeNombre(p.getNombre().trim(), excluirId)) {
            throw new IllegalArgumentException("Ya existe un producto registrado con el nombre '" + p.getNombre().trim() + "'.");
        }
    }
}
