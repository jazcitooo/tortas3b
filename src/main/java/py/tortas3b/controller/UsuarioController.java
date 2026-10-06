package py.tortas3b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.model.Usuario;
import py.tortas3b.service.UsuarioService;

/**
 * Mantenimiento de usuarios y asignación de roles (acceso exclusivo Administrador).
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(Usuario usuario,
                          @RequestParam("password") String password,
                          RedirectAttributes flash) {
        try {
            usuarioService.registrar(usuario, password);
            flash.addFlashAttribute("exito", "Usuario '" + usuario.getUsername() + "' registrado correctamente.");
            return "redirect:/usuarios";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            flash.addFlashAttribute("usuario", usuario);
            return "redirect:/usuarios/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
        return usuarioService.buscarPorId(id)
                .map(u -> {
                    model.addAttribute("usuario", u);
                    return "usuarios/form";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Usuario no encontrado.");
                    return "redirect:/usuarios";
                });
    }

    @PostMapping("/actualizar")
    public String actualizar(Usuario usuario,
                             @RequestParam(value = "password", required = false) String password,
                             RedirectAttributes flash) {
        try {
            usuarioService.actualizar(usuario, password);
            flash.addFlashAttribute("exito", "Usuario actualizado exitosamente.");
            return "redirect:/usuarios";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/editar/" + usuario.getId();
        }
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable("id") Integer id,
                                @RequestParam("activo") boolean activo,
                                RedirectAttributes flash) {
        usuarioService.cambiarEstado(id, activo);
        flash.addFlashAttribute("exito", "Estado del usuario modificado correctamente.");
        return "redirect:/usuarios";
    }
}
