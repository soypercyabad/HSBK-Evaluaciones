package pe.iep.hsbk.evaluaciones.model;

public class Firma {
  private Long id;
  private Long usuarioId;
  private String nombre;
  private String rol;
  private byte[] imagen;
  private boolean activo;

  public Firma() {

  }

  public Firma(Long id, Long usuarioId, String nombre, String rol, byte[] imagen, boolean activo) {
    this.id = id;
    this.usuarioId = usuarioId;
    this.nombre = nombre;
    this.rol = rol;
    this.imagen = imagen;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public byte[] getImagen() {
    return imagen;
  }

  public void setImagen(byte[] imagen) {
    this.imagen = imagen;
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
    if (!(o instanceof Firma)) return false;
    Firma other = (Firma) o;
    return id != null && id.equals(other.id);
  }
}
