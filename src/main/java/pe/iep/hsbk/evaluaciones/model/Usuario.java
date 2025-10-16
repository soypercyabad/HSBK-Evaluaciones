package pe.iep.hsbk.evaluaciones.model;

public class Usuario {
  private String id;
  private String username;
  private String nombre;
  private String apellido;
  private String claveSecreta;
  private String rol; // Docente - Tutor - Admin
  private boolean activo;

  public Usuario() {
  }

  public Usuario(String id, String username, String nombre, String apellido, String claveSecreta, String rol, boolean activo) {
    this.id = id;
    this.username = username;
    this.nombre = nombre;
    this.apellido = apellido;
    this.claveSecreta = claveSecreta;
    this.rol = rol;
    this.activo = activo;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getClaveSecreta() {
    return claveSecreta;
  }

  public void setClaveSecreta(String claveSecreta) {
    this.claveSecreta = claveSecreta;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }
}
