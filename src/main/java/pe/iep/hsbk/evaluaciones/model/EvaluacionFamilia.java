package pe.iep.hsbk.evaluaciones.model;

public class EvaluacionFamilia {
  private String id;
  private String alumnoId;
  private String bimestreId;
  private String utiles; // A/B/C
  private String participacion; // A/B/C
  private String reuniones; // A/B/C
  private String escuelaPadres; // A/B/C

  public EvaluacionFamilia() {
  }

  public EvaluacionFamilia(String id, String alumnoId, String bimestreId, String utiles, String participacion, String reuniones, String escuelaPadres) {
    this.id = id;
    this.alumnoId = alumnoId;
    this.bimestreId = bimestreId;
    this.utiles = utiles;
    this.participacion = participacion;
    this.reuniones = reuniones;
    this.escuelaPadres = escuelaPadres;
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

  public String getUtiles() {
    return utiles;
  }

  public void setUtiles(String utiles) {
    this.utiles = utiles;
  }

  public String getParticipacion() {
    return participacion;
  }

  public void setParticipacion(String participacion) {
    this.participacion = participacion;
  }

  public String getReuniones() {
    return reuniones;
  }

  public void setReuniones(String reuniones) {
    this.reuniones = reuniones;
  }

  public String getEscuelaPadres() {
    return escuelaPadres;
  }

  public void setEscuelaPadres(String escuelaPadres) {
    this.escuelaPadres = escuelaPadres;
  }
}
