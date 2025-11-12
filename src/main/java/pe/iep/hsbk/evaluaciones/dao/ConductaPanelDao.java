package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.dto.ConductaResumen;
import pe.iep.hsbk.evaluaciones.dto.RecomendacionAlumnoDto;
import pe.iep.hsbk.evaluaciones.model.Conducta;
import pe.iep.hsbk.evaluaciones.model.EvaluacionFamiliar;
import pe.iep.hsbk.evaluaciones.model.RecomendacionCatalogo;

import java.util.List;

public interface ConductaPanelDao {
    ConductaResumen getResumenByAlumno(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception;

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
