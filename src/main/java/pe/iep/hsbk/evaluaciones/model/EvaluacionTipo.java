package pe.iep.hsbk.evaluaciones.model;

public class EvaluacionTipo {
  private Long id;
  private String codigo;
  private String nombre;
  private boolean activo;

  public EvaluacionTipo() {
  }

  public EvaluacionTipo(Long id, String codigo, String nombre, boolean activo) {
    this.id = id;
    this.codigo = codigo;
    this.nombre = nombre;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
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

  @Override
  public String toString() {
    return codigo + " - " + nombre;
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EvaluacionTipo)) return false;
    EvaluacionTipo other = (EvaluacionTipo) o;
    return id != null && id.equals(other.id);
  }
}
