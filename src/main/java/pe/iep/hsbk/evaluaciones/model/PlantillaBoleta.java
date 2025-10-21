package pe.iep.hsbk.evaluaciones.model;

public class PlantillaBoleta {
  private Long id;
  private String nombre;
  private String contenidoHtml;
  private boolean activo;

  public PlantillaBoleta() {
  }

  public PlantillaBoleta(Long id, String nombre, String contenidoHtml, boolean activo) {
    this.id = id;
    this.nombre = nombre;
    this.contenidoHtml = contenidoHtml;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getContenidoHtml() {
    return contenidoHtml;
  }

  public void setContenidoHtml(String contenidoHtml) {
    this.contenidoHtml = contenidoHtml;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @Override
  public String toString() {
    return nombre;
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PlantillaBoleta)) return false;
    PlantillaBoleta other = (PlantillaBoleta) o;
    return id != null && id.equals(other.id);
  }
}
