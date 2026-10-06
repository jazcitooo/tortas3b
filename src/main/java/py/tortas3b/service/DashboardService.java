package py.tortas3b.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import py.tortas3b.model.Pedido;
import py.tortas3b.repository.IngredienteRepository;
import py.tortas3b.repository.PedidoRepository;
import py.tortas3b.repository.ProductoRepository;
import py.tortas3b.repository.VentaRepository;

/**
 * Servicio para recopilar métricas consolidadas del dashboard y la API dinámica.
 */
@Service
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final VentaRepository ventaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;

    public DashboardService(PedidoRepository pedidoRepository,
                            VentaRepository ventaRepository,
                            IngredienteRepository ingredienteRepository,
                            ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.ventaRepository = ventaRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.productoRepository = productoRepository;
    }

    public Map<String, Object> obtenerMetricas() {
        Map<String, Object> metricas = new HashMap<>();

        int pedidosPendientes = pedidoRepository.contarPendientes();
        int pedidosHoy = pedidoRepository.contarHoy();
        BigDecimal ventasHoy = ventaRepository.obtenerTotalVentasHoy();
        int stockBajo = ingredienteRepository.contarStockBajo();
        int productosActivos = productoRepository.listarActivos().size();
        List<Pedido> proximasEntregas = pedidoRepository.listarProximasEntregas(8);

        metricas.put("pedidosPendientes", pedidosPendientes);
        metricas.put("pedidosHoy", pedidosHoy);
        metricas.put("ventasHoy", ventasHoy != null ? ventasHoy : BigDecimal.ZERO);
        metricas.put("stockBajo", stockBajo);
        metricas.put("productosActivos", productosActivos);
        metricas.put("proximasEntregas", proximasEntregas);

        return metricas;
    }
}
