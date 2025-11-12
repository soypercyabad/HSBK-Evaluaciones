package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Seccion;

import java.util.List;

public interface SeccionDao {

  List<Seccion> listarSeccionesActivasPorUsuario(Long periodoId, Long gradoId, Long usuarioId) throws Exception;
}
