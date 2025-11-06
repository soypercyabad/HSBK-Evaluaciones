package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Bimestre;

import java.util.List;

public interface BimestreDao {

  List<Bimestre> listarbimestres(Long periodoId) throws  Exception;
}
