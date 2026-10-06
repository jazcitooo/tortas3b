package py.tortas3b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Producto;
import py.tortas3b.service.ProductoService;
import py.tortas3b.util.SecurityUtil;

/**
 * Catálogo de productos: tortas, cupcakes y chocolates artesanales.
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "categoria", required = false) String categoria, Model model) {
        model.addAttribute("productos", productoService.listar(categoria));
        model.addAttribute("categoriaActual", categoria);
        model.addAttribute("esAdmin", SecurityUtil.esAdmin());
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(Producto producto, RedirectAttributes flash) {
        try {
            productoService.registrar(producto);
            flash.addFlashAttribute("exito", "Producto '" + producto.getNombre() + "' registrado.");
            return "redirect:/productos";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("producto", producto);
            return "redirect:/productos/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return productoService.buscarPorId(id)
                .map(p -> {
                    model.addAttribute("producto", p);
                    return "productos/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Producto no encontrado.");
                    return "redirect:/productos";
                });
    }

    @PostMapping("/actualizar")
    public String actualizar(Producto producto, RedirectAttributes flash) {
        try {
            productoService.actualizar(producto);
            flash.addFlashAttribute("exito", "Producto actualizado correctamente.");
            return "redirect:/productos";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos/editar/" + producto.getId();
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable("id") Integer id,
                             @RequestParam("activo") boolean activo,
                             RedirectAttributes flash) {
        productoService.cambiarEstado(id, activo);
        flash.addFlashAttribute("exito", "Estado del producto actualizado.");
        return "redirect:/productos";
    }
}
