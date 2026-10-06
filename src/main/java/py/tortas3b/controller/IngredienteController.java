package py.tortas3b.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Ingrediente;
import py.tortas3b.service.InventarioService;
import py.tortas3b.service.ProveedorService;
import py.tortas3b.util.SecurityUtil;

/**
 * Control de inventario, stock de ingredientes y ajustes auditados (rol Administrador).
 */
@Controller
@RequestMapping("/ingredientes")
public class IngredienteController {

    private final InventarioService inventarioService;
    private final ProveedorService proveedorService;

    public IngredienteController(InventarioService inventarioService, ProveedorService proveedorService) {
        this.inventarioService = inventarioService;
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ingredientes", inventarioService.listarTodos());
        model.addAttribute("stockBajoCantidad", inventarioService.contarStockBajo());
        return "ingredientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("ingrediente", new Ingrediente());
        model.addAttribute("proveedores", proveedorService.listarActivos());
        return "ingredientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(Ingrediente ingrediente, RedirectAttributes flash) {
        try {
            inventarioService.registrar(ingrediente);
            flash.addFlashAttribute("exito", "Ingrediente '" + ingrediente.getNombre() + "' registrado.");
            return "redirect:/ingredientes";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("ingrediente", ingrediente);
            return "redirect:/ingredientes/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return inventarioService.buscarPorId(id)
                .map(i -> {
                    model.addAttribute("ingrediente", i);
                    model.addAttribute("proveedores", proveedorService.listarActivos());
                    return "ingredientes/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Ingrediente no encontrado.");
                    return "redirect:/ingredientes";
                });
    }

    @PostMapping("/actualizar")
    public String actualizar(Ingrediente ingrediente, RedirectAttributes flash) {
        try {
            inventarioService.actualizar(ingrediente);
            flash.addFlashAttribute("exito", "Ingrediente actualizado con éxito.");
            return "redirect:/ingredientes";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingredientes/editar/" + ingrediente.getId();
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable("id") Integer id,
                             @RequestParam("activo") boolean activo,
                             RedirectAttributes flash) {
        inventarioService.cambiarEstado(id, activo);
        flash.addFlashAttribute("exito", "Estado del ingrediente modificado.");
        return "redirect:/ingredientes";
    }

    @GetMapping("/ajuste/{id}")
    public String formAjuste(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return inventarioService.buscarPorId(id)
                .map(i -> {
                    model.addAttribute("ingrediente", i);
                    return "ingredientes/ajuste";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Ingrediente no encontrado.");
                    return "redirect:/ingredientes";
                });
    }

    @PostMapping("/ajustar")
    public String realizarAjuste(@RequestParam("ingredienteId") Integer ingredienteId,
                                 @RequestParam("cantidad") BigDecimal cantidad,
                                 @RequestParam(value = "motivo", required = false) String motivo,
                                 RedirectAttributes flash) {
        try {
            Integer usuarioId = SecurityUtil.getUsuarioIdActual();
            inventarioService.ajustarStock(ingredienteId, usuarioId, cantidad, motivo);
            flash.addFlashAttribute("exito", "Ajuste de inventario registrado correctamente.");
            return "redirect:/ingredientes";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingredientes/ajuste/" + ingredienteId;
        }
    }

    @GetMapping("/movimientos")
    public String historialMovimientos(Model model) {
        model.addAttribute("movimientos", inventarioService.listarMovimientosRecientes(100));
        return "ingredientes/movimientos";
    }
}
