package pe.iep.hsbk.evaluaciones.util;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;

public interface SesionAware {
  // Establece la sesión de usuario
  void setSession(UserSession userSession);
}
