package pe.iep.hsbk.evaluaciones.dto;

import pe.iep.hsbk.evaluaciones.model.Conducta;
import pe.iep.hsbk.evaluaciones.model.EvaluacionFamiliar;
import pe.iep.hsbk.evaluaciones.model.RecomendacionCatalogo;

import java.util.ArrayList;
import java.util.List;

public class ConductaResumenDto {
  private Conducta conducta; // puede ser null si no hay registro
  private EvaluacionFamiliar evaluacionFamiliar; // idem
  private List<RecomendacionCatalogo> recomendaciones; // nunca null
  private List<RecomendacionAlumnoDto> recomendacionesAlumno;

  public Conducta getConducta() {
    return conducta;
  }

  public void setConducta(Conducta c) {
    this.conducta = c;
  }

  public EvaluacionFamiliar getEvaluacionFamiliar() {
    return evaluacionFamiliar;
  }

  public void setEvaluacionFamiliar(EvaluacionFamiliar e) {
    this.evaluacionFamiliar = e;
  }

  public List<RecomendacionCatalogo> getRecomendaciones() {
    return recomendaciones;
  }

  public void setRecomendaciones(List<RecomendacionCatalogo> recs) {
    this.recomendaciones = recs;
  }

  public List<RecomendacionAlumnoDto> getRecomendacionesAlumno() {
    return recomendacionesAlumno;
  }

  public void setRecomendacionesAlumno(List<RecomendacionAlumnoDto> recomendacionesAlumno) {
    this.recomendacionesAlumno = recomendacionesAlumno != null ? recomendacionesAlumno : new ArrayList<>();
  }

  // ===== Helpers =====
  public boolean tieneRecomendacionAlumno() {
    return recomendacionesAlumno != null && !recomendacionesAlumno.isEmpty();
  }

  public RecomendacionAlumnoDto getPrimeraRecomendacionAlumno() {
    return (tieneRecomendacionAlumno()) ? recomendacionesAlumno.get(0) : null;
  }


}
