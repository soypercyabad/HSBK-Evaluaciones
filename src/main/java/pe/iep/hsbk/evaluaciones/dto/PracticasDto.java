package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

public class PracticasDto {
  private BigDecimal p1;
  private BigDecimal p2;
  private BigDecimal p3;
  private BigDecimal p4;
  private BigDecimal prom;

  public PracticasDto() {}

  public PracticasDto(BigDecimal p1, BigDecimal p2, BigDecimal p3, BigDecimal p4, BigDecimal prom) {
    this.p1 = p1; this.p2 = p2; this.p3 = p3; this.p4 = p4; this.prom = prom;
  }

  public BigDecimal getP1() { return p1; }
  public void setP1(BigDecimal p1) { this.p1 = p1; }

  public BigDecimal getP2() { return p2; }
  public void setP2(BigDecimal p2) { this.p2 = p2; }

  public BigDecimal getP3() { return p3; }
  public void setP3(BigDecimal p3) { this.p3 = p3; }

  public BigDecimal getP4() { return p4; }
  public void setP4(BigDecimal p4) { this.p4 = p4; }

  public BigDecimal getProm() { return prom; }
  public void setProm(BigDecimal prom) { this.prom = prom; }
}
