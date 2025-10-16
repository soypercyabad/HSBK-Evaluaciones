package pe.iep.hsbk.evaluaciones.model;

public class Conducta {
  private String id;
  private String alumnoId;
  private String bimestreId;
  private String valor;

  public Conducta() {
  }

  public Conducta(String id, String alumnoId, String bimestreId, String valor) {
    this.id = id;
    this.alumnoId = alumnoId;
    this.bimestreId = bimestreId;
    this.valor = valor;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAlumnoId() {
    return alumnoId;
  }

  public void setAlumnoId(String alumnoId) {
    this.alumnoId = alumnoId;
  }

  public String getBimestreId() {
    return bimestreId;
  }

  public void setBimestreId(String bimestreId) {
    this.bimestreId = bimestreId;
  }

  public String getValor() {
    return valor;
  }

  public void setValor(String valor) {
    this.valor = valor;
  }
}
