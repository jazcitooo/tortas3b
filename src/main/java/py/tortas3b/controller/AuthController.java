package py.tortas3b.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Rutas de autenticación: pantalla de login y acceso denegado (403).
 */
@Controller
public class AuthController {

    @Value("${app.mostrar-usuarios-demo:true}")
    private boolean mostrarUsuariosDemo;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos. Verifique sus credenciales.");
        }
        if (logout != null) {
            model.addAttribute("exito", "Ha cerrado la sesión correctamente.");
        }
        model.addAttribute("mostrarUsuariosDemo", mostrarUsuariosDemo);

        return "login";
    }

    @GetMapping("/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
