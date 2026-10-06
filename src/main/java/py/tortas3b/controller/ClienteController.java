package py.tortas3b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Cliente;
import py.tortas3b.service.ClienteService;
import py.tortas3b.util.SecurityUtil;

/**
 * Gestión de clientes y consulta de su historial de pedidos.
 */
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "busqueda", required = false) String busqueda, Model model) {
        model.addAttribute("clientes", clienteService.listar(busqueda));
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("esAdmin", SecurityUtil.esAdmin());
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(Cliente cliente, RedirectAttributes flash) {
        try {
            clienteService.registrar(cliente);
            flash.addFlashAttribute("exito", "Cliente '" + cliente.getNombre() + "' registrado con éxito.");
            return "redirect:/clientes";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return clienteService.buscarPorId(id)
                .map(c -> {
                    model.addAttribute("cliente", c);
                    return "clientes/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Cliente no encontrado.");
                    return "redirect:/clientes";
                });
    }

    @PostMapping("/actualizar")
    public String actualizar(Cliente cliente, RedirectAttributes flash) {
        try {
            clienteService.actualizar(cliente);
            flash.addFlashAttribute("exito", "Cliente actualizado exitosamente.");
            return "redirect:/clientes";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes/editar/" + cliente.getId();
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return clienteService.buscarPorId(id)
                .map(c -> {
                    model.addAttribute("cliente", c);
                    model.addAttribute("pedidos", clienteService.obtenerHistorialPedidos(id));
                    return "clientes/detalle";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Cliente no encontrado.");
                    return "redirect:/clientes";
                });
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable("id") Integer id,
                             @RequestParam("activo") boolean activo,
                             RedirectAttributes flash) {
        clienteService.cambiarEstado(id, activo);
        flash.addFlashAttribute("exito", "Estado del cliente actualizado.");
        return "redirect:/clientes";
    }
}
