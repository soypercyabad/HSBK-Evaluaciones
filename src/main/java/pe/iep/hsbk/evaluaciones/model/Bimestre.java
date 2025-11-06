package pe.iep.hsbk.evaluaciones.model;

import java.time.LocalDate;

public class Bimestre {
  private Long id;
  private Long periodoId;
  private int numero; // 1..4
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private boolean abierto;

  public Bimestre() {
  }

  public Bimestre(Long id, Long periodoId, int numero, LocalDate fechaInicio, LocalDate fechaFin, boolean abierto) {
    this.id = id;
    this.periodoId = periodoId;
    this.numero = numero;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.abierto = abierto;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getPeriodoId() {
    return periodoId;
  }

  public void setPeriodoId(Long periodoId) {
    this.periodoId = periodoId;
  }

  public int getNumero() {
    return numero;
  }

  public void setNumero(int numero) {
    this.numero = numero;
  }

  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
  }

  public boolean isAbierto() {
    return abierto;
  }

  public void setAbierto(boolean abierto) {
    this.abierto = abierto;
  }

  @Override
  public String toString() {
    return "Bim " + numero;
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bimestre)) return false;
    Bimestre other = (Bimestre) o;
    return id != null && id.equals(other.id);
  }
}
