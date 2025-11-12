package pe.iep.hsbk.evaluaciones.dao;

public interface AsignacionDao {
  boolean usuarioTieneAsignacionEnNivel(long usuarioId, long nivelId, long periodoId) throws Exception;
}
