package py.tortas3b.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.tortas3b.model.Pedido;
import py.tortas3b.model.Venta;
import py.tortas3b.repository.PedidoRepository;
import py.tortas3b.repository.VentaRepository;

/**
 * Reglas de negocio para cobro y arqueo de ventas.
 */
@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final PedidoRepository pedidoRepository;

    public VentaService(VentaRepository ventaRepository, PedidoRepository pedidoRepository) {
        this.ventaRepository = ventaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Venta> listar(LocalDate fechaDesde, LocalDate fechaHasta) {
        return ventaRepository.listar(fechaDesde, fechaHasta);
    }

    public Optional<Venta> buscarPorId(Integer id) {
        return ventaRepository.findById(id);
    }

    public Optional<Venta> buscarPorPedidoId(Integer pedidoId) {
        return ventaRepository.findByPedidoId(pedidoId);
    }

    public BigDecimal totalVentasHoy() {
        BigDecimal total = ventaRepository.obtenerTotalVentasHoy();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Registra el cobro de un pedido.
     */
    @Transactional
    public void registrarVenta(Integer pedidoId, Integer usuarioId, String metodoPago) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("Debe indicar el pedido a cobrar.");
        }
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un método de pago.");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido ID " + pedidoId + " no encontrado"));

        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalStateException("No se puede registrar venta de un pedido cancelado.");
        }

        if (ventaRepository.findByPedidoId(pedidoId).isPresent()) {
            throw new IllegalStateException("El pedido #" + pedidoId + " ya tiene una venta registrada.");
        }

        Venta venta = new Venta();
        venta.setPedidoId(pedidoId);
        venta.setUsuarioId(usuarioId);
        venta.setMetodoPago(metodoPago);
        venta.setTotal(pedido.getTotal());

        ventaRepository.insertar(venta);
    }

    /**
     * Anula una venta registrada (exclusivo para rol ADMIN según D1/plan).
     */
    @Transactional
    public void anularVenta(Integer ventaId) {
        Venta v = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID " + ventaId));

        ventaRepository.eliminar(v.getId());
    }
}
