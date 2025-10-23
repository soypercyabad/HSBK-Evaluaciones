package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Grado;

import java.util.List;

public interface GradoDao {

  List<Grado> listarGradosActivos(Long periodoId, Long nivelId) throws  Exception;
}
