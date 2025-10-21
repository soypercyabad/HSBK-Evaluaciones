package pe.iep.hsbk.evaluaciones.model;

public class Usuario {
  private Long id;
  private String username;
  private String passwordHash;
  private String nombre;
  private boolean activo;

  public Usuario() {
  }

  public Usuario(Long id, String username, String passwordHash, String nombre, boolean activo) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.nombre = nombre;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
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
    return nombre + " (" + username + ")";
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Usuario)) return false;
    Usuario other = (Usuario) o;
    return id != null && id.equals(other.id);
  }
}
