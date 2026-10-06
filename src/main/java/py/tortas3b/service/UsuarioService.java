package py.tortas3b.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import py.tortas3b.model.Usuario;
import py.tortas3b.repository.UsuarioRepository;

/**
 * Servicio para la gestión de usuarios y encriptación de credenciales con BCrypt.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.listarTodos();
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public void registrar(Usuario u, String passwordPlano) {
        if (usuarioRepository.existeUsername(u.getUsername(), null)) {
            throw new IllegalArgumentException("El nombre de usuario '" + u.getUsername() + "' ya existe.");
        }
        if (passwordPlano == null || passwordPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        u.setPasswordHash(passwordEncoder.encode(passwordPlano));
        usuarioRepository.insertar(u);
    }

    public void actualizar(Usuario u, String nuevoPasswordPlano) {
        if (usuarioRepository.existeUsername(u.getUsername(), u.getId())) {
            throw new IllegalArgumentException("El nombre de usuario '" + u.getUsername() + "' ya está en uso.");
        }
        usuarioRepository.actualizar(u);
        if (nuevoPasswordPlano != null && !nuevoPasswordPlano.trim().isEmpty()) {
            usuarioRepository.actualizarPassword(u.getId(), passwordEncoder.encode(nuevoPasswordPlano));
        }
    }

    public void cambiarEstado(Integer id, boolean activo) {
        usuarioRepository.cambiarEstado(id, activo);
    }
}
