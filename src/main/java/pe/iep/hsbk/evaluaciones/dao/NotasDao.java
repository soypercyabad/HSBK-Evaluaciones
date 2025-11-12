package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.*;

public interface NotasDao {
  PracticasDto getPracticas(long matriculaId, long cursoId, long bimestreId) throws Exception;
  TareasDto    getTareas   (long matriculaId, long cursoId, long bimestreId) throws Exception;
  ExamenesDto  getExamenes (long matriculaId, long cursoId, long bimestreId) throws Exception;
  PromedioDto  getPromedio (long matriculaId, long cursoId, long bimestreId) throws Exception;
}
