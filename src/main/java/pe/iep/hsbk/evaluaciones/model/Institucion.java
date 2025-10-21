package pe.iep.hsbk.evaluaciones.model;

public class Institucion {
  private Long id;
  private String nombre;
  private byte[] sello;
  private boolean activo;

  public Institucion() {
  }

  public Institucion(Long id, String nombre, byte[] sello, boolean activo) {
    this.id = id;
    this.nombre = nombre;
    this.sello = sello;
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

  public byte[] getSello() {
    return sello;
  }

  public void setSello(byte[] sello) {
    this.sello = sello;
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
    if (!(o instanceof Institucion)) return false;
    Institucion other = (Institucion) o;
    return id != null && id.equals(other.id);
  }
}
