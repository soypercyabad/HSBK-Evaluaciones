package pe.iep.hsbk.evaluaciones.model;

public class Alumno {
  private String id;
  private String nombres;
  private String apellidos;
  private String grado;
  private String seccion;

  public Alumno() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getGrado() {
    return grado;
  }

  public void setGrado(String grado) {
    this.grado = grado;
  }

  public String getSeccion() {
    return seccion;
  }

  public void setSeccion(String seccion) {
    this.seccion = seccion;
  }
}
