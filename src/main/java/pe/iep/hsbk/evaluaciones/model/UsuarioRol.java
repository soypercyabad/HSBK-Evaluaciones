package pe.iep.hsbk.evaluaciones.model;

public class UsuarioRol {
  private Long usuarioId;
  private Long rolId;

  public UsuarioRol() {
  }

  public UsuarioRol(Long usuarioId, Long rolId) {
    this.usuarioId = usuarioId;
    this.rolId = rolId;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getRolId() {
    return rolId;
  }

  public void setRolId(Long rolId) {
    this.rolId = rolId;
  }

  @Override
  public String toString() {
    return "UsuarioRol{usuarioId=" + usuarioId + ", rolId=" + rolId + "}";
  }

  @Override
  public int hashCode() {
    return (usuarioId == null ? 0 : usuarioId.hashCode()) * 31 + (rolId == null ? 0 : rolId.hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UsuarioRol)) return false;
    UsuarioRol other = (UsuarioRol) o;
    return (usuarioId != null && usuarioId.equals(other.usuarioId)) &&
        (rolId != null && rolId.equals(other.rolId));
  }
}
