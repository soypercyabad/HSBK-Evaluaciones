package pe.iep.hsbk.evaluaciones.dto;

public class RolesEnSeccionDto {
  private final boolean esDocente;
  private final boolean esTutor;

  public RolesEnSeccionDto(boolean esDocente, boolean esTutor) {
    this.esDocente = esDocente;
    this.esTutor = esTutor;
  }

  public boolean isDocente() {
    return esDocente;
  }

  public boolean isTutor() {
    return esTutor;
  }
}
