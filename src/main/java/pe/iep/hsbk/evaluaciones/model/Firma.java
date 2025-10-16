package pe.iep.hsbk.evaluaciones.model;

import pe.iep.hsbk.evaluaciones.enums.TipoFirma;

import java.time.LocalDateTime;

public class Firma {
  private String id;
  private TipoFirma tipoFirma;
  private String grado;
  private String seccion;
  private String imagenPath;
  private boolean mostrarEnPDF;
  private boolean vigente;
  private String usuarioRegistro;
  private LocalDateTime fechaRegistro;

  public Firma() {
  }
}
