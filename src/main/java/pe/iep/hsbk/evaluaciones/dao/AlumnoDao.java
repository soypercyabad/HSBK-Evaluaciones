package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Alumno;

import java.util.List;

public interface AlumnoDao {
  List<Alumno> listarPorSeccionPeriodo(int seccionId, int periodoId) throws Exception;
}
