package pe.iep.hsbk.evaluaciones.model;

import java.time.LocalDateTime;

public class Nota {
  private Long id;
  private Long matriculaId;
  private Long cursoId;
  private Long bimestreId;
  private Long evaluacionTipoId;
  private Double calificacion;     // DECIMAL(5,2)
  private String letra;            // AD/A/B/C/00
  private LocalDateTime fechaRegistro;
  private LocalDateTime fechaActualizacion;

  public Nota() {
  }

  public Nota(Long id, Long matriculaId, Long cursoId,
              Long bimestreId, Long evaluacionTipoId,
              Double calificacion, String letra,
              LocalDateTime fechaRegistro,
              LocalDateTime fechaActualizacion) {
    this.id = id;
    this.matriculaId = matriculaId;
    this.cursoId = cursoId;
    this.bimestreId = bimestreId;
    this.evaluacionTipoId = evaluacionTipoId;
    this.calificacion = calificacion;
    this.letra = letra;
    this.fechaRegistro = fechaRegistro;
    this.fechaActualizacion = fechaActualizacion;
  }

  public Long getId() {return id;}

  public void setId(Long id) {this.id = id;}

  public Long getMatriculaId() {return matriculaId;}

  public void setMatriculaId(Long matriculaId) {this.matriculaId = matriculaId;}

  public Long getCursoId() {return cursoId;}

  public void setCursoId(Long cursoId) {this.cursoId = cursoId;}

  public Long getBimestreId() {return bimestreId;}

  public void setBimestreId(Long bimestreId) {this.bimestreId = bimestreId;}

  public Long getEvaluacionTipoId() {return evaluacionTipoId;}

  public void setEvaluacionTipoId(Long evaluacionTipoId) {this.evaluacionTipoId = evaluacionTipoId;}

  public Double getCalificacion() {return calificacion;}

  public void setCalificacion(Double calificacion) {this.calificacion = calificacion;}

  public String getLetra() {return letra;}

  public void setLetra(String letra) {this.letra = letra;}

  public LocalDateTime getFechaRegistro() {return fechaRegistro;}

  public void setFechaRegistro(LocalDateTime fechaRegistro) {this.fechaRegistro = fechaRegistro;}

  public LocalDateTime getFechaActualizacion() {return fechaActualizacion;}

  public void setFechaActualizacion(LocalDateTime fechaActualizacion) {this.fechaActualizacion = fechaActualizacion;}

  @Override
  public String toString() {return "Nota{id=" + id + "}";}
}
