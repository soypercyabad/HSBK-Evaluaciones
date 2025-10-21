package pe.iep.hsbk.evaluaciones.model;

public class Grado {
  private Long id;
  private Long nivelId;
  private String nombre;
  private Integer orden;

  public Grado() {
  }

  public Grado(Long id, Long nivelId, String nombre, Integer orden) {
    this.id = id;
    this.nivelId = nivelId;
    this.nombre = nombre;
    this.orden = orden;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getNivelId() {
    return nivelId;
  }

  public void setNivelId(Long nivelId) {
    this.nivelId = nivelId;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Integer getOrden() {
    return orden;
  }

  public void setOrden(Integer orden) {
    this.orden = orden;
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
    if (!(o instanceof Grado)) return false;
    Grado other = (Grado) o;
    return id != null && id.equals(other.id);
  }
}
