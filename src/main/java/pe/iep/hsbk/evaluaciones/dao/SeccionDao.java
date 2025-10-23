package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Seccion;

import java.util.List;

public interface SeccionDao {

  List<Seccion> listarSeccionesActivas(Long periodoId, Long gradoId) throws Exception;
}
