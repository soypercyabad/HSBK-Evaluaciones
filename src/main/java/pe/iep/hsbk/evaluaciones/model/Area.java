package pe.iep.hsbk.evaluaciones.model;

public class Area {
  private String id;
  private String nombre;
  private boolean activo;

  public Area() {
  }

  public Area(String id, String nombre, boolean activo) {
    this.id = id;
    this.nombre = nombre;
    this.activo = activo;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
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
}
