package pe.iep.hsbk.evaluaciones.model;

import java.time.LocalDateTime;

public class Conducta {
  private Long id;
  private Long matriculaId;
  private Long bimestreId;
  private Double nota;
  private String letra;
  private LocalDateTime fechaRegistro;
  private LocalDateTime fechaActualizacion;

  public Conducta() {
  }

  public Conducta(Long id, Long matriculaId, Long bimestreId, Double nota, String letra, LocalDateTime fechaRegistro, LocalDateTime fechaActualizacion) {
    this.id = id;
    this.matriculaId = matriculaId;
    this.bimestreId = bimestreId;
    this.nota = nota;
    this.letra = letra;
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

  public Double getNota() {
    return nota;
  }

  public void setNota(Double nota) {
    this.nota = nota;
  }

  public String getLetra() {
    return letra;
  }

  public void setLetra(String letra) {
    this.letra = letra;
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
    return "Conducta{id=" + id + "}";
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Conducta)) return false;
    Conducta other = (Conducta) o;
    return id != null && id.equals(other.id);
  }
}
