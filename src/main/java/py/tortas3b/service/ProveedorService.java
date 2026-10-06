package py.tortas3b.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import py.tortas3b.model.Proveedor;
import py.tortas3b.repository.ProveedorRepository;

/**
 * Reglas de negocio para la gestión de proveedores.
 */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<Proveedor> listar() {
        return proveedorRepository.listarTodos();
    }

    public List<Proveedor> listarActivos() {
        return proveedorRepository.listarActivos();
    }

    public Optional<Proveedor> buscarPorId(Integer id) {
        return proveedorRepository.findById(id);
    }

    public void registrar(Proveedor p) {
        validarProveedor(p, null);
        proveedorRepository.insertar(p);
    }

    public void actualizar(Proveedor p) {
        if (p.getId() == null) {
            throw new IllegalArgumentException("El proveedor debe tener un ID para actualizarse.");
        }
        validarProveedor(p, p.getId());
        proveedorRepository.actualizar(p);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        proveedorRepository.cambiarEstado(id, activo);
    }

    private void validarProveedor(Proveedor p, Integer excluirId) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");
        }
        if (p.getTelefono() == null || p.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono del proveedor es obligatorio.");
        }
        String tel = p.getTelefono().trim();
        if (!tel.matches("^\\d{7,15}$")) {
            throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos numéricos.");
        }
        if (proveedorRepository.existeNombre(p.getNombre().trim(), excluirId)) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con el nombre '" + p.getNombre().trim() + "'.");
        }
    }
}
