package py.tortas3b.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import py.tortas3b.config.UsuarioPrincipal;
import py.tortas3b.model.Usuario;

/**
 * Métodos auxiliares para obtener el usuario autenticado actualmente.
 */
public final class SecurityUtil {

    private SecurityUtil() {}

    public static Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }

    public static Integer getUsuarioIdActual() {
        Usuario u = getUsuarioActual();
        return u != null ? u.getId() : 1; // 1 = admin por defecto si no hay sesión activa
    }

    public static String getNombreActual() {
        Usuario u = getUsuarioActual();
        return u != null ? u.getNombre() : "Usuario";
    }

    public static String getRolActual() {
        Usuario u = getUsuarioActual();
        return u != null ? u.getRol() : "OPERADOR";
    }

    public static boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(getRolActual());
    }
}
