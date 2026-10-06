package py.tortas3b.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.tortas3b.model.Cliente;
import py.tortas3b.model.Delivery;
import py.tortas3b.model.DetallePedido;
import py.tortas3b.model.Pedido;
import py.tortas3b.model.Producto;
import py.tortas3b.repository.ClienteRepository;
import py.tortas3b.repository.DeliveryRepository;
import py.tortas3b.repository.DetallePedidoRepository;
import py.tortas3b.repository.PedidoRepository;
import py.tortas3b.repository.ProductoRepository;

/**
 * Reglas de negocio para pedidos: control de horario de atención,
 * límite transaccional de 5 entregas por hora y recálculo seguro de totales.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final DeliveryRepository deliveryRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    @Value("${negocio.max-entregas-por-hora:5}")
    private int maxEntregasPorHora;

    @Value("${negocio.hora-apertura:08:00}")
    private String horaAperturaStr;

    @Value("${negocio.hora-cierre:20:00}")
    private String horaCierreStr;

    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         DeliveryRepository deliveryRepository,
                         ClienteRepository clienteRepository,
                         ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.deliveryRepository = deliveryRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    public List<Pedido> listar(LocalDate fecha, String estado, String tipoEntrega) {
        return pedidoRepository.listar(fecha, estado, tipoEntrega);
    }

    public List<Pedido> listarProximasEntregas(int limite) {
        return pedidoRepository.listarProximasEntregas(limite);
    }

    public Optional<Pedido> buscarPorId(Integer id) {
        Optional<Pedido> opt = pedidoRepository.findById(id);
        if (opt.isPresent()) {
            Pedido p = opt.get();
            p.setDetalles(detallePedidoRepository.listarPorPedido(id));
            if ("DELIVERY".equalsIgnoreCase(p.getTipoEntrega())) {
                p.setDelivery(deliveryRepository.findByPedidoId(id).orElse(null));
            }
        }
        return opt;
    }

    public int contarPendientes() {
        return pedidoRepository.contarPendientes();
    }

    public int contarHoy() {
        return pedidoRepository.contarHoy();
    }

    /**
     * Registra un nuevo pedido dentro de una transacción con validación de límite horario.
     */
    @Transactional
    public Integer registrar(Pedido p, List<DetallePedido> detalles, Delivery deliveryInfo) {
        validarReglasGenerales(p, detalles, null);

        // 1. Verificar límite concurrente de entregas por hora
        verificarCapacidadHoraria(p.getFechaEntrega(), p.getHoraEntrega(), null);

        // 2. Recalcular precios desde el catálogo de la BD
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido item : detalles) {
            Producto prod = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto ID " + item.getProductoId() + " no encontrado"));
            item.setPrecioUnitario(prod.getPrecio());
            BigDecimal subtotal = prod.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
        }
        p.setTotal(total);

        // 3. Insertar cabecera de pedido
        Integer pedidoId = pedidoRepository.insertar(p);

        // 4. Insertar detalles
        for (DetallePedido item : detalles) {
            item.setPedidoId(pedidoId);
            detallePedidoRepository.insertar(item);
        }

        // 5. Insertar entrega si es delivery
        if ("DELIVERY".equalsIgnoreCase(p.getTipoEntrega()) && deliveryInfo != null) {
            deliveryInfo.setPedidoId(pedidoId);
            deliveryRepository.insertar(deliveryInfo);
        }

        return pedidoId;
    }

    /**
     * Actualiza un pedido existente recalculando totales y verificando el límite de entrega.
     */
    @Transactional
    public void actualizar(Pedido p, List<DetallePedido> detalles, Delivery deliveryInfo) {
        if (p.getId() == null) {
            throw new IllegalArgumentException("El pedido a actualizar debe tener un ID.");
        }
        validarReglasGenerales(p, detalles, p.getId());

        verificarCapacidadHoraria(p.getFechaEntrega(), p.getHoraEntrega(), p.getId());

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido item : detalles) {
            Producto prod = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto ID " + item.getProductoId() + " no encontrado"));
            item.setPrecioUnitario(prod.getPrecio());
            BigDecimal subtotal = prod.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
        }
        p.setTotal(total);

        // Actualizar cabecera
        pedidoRepository.actualizar(p);
        pedidoRepository.actualizarTotal(p.getId(), total);

        // Reemplazar detalles
        detallePedidoRepository.eliminarPorPedido(p.getId());
        for (DetallePedido item : detalles) {
            item.setPedidoId(p.getId());
            detallePedidoRepository.insertar(item);
        }

        // Manejar delivery
        if ("DELIVERY".equalsIgnoreCase(p.getTipoEntrega()) && deliveryInfo != null) {
            deliveryInfo.setPedidoId(p.getId());
            if (deliveryRepository.findByPedidoId(p.getId()).isPresent()) {
                deliveryRepository.actualizar(deliveryInfo);
            } else {
                deliveryRepository.insertar(deliveryInfo);
            }
        }
    }

    /**
     * Cambia el estado del pedido y sincroniza con el delivery si corresponde.
     */
    @Transactional
    public void cambiarEstado(Integer pedidoId, String nuevoEstado) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Si se va a reactivar un pedido que estaba cancelado, revalidar cupo horario
        if ("CANCELADO".equalsIgnoreCase(p.getEstado()) && !"CANCELADO".equalsIgnoreCase(nuevoEstado)) {
            verificarCapacidadHoraria(p.getFechaEntrega(), p.getHoraEntrega(), p.getId());
        }

        pedidoRepository.actualizarEstado(pedidoId, nuevoEstado);

        // Sincronización con la tabla delivery
        if ("DELIVERY".equalsIgnoreCase(p.getTipoEntrega())) {
            if ("ENTREGADO".equalsIgnoreCase(nuevoEstado)) {
                deliveryRepository.actualizarEstado(pedidoId, "ENTREGADO");
            } else if ("CANCELADO".equalsIgnoreCase(nuevoEstado)) {
                deliveryRepository.actualizarEstado(pedidoId, "CANCELADO");
            }
        }
    }

    private void validarReglasGenerales(Pedido p, List<DetallePedido> detalles, Integer excluirId) {
        if (p.getClienteId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para el pedido.");
        }
        Cliente c = clienteRepository.findById(p.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente seleccionado no existe"));
        if (!c.isActivo()) {
            throw new IllegalArgumentException("El cliente seleccionado se encuentra inactivo.");
        }

        if (p.getFechaEntrega() == null) {
            throw new IllegalArgumentException("La fecha de entrega es obligatoria.");
        }
        if (p.getHoraEntrega() == null) {
            throw new IllegalArgumentException("La hora de entrega es obligatoria.");
        }
        if (p.getFechaEntrega().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de entrega no puede ser anterior a hoy.");
        }

        // Validar franja horaria permitida
        LocalTime apertura = LocalTime.parse(horaAperturaStr);
        LocalTime cierre = LocalTime.parse(horaCierreStr);
        if (p.getHoraEntrega().isBefore(apertura) || p.getHoraEntrega().isAfter(cierre)) {
            throw new IllegalArgumentException("El horario de entrega debe estar entre " + horaAperturaStr + " y " + horaCierreStr + ".");
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto.");
        }

        for (DetallePedido item : detalles) {
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a 0.");
            }
        }
    }

    /**
     * Regla central: máximo 5 entregas por hora (D2 aprobada: retiros + delivery).
     */
    public void verificarCapacidadHoraria(LocalDate fecha, LocalTime hora, Integer excluirPedidoId) {
        int horaInt = hora.getHour();
        int existentes = pedidoRepository.contarEntregasPorHora(fecha, horaInt, excluirPedidoId);
        if (existentes >= maxEntregasPorHora) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String mensaje = String.format("Ya hay %d entregas programadas para el %s entre %02d:00 y %02d:59. Elija otro horario.",
                    maxEntregasPorHora, fecha.format(fmt), horaInt, horaInt);
            throw new IllegalStateException(mensaje);
        }
    }
}
