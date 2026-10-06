package py.tortas3b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Proveedor;
import py.tortas3b.service.ProveedorService;

/**
 * Gestión de proveedores de materias primas e insumos (rol Administrador).
 */
@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", proveedorService.listar());
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/form";
    }

    @PostMapping("/guardar")
    public String guardar(Proveedor proveedor, RedirectAttributes flash) {
        try {
            proveedorService.registrar(proveedor);
            flash.addFlashAttribute("exito", "Proveedor '" + proveedor.getNombre() + "' registrado.");
            return "redirect:/proveedores";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("proveedor", proveedor);
            return "redirect:/proveedores/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return proveedorService.buscarPorId(id)
                .map(p -> {
                    model.addAttribute("proveedor", p);
                    return "proveedores/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Proveedor no encontrado.");
                    return "redirect:/proveedores";
                });
    }

    @PostMapping("/actualizar")
    public String actualizar(Proveedor proveedor, RedirectAttributes flash) {
        try {
            proveedorService.actualizar(proveedor);
            flash.addFlashAttribute("exito", "Proveedor actualizado correctamente.");
            return "redirect:/proveedores";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/proveedores/editar/" + proveedor.getId();
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable("id") Integer id,
                             @RequestParam("activo") boolean activo,
                             RedirectAttributes flash) {
        proveedorService.cambiarEstado(id, activo);
        flash.addFlashAttribute("exito", "Estado del proveedor actualizado.");
        return "redirect:/proveedores";
    }
}
