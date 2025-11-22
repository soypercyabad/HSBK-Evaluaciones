package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.BoletaAlumnoDatasetDto;

import java.util.List;
import java.util.Map;

public interface BoletaDatasetDao {
  Map<Long, BoletaAlumnoDatasetDto> obtenerDatasetBoleta(
      long periodoId,
      long seccionId,
      int bimestreNum,
      List<Long> alumnosIds
  ) throws Exception;
}
