package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.*;

import java.util.List;

public interface NotasDao {
  NotasCursoResumenDto getNotasCursoResumen(long matriculaId, long cursoId, long bimestreId) throws Exception;
  void guardarNotasCurso(NotasCursoResumenDto notas) throws Exception;
  List<PromedioCursoBimestreDto> listarPromediosCursoBimestre(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception;
  List<PromedioAreaBimestreDto> listarPromediosAreaBimestre(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception;
  // Obtener data set?
}
