package pe.iep.hsbk.evaluaciones.model;

public class RecomendacionCatalogo {
  private Long id;
  private String mensaje;
  private boolean activo;

  public RecomendacionCatalogo() {
  }

  public RecomendacionCatalogo(Long id, String mensaje, boolean activo) {
    this.id = id;
    this.mensaje = mensaje;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @Override
  public String toString() {
    return mensaje;
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RecomendacionCatalogo)) return false;
    RecomendacionCatalogo other = (RecomendacionCatalogo) o;
    return id != null && id.equals(other.id);
  }
}
