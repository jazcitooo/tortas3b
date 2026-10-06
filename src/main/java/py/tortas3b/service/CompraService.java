package py.tortas3b.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.tortas3b.model.Compra;
import py.tortas3b.model.DetalleCompra;
import py.tortas3b.model.Ingrediente;
import py.tortas3b.model.MovimientoStock;
import py.tortas3b.repository.CompraRepository;
import py.tortas3b.repository.DetalleCompraRepository;
import py.tortas3b.repository.IngredienteRepository;
import py.tortas3b.repository.MovimientoStockRepository;
import py.tortas3b.repository.ProveedorRepository;

/**
 * Reglas de negocio para compras de reposición y actualización automática de stock al recibirlas.
 */
@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final IngredienteRepository ingredienteRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ProveedorRepository proveedorRepository;

    public CompraService(CompraRepository compraRepository,
                         DetalleCompraRepository detalleCompraRepository,
                         IngredienteRepository ingredienteRepository,
                         MovimientoStockRepository movimientoStockRepository,
                         ProveedorRepository proveedorRepository) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.proveedorRepository = proveedorRepository;
    }

    public List<Compra> listar(String estado) {
        return compraRepository.listar(estado);
    }

    public Optional<Compra> buscarPorId(Integer id) {
        Optional<Compra> opt = compraRepository.findById(id);
        if (opt.isPresent()) {
            Compra c = opt.get();
            c.setDetalles(detalleCompraRepository.listarPorCompra(id));
        }
        return opt;
    }

    @Transactional
    public Integer registrar(Compra c, List<DetalleCompra> detalles) {
        if (c.getProveedorId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor.");
        }
        proveedorRepository.findById(c.getProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La compra debe incluir al menos un ingrediente.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompra item : detalles) {
            if (item.getIngredienteId() == null) {
                throw new IllegalArgumentException("Debe indicar el ingrediente.");
            }
            if (item.getCantidad() == null || item.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
            }
            if (item.getCostoUnitario() == null || item.getCostoUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El costo unitario no puede ser negativo.");
            }
            BigDecimal subtotal = item.getCostoUnitario().multiply(item.getCantidad());
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
        }

        c.setTotal(total);
        c.setEstado("PENDIENTE");
        Integer compraId = compraRepository.insertar(c);

        for (DetalleCompra item : detalles) {
            item.setCompraId(compraId);
            detalleCompraRepository.insertar(item);
        }

        return compraId;
    }

    /**
     * Marca la compra como recibida y suma automáticamente las cantidades al inventario.
     */
    @Transactional
    public void recibirCompra(Integer compraId, Integer usuarioId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada con ID " + compraId));

        if (!"PENDIENTE".equalsIgnoreCase(compra.getEstado())) {
            throw new IllegalStateException("Solo se pueden recibir compras en estado PENDIENTE.");
        }

        List<DetalleCompra> detalles = detalleCompraRepository.listarPorCompra(compraId);

        // 1. Sumar stock para cada ingrediente y registrar movimiento
        for (DetalleCompra item : detalles) {
            Ingrediente ing = ingredienteRepository.findById(item.getIngredienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Ingrediente ID " + item.getIngredienteId() + " no encontrado"));

            BigDecimal stockPrevio = ing.getStockActual() != null ? ing.getStockActual() : BigDecimal.ZERO;
            BigDecimal nuevoStock = stockPrevio.add(item.getCantidad());

            ingredienteRepository.actualizarStock(ing.getId(), nuevoStock);

            MovimientoStock mov = new MovimientoStock();
            mov.setIngredienteId(ing.getId());
            mov.setUsuarioId(usuarioId);
            mov.setCompraId(compraId);
            mov.setTipo("COMPRA");
            mov.setCantidad(item.getCantidad());
            mov.setStockResultante(nuevoStock);
            mov.setMotivo("Recepción de compra #" + compraId);

            movimientoStockRepository.insertar(mov);
        }

        // 2. Actualizar estado de la compra
        compraRepository.actualizarEstado(compraId, "RECIBIDA", LocalDateTime.now());
    }

    @Transactional
    public void cancelarCompra(Integer compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));

        if (!"PENDIENTE".equalsIgnoreCase(compra.getEstado())) {
            throw new IllegalStateException("Solo se pueden cancelar compras en estado PENDIENTE.");
        }

        compraRepository.actualizarEstado(compraId, "CANCELADA", null);
    }
}
