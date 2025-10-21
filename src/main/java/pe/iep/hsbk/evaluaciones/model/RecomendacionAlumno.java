package pe.iep.hsbk.evaluaciones.model;

import java.time.LocalDateTime;

public class RecomendacionAlumno {
  private Long id;
  private Long matriculaId;
  private Long bimestreId;
  private Long recomendacionId;
  private String mensajePersonal;
  private Long usuarioId;
  private LocalDateTime fechaRegistro;
  private LocalDateTime fechaActualizacion;

  public RecomendacionAlumno() {
  }

  public RecomendacionAlumno(Long id, Long matriculaId, Long bimestreId, Long recomendacionId,
                             String mensajePersonal, Long usuarioId,
                             LocalDateTime fechaRegistro, LocalDateTime fechaActualizacion) {
    this.id = id;
    this.matriculaId = matriculaId;
    this.bimestreId = bimestreId;
    this.recomendacionId = recomendacionId;
    this.mensajePersonal = mensajePersonal;
    this.usuarioId = usuarioId;
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

  public Long getRecomendacionId() {
    return recomendacionId;
  }

  public void setRecomendacionId(Long recomendacionId) {
    this.recomendacionId = recomendacionId;
  }

  public String getMensajePersonal() {
    return mensajePersonal;
  }

  public void setMensajePersonal(String mensajePersonal) {
    this.mensajePersonal = mensajePersonal;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
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
    return "RecomendacionAlumno{id=" + id + "}";
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RecomendacionAlumno)) return false;
    RecomendacionAlumno other = (RecomendacionAlumno) o;
    return id != null && id.equals(other.id);
  }
}
