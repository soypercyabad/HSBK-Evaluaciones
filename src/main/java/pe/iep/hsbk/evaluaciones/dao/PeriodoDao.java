package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Periodo;

import java.util.Optional;

public interface PeriodoDao {
  Optional<Periodo> getPeriodoActual();

  Long getPeriodoIdPorNombre(String perNombre);
}
