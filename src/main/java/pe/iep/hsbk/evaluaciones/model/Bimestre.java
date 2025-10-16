package pe.iep.hsbk.evaluaciones.model;

import java.time.LocalDateTime;

public class Bimestre {
  private String id;
  private String nombre;
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaFin;
  private boolean abierto;

  public Bimestre() {
  }

  public Bimestre(String id, String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean abierto) {
    this.id = id;
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.abierto = abierto;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }

  public boolean isAbierto() {
    return abierto;
  }

  public void setAbierto(boolean abierto) {
    this.abierto = abierto;
  }
}
