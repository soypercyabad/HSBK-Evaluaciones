package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Bimestre;

import java.util.List;

public interface BimestreDao {
  Bimestre estadoBimestre(Integer numBimestre, Long periodoId) throws  Exception;
  List<Bimestre> listarBimestres(Long periodoId) throws  Exception;
}
