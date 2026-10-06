package py.tortas3b.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import py.tortas3b.model.Cliente;
import py.tortas3b.model.DetallePedido;
import py.tortas3b.model.Pedido;
import py.tortas3b.model.Producto;
import py.tortas3b.repository.ClienteRepository;
import py.tortas3b.repository.DeliveryRepository;
import py.tortas3b.repository.DetallePedidoRepository;
import py.tortas3b.repository.PedidoRepository;
import py.tortas3b.repository.ProductoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pedidoService, "maxEntregasPorHora", 5);
        ReflectionTestUtils.setField(pedidoService, "horaAperturaStr", "08:00");
        ReflectionTestUtils.setField(pedidoService, "horaCierreStr", "20:00");
    }

    @Test
    @DisplayName("Regla de negocio: rechazar pedido cuando ya existen 5 entregas en la misma hora")
    void debeRechazarCuandoSeSuperaLimiteCincoEntregasPorHora() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(14, 30);

        when(pedidoRepository.contarEntregasPorHora(eq(fecha), eq(14), isNull()))
                .thenReturn(5);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                pedidoService.verificarCapacidadHoraria(fecha, hora, null));

        assertTrue(ex.getMessage().contains("Ya hay 5 entregas programadas"));
        assertTrue(ex.getMessage().contains("14:00 y 14:59"));
    }

    @Test
    @DisplayName("Regla de negocio: permitir pedido cuando hay menos de 5 entregas")
    void debePermitirCuandoHayMenosDeCincoEntregas() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(15, 0);

        when(pedidoRepository.contarEntregasPorHora(eq(fecha), eq(15), isNull()))
                .thenReturn(4);

        assertDoesNotThrow(() -> pedidoService.verificarCapacidadHoraria(fecha, hora, null));
    }

    @Test
    @DisplayName("Cálculo seguro: recalcula subtotales y total con precios de la base de datos")
    void debeRecalcularTotalesDesdeBaseDeDatos() {
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 0);

        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setActivo(true);
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));

        Producto p1 = new Producto();
        p1.setId(10);
        p1.setPrecio(BigDecimal.valueOf(120000));
        when(productoRepository.findById(10)).thenReturn(Optional.of(p1));

        Producto p2 = new Producto();
        p2.setId(20);
        p2.setPrecio(BigDecimal.valueOf(15000));
        when(productoRepository.findById(20)).thenReturn(Optional.of(p2));

        when(pedidoRepository.contarEntregasPorHora(eq(fecha), eq(10), isNull()))
                .thenReturn(1);
        when(pedidoRepository.insertar(any(Pedido.class))).thenReturn(100);

        Pedido pedido = new Pedido();
        pedido.setClienteId(1);
        pedido.setFechaEntrega(fecha);
        pedido.setHoraEntrega(hora);
        pedido.setTipoEntrega("RETIRO");

        DetallePedido item1 = new DetallePedido();
        item1.setProductoId(10);
        item1.setCantidad(2); // 2 x 120.000 = 240.000

        DetallePedido item2 = new DetallePedido();
        item2.setProductoId(20);
        item2.setCantidad(4); // 4 x 15.000 = 60.000

        Integer idGenerado = pedidoService.registrar(pedido, List.of(item1, item2), null);

        assertEquals(100, idGenerado);
        assertEquals(BigDecimal.valueOf(300000), pedido.getTotal());
        assertEquals(BigDecimal.valueOf(240000), item1.getSubtotal());
        assertEquals(BigDecimal.valueOf(60000), item2.getSubtotal());
    }
}
