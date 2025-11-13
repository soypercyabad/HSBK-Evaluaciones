package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

/** DTO agregado con todas las columnas devueltas por el SP único. */
public class NotasCursoResumenDto {
  private final Long matriculaId;
  private final Long cursoId;
  private final Long bimestreId;

  private final BigDecimal p1;
  private final BigDecimal p2;
  private final BigDecimal p3;
  private final BigDecimal p4;
  private final BigDecimal promPracticas;

  private final BigDecimal tareaLibro;
  private final BigDecimal tareaCuaderno;
  private final BigDecimal promTareas;

  private final BigDecimal exMensual;
  private final BigDecimal exBimestral;

  private final BigDecimal promFinal;
  private final String letra;

  public NotasCursoResumenDto(Long matriculaId, Long cursoId, Long bimestreId,
                       BigDecimal p1, BigDecimal p2, BigDecimal p3, BigDecimal p4, BigDecimal promPracticas,
                       BigDecimal tareaLibro, BigDecimal tareaCuaderno, BigDecimal promTareas,
                       BigDecimal exMensual, BigDecimal exBimestral,
                       BigDecimal promFinal, String letra) {
    this.matriculaId = matriculaId;
    this.cursoId = cursoId;
    this.bimestreId = bimestreId;
    this.p1 = p1; this.p2 = p2; this.p3 = p3; this.p4 = p4; this.promPracticas = promPracticas;
    this.tareaLibro = tareaLibro; this.tareaCuaderno = tareaCuaderno; this.promTareas = promTareas;
    this.exMensual = exMensual; this.exBimestral = exBimestral;
    this.promFinal = promFinal; this.letra = letra;
  }

  public Long getMatriculaId() { return matriculaId; }
  public Long getCursoId() { return cursoId; }
  public Long getBimestreId() { return bimestreId; }

  public BigDecimal getP1() { return p1; }
  public BigDecimal getP2() { return p2; }
  public BigDecimal getP3() { return p3; }
  public BigDecimal getP4() { return p4; }
  public BigDecimal getPromPracticas() { return promPracticas; }

  public BigDecimal getTareaLibro() { return tareaLibro; }
  public BigDecimal getTareaCuaderno() { return tareaCuaderno; }
  public BigDecimal getPromTareas() { return promTareas; }

  public BigDecimal getExMensual() { return exMensual; }
  public BigDecimal getExBimestral() { return exBimestral; }

  public BigDecimal getPromFinal() { return promFinal; }
  public String getLetra() { return letra; }
}
