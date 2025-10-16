module pe.iep.hsbk.evaluaciones {
    requires javafx.controls;
    requires javafx.fxml;
  requires java.desktop;

  opens pe.iep.hsbk.evaluaciones.controller to javafx.fxml;
    exports pe.iep.hsbk.evaluaciones;
}
