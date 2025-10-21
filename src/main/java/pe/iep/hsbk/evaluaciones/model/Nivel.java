package pe.iep.hsbk.evaluaciones.model;

public class Nivel {
  private Long id;
  private String nombre;

  public Nivel() {
  }

  public Nivel(Long id, String nombre) {
    this.id = id;
    this.nombre = nombre;
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
    if (!(o instanceof Nivel)) return false;
    Nivel other = (Nivel) o;
    return id != null && id.equals(other.id);
  }
}
