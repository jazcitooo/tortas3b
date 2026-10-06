package py.tortas3b.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Pedido;
import py.tortas3b.service.PedidoService;
import py.tortas3b.service.VentaService;
import py.tortas3b.util.SecurityUtil;

/**
 * Gestión de caja, cobro de pedidos y arqueo diario de ventas.
 */
@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final PedidoService pedidoService;

    public VentaController(VentaService ventaService, PedidoService pedidoService) {
        this.ventaService = ventaService;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "fechaDesde", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                         @RequestParam(value = "fechaHasta", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                         Model model) {
        if (fechaDesde == null && fechaHasta == null) {
            fechaDesde = LocalDate.now();
            fechaHasta = LocalDate.now();
        }
        model.addAttribute("ventas", ventaService.listar(fechaDesde, fechaHasta));
        model.addAttribute("totalHoy", ventaService.totalVentasHoy());
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("esAdmin", SecurityUtil.esAdmin());
        return "ventas/lista";
    }

    @GetMapping("/nuevo/{pedidoId}")
    public String nuevo(@PathVariable("pedidoId") Integer pedidoId, Model model, RedirectAttributes flash) {
        return pedidoService.buscarPorId(pedidoId)
                .map(p -> {
                    if (ventaService.buscarPorPedidoId(pedidoId).isPresent()) {
                        flash.addFlashAttribute("error", "Este pedido ya tiene un cobro registrado.");
                        return "redirect:/pedidos/detalle/" + pedidoId;
                    }
                    model.addAttribute("pedido", p);
                    return "ventas/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Pedido no encontrado.");
                    return "redirect:/pedidos";
                });
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam("pedidoId") Integer pedidoId,
                          @RequestParam("metodoPago") String metodoPago,
                          RedirectAttributes flash) {
        try {
            Integer usuarioId = SecurityUtil.getUsuarioIdActual();
            ventaService.registrarVenta(pedidoId, usuarioId, metodoPago);
            flash.addFlashAttribute("exito", "Cobro del pedido #" + pedidoId + " registrado exitosamente en caja.");
            return "redirect:/ventas";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/nuevo/" + pedidoId;
        }
    }

    @PostMapping("/anular/{id}")
    public String anular(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            ventaService.anularVenta(id);
            flash.addFlashAttribute("exito", "Venta #" + id + " anulada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }
}
