package py.tortas3b.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Compra;
import py.tortas3b.model.DetalleCompra;
import py.tortas3b.service.CompraService;
import py.tortas3b.service.InventarioService;
import py.tortas3b.service.ProveedorService;
import py.tortas3b.util.SecurityUtil;

/**
 * Gestión de órdenes de compra y reposición de materias primas a proveedores.
 */
@Controller
@RequestMapping("/compras")
public class CompraController {

    private final CompraService compraService;
    private final ProveedorService proveedorService;
    private final InventarioService inventarioService;

    public CompraController(CompraService compraService,
                            ProveedorService proveedorService,
                            InventarioService inventarioService) {
        this.compraService = compraService;
        this.proveedorService = proveedorService;
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "estado", required = false) String estado, Model model) {
        model.addAttribute("compras", compraService.listar(estado));
        model.addAttribute("estadoFiltro", estado);
        return "compras/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("compra", new Compra());
        model.addAttribute("proveedores", proveedorService.listarActivos());
        model.addAttribute("ingredientes", inventarioService.listarActivos());
        return "compras/form";
    }

    @PostMapping("/guardar")
    public String guardar(Compra compra,
                          @RequestParam("ingredienteId") List<Integer> ingredienteIds,
                          @RequestParam("cantidad") List<BigDecimal> cantidades,
                          @RequestParam("costoUnitario") List<BigDecimal> costos,
                          RedirectAttributes flash) {
        try {
            compra.setUsuarioId(SecurityUtil.getUsuarioIdActual());

            List<DetalleCompra> detalles = new ArrayList<>();
            for (int i = 0; i < ingredienteIds.size(); i++) {
                Integer ingId = ingredienteIds.get(i);
                BigDecimal cant = cantidades.get(i);
                BigDecimal costo = costos.get(i);
                if (ingId != null && cant != null && cant.compareTo(BigDecimal.ZERO) > 0) {
                    DetalleCompra dc = new DetalleCompra();
                    dc.setIngredienteId(ingId);
                    dc.setCantidad(cant);
                    dc.setCostoUnitario(costo != null ? costo : BigDecimal.ZERO);
                    detalles.add(dc);
                }
            }

            Integer nuevaCompraId = compraService.registrar(compra, detalles);
            flash.addFlashAttribute("exito", "Orden de compra #" + nuevaCompraId + " creada en estado PENDIENTE.");
            return "redirect:/compras/detalle/" + nuevaCompraId;
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/compras/nuevo";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return compraService.buscarPorId(id)
                .map(c -> {
                    model.addAttribute("compra", c);
                    return "compras/detalle";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Compra no encontrada.");
                    return "redirect:/compras";
                });
    }

    @PostMapping("/recibir/{id}")
    public String recibir(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            Integer usuarioId = SecurityUtil.getUsuarioIdActual();
            compraService.recibirCompra(id, usuarioId);
            flash.addFlashAttribute("exito", "Compra #" + id + " marcada como RECIBIDA y stock incrementado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/compras/detalle/" + id;
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            compraService.cancelarCompra(id);
            flash.addFlashAttribute("exito", "Orden de compra #" + id + " cancelada.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/compras/detalle/" + id;
    }
}
