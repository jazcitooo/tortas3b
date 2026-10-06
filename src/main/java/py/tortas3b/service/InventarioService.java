package py.tortas3b.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.tortas3b.model.Ingrediente;
import py.tortas3b.model.MovimientoStock;
import py.tortas3b.repository.IngredienteRepository;
import py.tortas3b.repository.MovimientoStockRepository;

/**
 * Servicio para control de ingredientes, alertas de stock mínimo y ajustes auditados de inventario.
 */
@Service
public class InventarioService {

    private final IngredienteRepository ingredienteRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    public InventarioService(IngredienteRepository ingredienteRepository,
                             MovimientoStockRepository movimientoStockRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.movimientoStockRepository = movimientoStockRepository;
    }

    public List<Ingrediente> listarTodos() {
        return ingredienteRepository.listarTodos();
    }

    public List<Ingrediente> listarActivos() {
        return ingredienteRepository.listarActivos();
    }

    public List<Ingrediente> listarConStockBajo() {
        return ingredienteRepository.listarConStockBajo();
    }

    public int contarStockBajo() {
        return ingredienteRepository.contarStockBajo();
    }

    public Optional<Ingrediente> buscarPorId(Integer id) {
        return ingredienteRepository.findById(id);
    }

    public void registrar(Ingrediente i) {
        validarIngrediente(i, null);
        ingredienteRepository.insertar(i);
    }

    public void actualizar(Ingrediente i) {
        if (i.getId() == null) {
            throw new IllegalArgumentException("El ingrediente debe tener un ID para actualizarse.");
        }
        validarIngrediente(i, i.getId());
        ingredienteRepository.actualizar(i);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        ingredienteRepository.cambiarEstado(id, activo);
    }

    /**
     * Realiza un ajuste manual en el stock y registra el movimiento para auditoría.
     */
    @Transactional
    public void ajustarStock(Integer ingredienteId, Integer usuarioId, BigDecimal cantidad, String motivo) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("La cantidad del ajuste no puede ser cero.");
        }

        Ingrediente ing = ingredienteRepository.findById(ingredienteId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado con ID " + ingredienteId));

        BigDecimal stockActual = ing.getStockActual() != null ? ing.getStockActual() : BigDecimal.ZERO;
        BigDecimal nuevoStock = stockActual.add(cantidad);

        if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El ajuste dejaría el stock en negativo (" + nuevoStock + " " + ing.getUnidad() + "). Operación no permitida.");
        }

        // 1. Actualizar el stock del ingrediente
        ingredienteRepository.actualizarStock(ingredienteId, nuevoStock);

        // 2. Registrar el movimiento para auditoría
        MovimientoStock mov = new MovimientoStock();
        mov.setIngredienteId(ingredienteId);
        mov.setUsuarioId(usuarioId);
        mov.setTipo("AJUSTE");
        mov.setCantidad(cantidad);
        mov.setStockResultante(nuevoStock);
        mov.setMotivo(motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Ajuste manual de inventario");

        movimientoStockRepository.insertar(mov);
    }

    public List<MovimientoStock> listarMovimientosRecientes(int limite) {
        return movimientoStockRepository.listarUltimos(limite);
    }

    public List<MovimientoStock> listarMovimientosPorIngrediente(Integer ingredienteId) {
        return movimientoStockRepository.listarPorIngrediente(ingredienteId);
    }

    private void validarIngrediente(Ingrediente i, Integer excluirId) {
        if (i.getNombre() == null || i.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del ingrediente es obligatorio.");
        }
        if (i.getUnidad() == null || i.getUnidad().trim().isEmpty()) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        }
        if (i.getStockMinimo() != null && i.getStockMinimo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        if (ingredienteRepository.existeNombre(i.getNombre().trim(), excluirId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente registrado con el nombre '" + i.getNombre().trim() + "'.");
        }
    }
}
