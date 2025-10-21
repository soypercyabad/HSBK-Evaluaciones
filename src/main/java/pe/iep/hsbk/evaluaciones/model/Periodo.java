package pe.iep.hsbk.evaluaciones.model;

public class Periodo {
  private Long id;
  private String nombre;
  private boolean activo;

  public Periodo() {
  }

  public Periodo(Long id, String nombre, boolean activo) {
    this.id = id;
    this.nombre = nombre;
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

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  // java
  @Override
  public String toString() {
    return nombre;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Periodo)) return false;
    Periodo other = (Periodo) o;
    return id != null && id.equals(other.id);
  }
}
