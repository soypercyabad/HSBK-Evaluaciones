package pe.iep.hsbk.evaluaciones.service;

import pe.iep.hsbk.evaluaciones.dao.AsignacionDao;
import pe.iep.hsbk.evaluaciones.dao.impl.AsignacionDaoImpl;

public class AsignacionService {

  private static AsignacionService INSTANCE;

  public static AsignacionService getInstance() {
    if (INSTANCE == null) INSTANCE = new AsignacionService();
    return INSTANCE;
  }

  private final AsignacionDao asignacionDao = new AsignacionDaoImpl();

  public boolean tieneAsignacionEnNivel(long usuarioId, long nivelId, long periodoId) {
    try {
      return asignacionDao.usuarioTieneAsignacionEnNivel(usuarioId, nivelId, periodoId);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
