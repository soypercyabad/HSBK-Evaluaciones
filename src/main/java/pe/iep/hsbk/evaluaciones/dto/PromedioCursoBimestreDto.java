package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

public class PromedioCursoBimestreDto {
  private Long areaId;        // null si el curso no tiene área
  private String areaNombre;  // null si no tiene área
  private Long cursoId;
  private String cursoNombre;
  private BigDecimal promedioBimestre;

  public PromedioCursoBimestreDto(Long areaId, String areaNombre, Long cursoId, String cursoNombre, BigDecimal promedioBimestre) {
    this.areaId = areaId;
    this.areaNombre = areaNombre;
    this.cursoId = cursoId;
    this.cursoNombre = cursoNombre;
    this.promedioBimestre = promedioBimestre;
  }

  public PromedioCursoBimestreDto() { }

  public Long getAreaId() {
    return areaId;
  }

  public void setAreaId(Long areaId) {
    this.areaId = areaId;
  }

  public String getAreaNombre() {
    return areaNombre;
  }

  public void setAreaNombre(String areaNombre) {
    this.areaNombre = areaNombre;
  }

  public Long getCursoId() {
    return cursoId;
  }

  public void setCursoId(Long cursoId) {
    this.cursoId = cursoId;
  }

  public String getCursoNombre() {
    return cursoNombre;
  }

  public void setCursoNombre(String cursoNombre) {
    this.cursoNombre = cursoNombre;
  }

  public BigDecimal getPromedioBimestre() {
    return promedioBimestre;
  }

  public void setPromedioBimestre(BigDecimal promedioBimestre) {
    this.promedioBimestre = promedioBimestre;
  }
}
