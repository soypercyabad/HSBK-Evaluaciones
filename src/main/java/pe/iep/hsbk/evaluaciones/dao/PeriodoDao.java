package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Periodo;

import java.util.Optional;

public interface PeriodoDao {
  Optional<Periodo> getPeriodoActual() throws Exception;
  Long getPeriodoIdPorNombre(String perNombre) throws Exception;
}
