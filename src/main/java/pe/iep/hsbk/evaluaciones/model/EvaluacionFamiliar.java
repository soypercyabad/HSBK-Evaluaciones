package pe.iep.hsbk.evaluaciones.model;

import java.time.LocalDateTime;

public class EvaluacionFamiliar {
  private Long id;
  private Long matriculaId;
  private Long bimestreId;
  private String utiles;
  private String participacion;
  private String reuniones;
  private String escuelaPadres;
  private String comentarios;
  private LocalDateTime fechaRegistro;
  private LocalDateTime fechaActualizacion;

  public EvaluacionFamiliar() {
  }

  public EvaluacionFamiliar(Long id, Long matriculaId, Long bimestreId, String utiles, String participacion,
                            String reuniones, String escuelaPadres, String comentarios,
                            LocalDateTime fechaRegistro, LocalDateTime fechaActualizacion) {
    this.id = id;
    this.matriculaId = matriculaId;
    this.bimestreId = bimestreId;
    this.utiles = utiles;
    this.participacion = participacion;
    this.reuniones = reuniones;
    this.escuelaPadres = escuelaPadres;
    this.comentarios = comentarios;
    this.fechaRegistro = fechaRegistro;
    this.fechaActualizacion = fechaActualizacion;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMatriculaId() {
    return matriculaId;
  }

  public void setMatriculaId(Long matriculaId) {
    this.matriculaId = matriculaId;
  }

  public Long getBimestreId() {
    return bimestreId;
  }

  public void setBimestreId(Long bimestreId) {
    this.bimestreId = bimestreId;
  }

  public String getUtiles() {
    return utiles;
  }

  public void setUtiles(String utiles) {
    this.utiles = utiles;
  }

  public String getParticipacion() {
    return participacion;
  }

  public void setParticipacion(String participacion) {
    this.participacion = participacion;
  }

  public String getReuniones() {
    return reuniones;
  }

  public void setReuniones(String reuniones) {
    this.reuniones = reuniones;
  }

  public String getEscuelaPadres() {
    return escuelaPadres;
  }

  public void setEscuelaPadres(String escuelaPadres) {
    this.escuelaPadres = escuelaPadres;
  }

  public String getComentarios() {
    return comentarios;
  }

  public void setComentarios(String comentarios) {
    this.comentarios = comentarios;
  }

  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public LocalDateTime getFechaActualizacion() {
    return fechaActualizacion;
  }

  public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
    this.fechaActualizacion = fechaActualizacion;
  }

  @Override
  public String toString() {
    return "EvaluacionFamiliar{id=" + id + "}";
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EvaluacionFamiliar)) return false;
    EvaluacionFamiliar other = (EvaluacionFamiliar) o;
    return id != null && id.equals(other.id);
  }
}
