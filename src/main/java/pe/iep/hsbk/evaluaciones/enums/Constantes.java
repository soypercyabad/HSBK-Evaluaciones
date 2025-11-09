package pe.iep.hsbk.evaluaciones.enums;

public class Constantes {
  // HTML Preview Messages
  public static final String HTML_PREVIEW = "<html><body style='font-family: sans-serif;margin: 5%; height: 100%; width: 100%;'><div style='border: 1px solid #9EEAF9; padding: 12px; color: #055160; background-color: #CFF4FC; border-radius: 10px;'><strong>Aviso:</strong><br><br>✅ Selecciona el <strong>ojito</strong> para previsualizar el contenido HTML.</div></body></html>";
  public static final String HTML_NOT_FOUND = "<html><body style='font-family: sans-serif;margin: 5%; height: 100%; width: 100%;'><div style='border: 1px solid #F1AEB5; padding: 12px; color: #58151C; background-color: #F8D7DA; border-radius: 10px;'><strong>Aviso:</strong><br><br>❌ Esta plantilla no tiene contenido HTML.</div></body></html>";

  // IMG Preview Messages
  public static final String IMG_PREVIEW = "<html><body style='font-family: sans-serif;margin: 5%; height: 100%; width: 100%;'><div style='border: 1px solid #9EEAF9; padding: 12px; color: #055160; background-color: #CFF4FC; border-radius: 10px;'><strong>Aviso:</strong><br><br>✅ Selecciona el <strong>ojito</strong> para previsualizar el contenido de la imagen.</div></body></html>";
  public static final String IMG_NOT_FOUND = "<html><body style='font-family: sans-serif;margin: 5%; height: 100%; width: 100%;'><div style='border: 1px solid #F1AEB5; padding: 12px; color: #58151C; background-color: #F8D7DA; border-radius: 10px;'><strong>Aviso:</strong><br><br>❌ Esta imagen no tiene contenido.</div></body></html>";

  // Estados
  public static final String ESTADO_ACTIVO = "Activo";
  public static final String ESTADO_INACTIVO = "Desactivo";

  // Titulos de panels
  public static final String PANEL_PLANTILLA_BOLETA = "Gestión de Plantillas de Boletas";
  public static final String PANEL_FIRMA = "Gestión de Firmas Digitales";
  public static final String PANEL_SELLO = "Gestión de Sellos Digitales";

  // Iconos UI Paths
  public static final String edit = "m3.99 16.854-1.314 3.504a.75.75 0 0 0 .966.965l3.503-1.314a3 3 0 0 0 1.068-.687 " +
      "L18.36 9.175s-.354-1.061-1.414-2.122c-1.06-1.06-2.122-1.414-2.122-1.414 " +
      "L4.677 15.786a3 3 0 0 0-.687 1.068zm12.249-12.63 1.383-1.383c.248-.248.579-.406.925-.348" +
      ".487.08 1.232.322 1.934 1.025.703.703.945 1.447 1.025 1.934.058.346-.1.677-.348.925 " +
      "L19.774 7.76s-.353-1.06-1.414-2.12c-1.06-1.062-2.121-1.415-2.121-1.415z";
  public static final String view = "M0 16q0.064 0.128 0.16 0.352t0.48 0.928 0.832 1.344 1.248 1.536 1.664 1.696 " +
      "2.144 1.568 2.624 1.344 3.136 0.896 3.712 0.352 3.712-0.352 3.168-0.928 " +
      "2.592-1.312 2.144-1.6 1.664-1.632 1.248-1.6 0.832-1.312 0.48-0.928l0.16-0.352" +
      "q-0.032-0.128-0.16-0.352t-0.48-0.896-0.832-1.344-1.248-1.568-1.664-1.664" +
      "-2.144-1.568-2.624-1.344-3.136-0.896-3.712-0.352-3.712 0.352-3.168 0.896" +
      "-2.592 1.344-2.144 1.568-1.664 1.664-1.248 1.568-0.832 1.344-0.48 0.928z";
  public static final String view_pupila = "M10.016 16q0-2.464 1.728-4.224t4.256-1.76 4.256 1.76 1.76 4.224-1.76 4.256" +
      "-4.256 1.76-4.256-1.76-1.728-4.256zM12 16q0 1.664 1.184 2.848t2.816 1.152 " +
      "2.816-1.152 1.184-2.848-1.184-2.816-2.816-1.184-2.816 1.184l2.816 2.816h-4z";

  // Rutas FXML
  public enum Route {
    STUDENTS("/pe/iep/hsbk/evaluaciones/view/students_list_view.fxml"),
    STUDENT_NOTAS("/pe/iep/hsbk/evaluaciones/view/alumno_notas_view.fxml"),
    STUDENT_CONDUCTA("/pe/iep/hsbk/evaluaciones/view/alumno_conducta_view.fxml"),
    PLANTILLAS("/pe/iep/hsbk/evaluaciones/view/plantilla_boleta_view.fxml"),
    FIRMAS("/pe/iep/hsbk/evaluaciones/view/firmas_view.fxml"),
    SELLOS("/pe/iep/hsbk/evaluaciones/view/sellos_view.fxml");

    public final String fxml;
    Route(String fxml) { this.fxml = fxml; }
  }
}
