package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.ConductaResumen;
import pe.iep.hsbk.evaluaciones.model.Conducta;
import pe.iep.hsbk.evaluaciones.model.EvaluacionFamiliar;
import pe.iep.hsbk.evaluaciones.model.RecomendacionCatalogo;

import java.util.List;

public interface ConductaPanelDao {
    ConductaResumen getResumenByAlumno(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception;

}
