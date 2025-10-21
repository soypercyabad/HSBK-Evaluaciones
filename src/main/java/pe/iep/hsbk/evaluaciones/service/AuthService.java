package pe.iep.hsbk.evaluaciones.service;

import org.mindrot.jbcrypt.BCrypt;
import pe.iep.hsbk.evaluaciones.repository.UsuarioRepository;

public class AuthService {
  private final UsuarioRepository repo = new UsuarioRepository();

  public boolean loginValido(String username, String plainPassword) {
    try {
      var u = repo.findByUsername(username);
      if (u == null) return false;
      if (!"ACTIVO".equalsIgnoreCase(u.estado)) return false;
      return BCrypt.checkpw(plainPassword, u.passwordHash);
    } catch (Exception e) {
      throw new RuntimeException("Error autenticando", e);
    }
  }
}
