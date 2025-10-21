package pe.iep.hsbk.evaluaciones.model;

public class Matricula {
  private Long id;
  private Long alumnoId;
  private Long seccionId;
  private Long periodoId;
  private Integer numeroOrden;
  private boolean activo;

  public Matricula() {
  }

  public Matricula(Long id, Long alumnoId, Long seccionId, Long periodoId, Integer numeroOrden, boolean activo) {
    this.id = id;
    this.alumnoId = alumnoId;
    this.seccionId = seccionId;
    this.periodoId = periodoId;
    this.numeroOrden = numeroOrden;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAlumnoId() {
    return alumnoId;
  }

  public void setAlumnoId(Long alumnoId) {
    this.alumnoId = alumnoId;
  }

  public Long getSeccionId() {
    return seccionId;
  }

  public void setSeccionId(Long seccionId) {
    this.seccionId = seccionId;
  }

  public Long getPeriodoId() {
    return periodoId;
  }

  public void setPeriodoId(Long periodoId) {
    this.periodoId = periodoId;
  }

  public Integer getNumeroOrden() {
    return numeroOrden;
  }

  public void setNumeroOrden(Integer numeroOrden) {
    this.numeroOrden = numeroOrden;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @Override
  public String toString() {
    return "Matricula{id=" + id + "}";
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Matricula)) return false;
    Matricula other = (Matricula) o;
    return id != null && id.equals(other.id);
  }
}
