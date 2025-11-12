package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

public class ExamenesDto {
  private BigDecimal mensual;
  private BigDecimal bimestral;

  public ExamenesDto() {}

  public ExamenesDto(BigDecimal mensual, BigDecimal bimestral) {
    this.mensual = mensual; this.bimestral = bimestral;
  }

  public BigDecimal getMensual() { return mensual; }
  public void setMensual(BigDecimal mensual) { this.mensual = mensual; }

  public BigDecimal getBimestral() { return bimestral; }
  public void setBimestral(BigDecimal bimestral) { this.bimestral = bimestral; }
}
