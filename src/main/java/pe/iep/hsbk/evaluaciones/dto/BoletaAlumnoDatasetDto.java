package pe.iep.hsbk.evaluaciones.dto;

import java.util.List;

public class BoletaAlumnoDatasetDto {

  // Cabecera
  private Long matriculaId;
  private Long alumnoId;
  private String dni;
  private String apellidos;
  private String nombres;
  private Integer numeroOrden;
  private String seccion;
  private String grado;
  private String nivel;
  private String periodo;
  private int bimestre;

  // Cursos
  public static class CursoNota {
    private String curso;
    private int notaFinal;
    private String letra;

    public String getCurso() {
      return curso;
    }

    public void setCurso(String curso) {
      this.curso = curso;
    }

    public int getNotaFinal() {
      return notaFinal;
    }

    public void setNotaFinal(int notaFinal) {
      this.notaFinal = notaFinal;
    }

    public String getLetra() {
      return letra;
    }

    public void setLetra(String letra) {
      this.letra = letra;
    }
  }

  // Áreas
  public static class AreaPromedio {
    private String area;
    private int promedioArea;
    private String letra;

    public String getArea() {
      return area;
    }

    public void setArea(String area) {
      this.area = area;
    }

    public int getPromedioArea() {
      return promedioArea;
    }

    public void setPromedioArea(int promedioArea) {
      this.promedioArea = promedioArea;
    }

    public String getLetra() {
      return letra;
    }

    public void setLetra(String letra) {
      this.letra = letra;
    }
  }

  // Ranking
  private Double puntajeTotal;
  private Integer puesto;

  // Conducta
  private Integer conductaNota;
  private String conductaLetra;

  // Evaluación familiar
  private String utiles;
  private String participacion;
  private String reuniones;
  private String escuelaPadres;
  private String comentariosFam;

  // Recomendación
  private String recomendacion;

  // Listas
  private java.util.List<CursoNota> cursos;
  private java.util.List<AreaPromedio> areas;

  // Otros
  private String nivelNombre;
  private int anio;

  public BoletaAlumnoDatasetDto() {
  }

  public BoletaAlumnoDatasetDto(Long matriculaId, Long alumnoId, String dni, String apellidos, String nombres, Integer numeroOrden, String seccion, String grado, String nivel, String periodo, int bimestre, Double puntajeTotal, Integer puesto, Integer conductaNota, String conductaLetra, String utiles, String participacion, String reuniones, String escuelaPadres, String comentariosFam, String recomendacion, List<CursoNota> cursos, List<AreaPromedio> areas, String nivelNombre, int anio) {
    this.matriculaId = matriculaId;
    this.alumnoId = alumnoId;
    this.dni = dni;
    this.apellidos = apellidos;
    this.nombres = nombres;
    this.numeroOrden = numeroOrden;
    this.seccion = seccion;
    this.grado = grado;
    this.nivel = nivel;
    this.periodo = periodo;
    this.bimestre = bimestre;
    this.puntajeTotal = puntajeTotal;
    this.puesto = puesto;
    this.conductaNota = conductaNota;
    this.conductaLetra = conductaLetra;
    this.utiles = utiles;
    this.participacion = participacion;
    this.reuniones = reuniones;
    this.escuelaPadres = escuelaPadres;
    this.comentariosFam = comentariosFam;
    this.recomendacion = recomendacion;
    this.cursos = cursos;
    this.areas = areas;
    this.nivelNombre = nivelNombre;
    this.anio = anio;
  }

  // getters / setters de todo...
  public Long getMatriculaId() {
    return matriculaId;
  }

  public void setMatriculaId(Long matriculaId) {
    this.matriculaId = matriculaId;
  }

  public Long getAlumnoId() {
    return alumnoId;
  }

  public void setAlumnoId(Long alumnoId) {
    this.alumnoId = alumnoId;
  }

  public String getDni() {
    return dni;
  }

  public void setDni(String dni) {
    this.dni = dni;
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

  public String getSeccion() {
    return seccion;
  }

  public void setSeccion(String seccion) {
    this.seccion = seccion;
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

  public String getPeriodo() {
    return periodo;
  }

  public void setPeriodo(String periodo) {
    this.periodo = periodo;
  }

  public int getBimestre() {
    return bimestre;
  }

  public void setBimestre(int bimestre) {
    this.bimestre = bimestre;
  }

  public Double getPuntajeTotal() {
    return puntajeTotal;
  }

  public void setPuntajeTotal(Double puntajeTotal) {
    this.puntajeTotal = puntajeTotal;
  }

  public Integer getPuesto() {
    return puesto;
  }

  public void setPuesto(Integer puesto) {
    this.puesto = puesto;
  }

  public Integer getConductaNota() {
    return conductaNota;
  }

  public void setConductaNota(Integer conductaNota) {
    this.conductaNota = conductaNota;
  }

  public String getConductaLetra() {
    return conductaLetra;
  }

  public void setConductaLetra(String conductaLetra) {
    this.conductaLetra = conductaLetra;
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

  public String getComentariosFam() {
    return comentariosFam;
  }

  public void setComentariosFam(String comentariosFam) {
    this.comentariosFam = comentariosFam;
  }

  public String getRecomendacion() {
    return recomendacion;
  }

  public void setRecomendacion(String recomendacion) {
    this.recomendacion = recomendacion;
  }

  public List<CursoNota> getCursos() {
    return cursos;
  }

  public void setCursos(List<CursoNota> cursos) {
    this.cursos = cursos;
  }

  public List<AreaPromedio> getAreas() {
    return areas;
  }

  public void setAreas(List<AreaPromedio> areas) {
    this.areas = areas;
  }

  public String getNivelNombre() {
    return nivelNombre;
  }

  public void setNivelNombre(String nivelNombre) {
    this.nivelNombre = nivelNombre;
  }

  public int getAnio() {
    return anio;
  }

  public void setAnio(int anio) {
    this.anio = anio;
  }
}

