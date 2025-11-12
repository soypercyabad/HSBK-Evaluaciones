package pe.iep.hsbk.evaluaciones.service;

import org.mindrot.jbcrypt.BCrypt;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.dao.UsuarioDao;
import pe.iep.hsbk.evaluaciones.dao.impl.PeriodoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.UsuarioDaoImpl;
import pe.iep.hsbk.evaluaciones.model.Periodo;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.util.List;
import java.util.Optional;

public class AuthService {

  public static class UserSession {
    private final long userId;
    private final String username;
    private final String nombre;
    private final List<String> roles;
    private final long periodoId;
    private final String periodoNombre;

    public UserSession(long userId, String username, String nombre, List<String> roles, long periodoId, String periodoNombre) {
      this.userId = userId;
      this.username = username;
      this.nombre = nombre;
      this.roles = roles;
      this.periodoId = periodoId;
      this.periodoNombre = periodoNombre;
    }

    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public List<String> getRoles() { return roles; }
    public boolean hasRole(String r) { return roles.stream().anyMatch(x -> x.equalsIgnoreCase(r)); }
    public long getPeriodoId() { return periodoId; }
    public String getPeriodoNombre() { return periodoNombre; }
  }

  private final UsuarioDao usuarioDao = new UsuarioDaoImpl();
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();

  public UserSession login(String username, String plainPassword) throws Exception {
    // Usuario por SP
    Optional<Usuario> opt = usuarioDao.findByUsername(username);
    if (opt.isEmpty()) return null;

    Usuario u = opt.get();
    if (!u.isActivo()) return null;

    // Verifica BCrypt
    boolean ok = BCrypt.checkpw(plainPassword, u.getPasswordHash());
    if (!ok) return null;

    // Roles por SP
    List<String> roles = usuarioDao.findRolesByUserId(u.getId());

    // Periodo actual por SP
    Optional<Periodo> optPeriodo = periodoDao.getPeriodoActual();
    if (optPeriodo.isEmpty()) return null;
    Periodo p = optPeriodo.get();

    return new UserSession(u.getId(), u.getUsername(), u.getNombre(), roles, p.getId(), p.getNombre());
  }
}