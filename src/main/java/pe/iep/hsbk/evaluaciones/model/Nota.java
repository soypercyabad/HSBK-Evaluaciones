package pe.iep.hsbk.evaluaciones.model;

public class Nota {
  private String id;
  private String alumnoId;
  private String bimestreId;
  private String cursoId;
  private Integer nota;
  private String letra;
  private boolean noEvaluado;
  private String docenteId;

  private void setValor (Integer nota) {
    if (nota == null || nota < 0 || nota > 20) {
      throw new IllegalArgumentException("La nota debe estar entre 0 y 20");
    }

    this.nota = nota;
    this.letra = convertirANotaLetra(nota);
    this.noEvaluado = false;
  }

  private String convertirANotaLetra(Integer nota) {
    if (nota >= 18) {
      return "AD";
    } else if (nota >= 14) {
      return "A";
    } else if (nota >= 10) {
      return "B";
    } else {
      return "C";
    }
  }
}
