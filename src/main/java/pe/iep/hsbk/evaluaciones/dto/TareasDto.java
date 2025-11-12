package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

public class TareasDto {
  private BigDecimal libro;
  private BigDecimal cuaderno;
  private BigDecimal prom;

  public TareasDto() {}

  public TareasDto(BigDecimal libro, BigDecimal cuaderno, BigDecimal prom) {
    this.libro = libro; this.cuaderno = cuaderno; this.prom = prom;
  }

  public BigDecimal getLibro() { return libro; }
  public void setLibro(BigDecimal libro) { this.libro = libro; }

  public BigDecimal getCuaderno() { return cuaderno; }
  public void setCuaderno(BigDecimal cuaderno) { this.cuaderno = cuaderno; }

  public BigDecimal getProm() { return prom; }
  public void setProm(BigDecimal prom) { this.prom = prom; }
}
