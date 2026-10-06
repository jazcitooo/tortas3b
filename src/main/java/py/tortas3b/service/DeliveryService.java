package py.tortas3b.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.tortas3b.model.Delivery;
import py.tortas3b.repository.DeliveryRepository;
import py.tortas3b.repository.PedidoRepository;

/**
 * Servicio para seguimiento de entregas a domicilio y sincronización con pedidos.
 */
@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final PedidoRepository pedidoRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, PedidoRepository pedidoRepository) {
        this.deliveryRepository = deliveryRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Delivery> listar(LocalDate fecha, String estado) {
        return deliveryRepository.listar(fecha, estado);
    }

    public Optional<Delivery> buscarPorPedidoId(Integer pedidoId) {
        return deliveryRepository.findByPedidoId(pedidoId);
    }

    @Transactional
    public void cambiarEstado(Integer pedidoId, String nuevoEstado) {
        deliveryRepository.actualizarEstado(pedidoId, nuevoEstado);

        // Regla: si el delivery se marca como ENTREGADO, el pedido pasa a ENTREGADO
        if ("ENTREGADO".equalsIgnoreCase(nuevoEstado)) {
            pedidoRepository.actualizarEstado(pedidoId, "ENTREGADO");
        } else if ("CANCELADO".equalsIgnoreCase(nuevoEstado)) {
            pedidoRepository.actualizarEstado(pedidoId, "CANCELADO");
        }
    }
}
