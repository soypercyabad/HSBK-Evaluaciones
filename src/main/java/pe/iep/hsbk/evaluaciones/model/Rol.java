package pe.iep.hsbk.evaluaciones.model;

public class Rol {
  private Long id;
  private String nombre;

  public Rol() {
  }

  public Rol(String nombre, Long id) {
    this.nombre = nombre;
    this.id = id;
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
    if (!(o instanceof Rol)) return false;
    Rol other = (Rol) o;
    return id != null && id.equals(other.id);
  }
}
