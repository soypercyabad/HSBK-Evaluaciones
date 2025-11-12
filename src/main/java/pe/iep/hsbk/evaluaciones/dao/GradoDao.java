package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Grado;

import java.util.List;

public interface GradoDao {

  List<Grado> listarGradosActivosPorUsuario(Long periodoId, Long nivelId, Long usuarioId) throws  Exception;
}
