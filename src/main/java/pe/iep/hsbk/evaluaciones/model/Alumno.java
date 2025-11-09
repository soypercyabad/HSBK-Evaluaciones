package pe.iep.hsbk.evaluaciones.model;

public class Alumno {
  private Long id;
  private String dni;
  private String codigo;      // ALU{dni}
  private String apellidos;
  private String nombres;
  private Integer numeroOrden; // opcional
  private String grado;
  private String nivel;
  private String seccion;
  private boolean activo;


  public Alumno() {
  }

  public Alumno(Long id, String dni, String codigo, String apellidos, String nombres, Integer numeroOrden, String grado, String nivel, String seccion, boolean activo) {
    this.id = id;
    this.dni = dni;
    this.codigo = codigo;
    this.apellidos = apellidos;
    this.nombres = nombres;
    this.numeroOrden = numeroOrden;
    this.grado = grado;
    this.nivel = nivel;
    this.seccion = seccion;
    this.activo = activo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDni() {
    return dni;
  }

  public void setDni(String dni) {
    this.dni = dni;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public Integer getNumeroOrden() {
    return numeroOrden;
  }

  public void setNumeroOrden(Integer numeroOrden) {
    this.numeroOrden = numeroOrden;
  }

  public String getGrado() {
    return grado;
  }

  public void setGrado(String grado) {
    this.grado = grado;
  }

  public String getNivel() {
    return nivel;
  }

  public void setNivel(String nivel) {
    this.nivel = nivel;
  }

  public String getSeccion() {
    return seccion;
  }

  public void setSeccion(String seccion) {
    this.seccion = seccion;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @Override
  public String toString() {
    return apellidos + ", " + nombres;
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Alumno)) return false;
    Alumno other = (Alumno) o;
    return id != null && id.equals(other.id);
  }
}
