package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.*;

public interface NotasDao {
  NotasCursoResumenDto getNotasCursoResumen(long matriculaId, long cursoId, long bimestreId) throws Exception;
}
