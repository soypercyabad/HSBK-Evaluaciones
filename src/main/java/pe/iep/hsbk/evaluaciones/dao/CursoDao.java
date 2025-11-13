package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Curso;

import java.util.List;

public interface CursoDao {
  List<Curso> listarCursosAsignados(Long periodoId, Long seccionId, Long usuarioId) throws Exception;
}
