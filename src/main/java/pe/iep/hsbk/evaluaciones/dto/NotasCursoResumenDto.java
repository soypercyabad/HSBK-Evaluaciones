package pe.iep.hsbk.evaluaciones.dto;

import java.math.BigDecimal;

/** DTO agregado con todas las columnas devueltas por el SP único. */
public class NotasCursoResumenDto {
  private final Long matriculaId;
  private final Long cursoId;
  private final Long bimestreId;
  private final Long usuarioId;

  private final BigDecimal p1;
  private final String p1_letra;
  private final BigDecimal p2;
  private final String p2_letra;
  private final BigDecimal p3;
  private final String p3_letra;
  private final BigDecimal p4;
  private final String p4_letra;
  private final BigDecimal promPracticas;

  private final BigDecimal tareaLibro;
  private final String tareaLibro_letra;
  private final BigDecimal tareaCuaderno;
  private final String tareaCuaderno_letra;
  private final BigDecimal promTareas;

  private final BigDecimal exMensual;
  private final String exMensual_letra;
  private final BigDecimal exBimestral;
  private final String exBimestral_letra;

  private final BigDecimal promFinal;
  private final String letra;

  public NotasCursoResumenDto(Long matriculaId, Long cursoId,
                              Long bimestreId, Long usuarioId,
                              BigDecimal p1, String p1_letra,
                              BigDecimal p2, String p2_letra,
                              BigDecimal p3, String p3_letra,
                              BigDecimal p4, String p4_letra,
                              BigDecimal promPracticas,
                              BigDecimal tareaLibro, String tareaLibro_letra,
                              BigDecimal tareaCuaderno, String tareaCuaderno_letra,
                              BigDecimal promTareas,
                              BigDecimal exMensual, String exMensual_letra,
                              BigDecimal exBimestral, String exBimestral_letra,
                              BigDecimal promFinal,
                              String letra) {
    this.matriculaId = matriculaId;
    this.cursoId = cursoId;
    this.bimestreId = bimestreId;
    this.usuarioId = usuarioId;
    this.p1 = p1;
    this.p1_letra = p1_letra;
    this.p2 = p2;
    this.p2_letra = p2_letra;
    this.p3 = p3;
    this.p3_letra = p3_letra;
    this.p4 = p4;
    this.p4_letra = p4_letra;
    this.promPracticas = promPracticas;
    this.tareaLibro = tareaLibro;
    this.tareaLibro_letra = tareaLibro_letra;
    this.tareaCuaderno = tareaCuaderno;
    this.tareaCuaderno_letra = tareaCuaderno_letra;
    this.promTareas = promTareas;
    this.exMensual = exMensual;
    this.exMensual_letra = exMensual_letra;
    this.exBimestral = exBimestral;
    this.exBimestral_letra = exBimestral_letra;
    this.promFinal = promFinal;
    this.letra = letra;
  }

  public Long getMatriculaId() { return matriculaId; }
  public Long getCursoId() { return cursoId; }
  public Long getBimestreId() { return bimestreId; }
  public Long getUsuarioId() { return usuarioId; }

  public BigDecimal getP1() { return p1; }
  public String getP1_letra() { return p1_letra; }
  public BigDecimal getP2() { return p2; }
  public String getP2_letra() { return p2_letra; }
  public BigDecimal getP3() { return p3; }
  public String getP3_letra() { return p3_letra; }
  public BigDecimal getP4() { return p4; }
  public String getP4_letra() { return p4_letra; }
  public BigDecimal getPromPracticas() { return promPracticas; }

  public BigDecimal getTareaLibro() { return tareaLibro; }
  public String getTareaLibro_letra() { return tareaLibro_letra; }
  public BigDecimal getTareaCuaderno() { return tareaCuaderno; }
  public String getTareaCuaderno_letra() { return tareaCuaderno_letra; }
  public BigDecimal getPromTareas() { return promTareas; }

  public BigDecimal getExMensual() { return exMensual; }
  public String getExMensual_letra() { return exMensual_letra; }
  public BigDecimal getExBimestral() { return exBimestral; }
  public String getExBimestral_letra() { return exBimestral_letra; }

  public BigDecimal getPromFinal() { return promFinal; }
  public String getLetra() { return letra; }
}
