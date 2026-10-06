package py.tortas3b.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Delivery;
import py.tortas3b.model.DetallePedido;
import py.tortas3b.model.Pedido;
import py.tortas3b.service.ClienteService;
import py.tortas3b.service.PedidoService;
import py.tortas3b.service.ProductoService;
import py.tortas3b.service.VentaService;
import py.tortas3b.util.SecurityUtil;

/**
 * Gestión integral de pedidos, detalle de ítems y control de entregas por hora.
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final VentaService ventaService;

    public PedidoController(PedidoService pedidoService,
                            ClienteService clienteService,
                            ProductoService productoService,
                            VentaService ventaService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.ventaService = ventaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "fecha", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(value = "estado", required = false) String estado,
                         @RequestParam(value = "tipoEntrega", required = false) String tipoEntrega,
                         Model model) {
        model.addAttribute("pedidos", pedidoService.listar(fecha, estado, tipoEntrega));
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("tipoEntregaFiltro", tipoEntrega);
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Pedido pedido = new Pedido();
        pedido.setFechaEntrega(LocalDate.now());
        pedido.setHoraEntrega(LocalTime.of(10, 0));
        pedido.setTipoEntrega("RETIRO");

        model.addAttribute("pedido", pedido);
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("productos", productoService.listarActivos());
        return "pedidos/form";
    }

    @PostMapping("/guardar")
    public String guardar(Pedido pedido,
                          @RequestParam("productoId") List<Integer> productoIds,
                          @RequestParam("cantidad") List<Integer> cantidades,
                          @RequestParam(value = "deliveryDireccion", required = false) String deliveryDireccion,
                          @RequestParam(value = "deliveryTelefono", required = false) String deliveryTelefono,
                          @RequestParam(value = "deliveryObservaciones", required = false) String deliveryObservaciones,
                          RedirectAttributes flash) {
        try {
            pedido.setUsuarioId(SecurityUtil.getUsuarioIdActual());

            List<DetallePedido> detalles = new ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                Integer prodId = productoIds.get(i);
                Integer cant = cantidades.get(i);
                if (prodId != null && cant != null && cant > 0) {
                    DetallePedido dp = new DetallePedido();
                    dp.setProductoId(prodId);
                    dp.setCantidad(cant);
                    detalles.add(dp);
                }
            }

            Delivery deliv = null;
            if ("DELIVERY".equalsIgnoreCase(pedido.getTipoEntrega())) {
                deliv = new Delivery();
                deliv.setDireccion(deliveryDireccion != null ? deliveryDireccion.trim() : "");
                deliv.setTelefonoContacto(deliveryTelefono != null ? deliveryTelefono.trim() : "");
                deliv.setObservaciones(deliveryObservaciones);
            }

            Integer nuevoId = pedidoService.registrar(pedido, detalles, deliv);
            flash.addFlashAttribute("exito", "Pedido #" + nuevoId + " registrado con éxito.");
            return "redirect:/pedidos/detalle/" + nuevoId;
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return pedidoService.buscarPorId(id)
                .map(p -> {
                    model.addAttribute("pedido", p);
                    model.addAttribute("clientes", clienteService.listarActivos());
                    model.addAttribute("productos", productoService.listarActivos());
                    return "pedidos/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Pedido no encontrado.");
                    return "redirect:/pedidos";
                });
    }

    @PostMapping("/actualizar")
    public String actualizar(Pedido pedido,
                             @RequestParam("productoId") List<Integer> productoIds,
                             @RequestParam("cantidad") List<Integer> cantidades,
                             @RequestParam(value = "deliveryDireccion", required = false) String deliveryDireccion,
                             @RequestParam(value = "deliveryTelefono", required = false) String deliveryTelefono,
                             @RequestParam(value = "deliveryObservaciones", required = false) String deliveryObservaciones,
                             RedirectAttributes flash) {
        try {
            List<DetallePedido> detalles = new ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                Integer prodId = productoIds.get(i);
                Integer cant = cantidades.get(i);
                if (prodId != null && cant != null && cant > 0) {
                    DetallePedido dp = new DetallePedido();
                    dp.setProductoId(prodId);
                    dp.setCantidad(cant);
                    detalles.add(dp);
                }
            }

            Delivery deliv = null;
            if ("DELIVERY".equalsIgnoreCase(pedido.getTipoEntrega())) {
                deliv = new Delivery();
                deliv.setDireccion(deliveryDireccion != null ? deliveryDireccion.trim() : "");
                deliv.setTelefonoContacto(deliveryTelefono != null ? deliveryTelefono.trim() : "");
                deliv.setObservaciones(deliveryObservaciones);
            }

            pedidoService.actualizar(pedido, detalles, deliv);
            flash.addFlashAttribute("exito", "Pedido #" + pedido.getId() + " actualizado correctamente.");
            return "redirect:/pedidos/detalle/" + pedido.getId();
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/editar/" + pedido.getId();
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return pedidoService.buscarPorId(id)
                .map(p -> {
                    model.addAttribute("pedido", p);
                    model.addAttribute("venta", ventaService.buscarPorPedidoId(id).orElse(null));
                    model.addAttribute("esAdmin", SecurityUtil.esAdmin());
                    return "pedidos/detalle";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Pedido no encontrado.");
                    return "redirect:/pedidos";
                });
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable("id") Integer id,
                                @RequestParam("estado") String nuevoEstado,
                                RedirectAttributes flash) {
        try {
            pedidoService.cambiarEstado(id, nuevoEstado);
            flash.addFlashAttribute("exito", "Estado del pedido actualizado a " + nuevoEstado + ".");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/detalle/" + id;
    }
}
