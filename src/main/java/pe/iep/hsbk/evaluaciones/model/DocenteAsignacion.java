package pe.iep.hsbk.evaluaciones.model;

public class DocenteAsignacion {
  private Long id;
  private Long usuarioId;
  private Long cursoId;
  private Long seccionId;
  private Long periodoId;
  private boolean activo;

  public DocenteAsignacion() {
  }

  public DocenteAsignacion(Long id, Long usuarioId, Long cursoId, Long seccionId, Long periodoId, boolean activo) {
    this.id = id;
    this.usuarioId = usuarioId;
    this.cursoId = cursoId;
    this.seccionId = seccionId;
    this.periodoId = periodoId;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getCursoId() {
    return cursoId;
  }

  public void setCursoId(Long cursoId) {
    this.cursoId = cursoId;
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

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @Override
  public String toString() {
    return "DocenteAsignacion{id=" + id + "}";
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DocenteAsignacion)) return false;
    DocenteAsignacion other = (DocenteAsignacion) o;
    return id != null && id.equals(other.id);
  }
}
