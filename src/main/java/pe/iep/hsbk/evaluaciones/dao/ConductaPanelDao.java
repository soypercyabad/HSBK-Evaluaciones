package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.ConductaResumenDto;

public interface ConductaPanelDao {
  ConductaResumenDto getResumenByAlumno(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception;

  void saveConductaBimestre(
      Long alumnoId,
      Long periodoId,
      Long nivelId,
      Long bimestreId,
      java.math.BigDecimal notaConducta,   // DECIMAL(5,2)
      String utiles,
      String participacion,
      String reuniones,
      String escuelaPadres,
      String comentarios,
      Long recomendacionId,                // puede ser null
      String mensajePersonal,              // puede ser null
      Long usuarioId
  ) throws Exception;

}
