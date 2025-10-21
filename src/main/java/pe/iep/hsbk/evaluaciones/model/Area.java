package pe.iep.hsbk.evaluaciones.model;

public class Area {
  private Long id;
  private String nombre;
  private String descripcion;
  private boolean activo;

  public Area() {
  }

  public Area(Long id, String nombre, String descripcion, boolean activo) {
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
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

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
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
    if (!(o instanceof Area)) return false;
    Area other = (Area) o;
    return id != null && id.equals(other.id);
  }
}
