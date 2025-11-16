package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

public class PromedioAreaBimestreDto {
  private Long areaId;
  private String areaNombre;
  private BigDecimal promedioBimestre;

  public PromedioAreaBimestreDto(Long areaId, String areaNombre, BigDecimal promedioBimestre) {
    this.areaId = areaId;
    this.areaNombre = areaNombre;
    this.promedioBimestre = promedioBimestre;
  }

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

  public BigDecimal getPromedioBimestre() {
    return promedioBimestre;
  }

  public void setPromedioBimestre(BigDecimal promedioBimestre) {
    this.promedioBimestre = promedioBimestre;
  }
}
