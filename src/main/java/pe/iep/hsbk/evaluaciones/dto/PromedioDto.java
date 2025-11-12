package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

public class PromedioDto {
  private BigDecimal promPracticas;
  private BigDecimal promTareas;
  private BigDecimal exMensual;
  private BigDecimal exBimestral;
  private BigDecimal promedioCurso;
  private String letra;

  public PromedioDto() {}

  public PromedioDto(BigDecimal promPracticas, BigDecimal promTareas,
                     BigDecimal exMensual, BigDecimal exBimestral,
                     BigDecimal promedioCurso, String letra) {
    this.promPracticas = promPracticas;
    this.promTareas = promTareas;
    this.exMensual = exMensual;
    this.exBimestral = exBimestral;
    this.promedioCurso = promedioCurso;
    this.letra = letra;
  }

  public BigDecimal getPromPracticas() { return promPracticas; }
  public void setPromPracticas(BigDecimal v) { this.promPracticas = v; }

  public BigDecimal getPromTareas() { return promTareas; }
  public void setPromTareas(BigDecimal v) { this.promTareas = v; }

  public BigDecimal getExMensual() { return exMensual; }
  public void setExMensual(BigDecimal v) { this.exMensual = v; }

  public BigDecimal getExBimestral() { return exBimestral; }
  public void setExBimestral(BigDecimal v) { this.exBimestral = v; }

  public BigDecimal getPromedioCurso() { return promedioCurso; }
  public void setPromedioCurso(BigDecimal v) { this.promedioCurso = v; }

  public String getLetra() { return letra; }
  public void setLetra(String letra) { this.letra = letra; }
}
