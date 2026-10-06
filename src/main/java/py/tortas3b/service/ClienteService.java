package py.tortas3b.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import py.tortas3b.model.Cliente;
import py.tortas3b.model.Pedido;
import py.tortas3b.repository.ClienteRepository;

/**
 * Reglas de negocio para la gestión de clientes.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listar(String busqueda) {
        return clienteRepository.listarTodos(busqueda);
    }

    public List<Cliente> listarActivos() {
        return clienteRepository.listarActivos();
    }

    public Optional<Cliente> buscarPorId(Integer id) {
        return clienteRepository.findById(id);
    }

    public List<Pedido> obtenerHistorialPedidos(Integer clienteId) {
        return clienteRepository.obtenerHistorialPedidos(clienteId);
    }

    public Integer registrar(Cliente c) {
        validarCliente(c, null);
        return clienteRepository.insertar(c);
    }

    public void actualizar(Cliente c) {
        if (c.getId() == null) {
            throw new IllegalArgumentException("El cliente debe tener un ID para actualizarse.");
        }
        validarCliente(c, c.getId());
        clienteRepository.actualizar(c);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        clienteRepository.cambiarEstado(id, activo);
    }

    private void validarCliente(Cliente c, Integer excluirId) {
        if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (c.getTelefono() == null || c.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        String tel = c.getTelefono().trim();
        if (!tel.matches("^\\d{7,15}$")) {
            throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos numéricos.");
        }
        if (clienteRepository.existeTelefono(tel, excluirId)) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el teléfono " + tel + ".");
        }
    }
}
