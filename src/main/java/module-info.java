module pe.iep.hsbk.evaluaciones {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jbcrypt;

    opens pe.iep.hsbk.evaluaciones.controller to javafx.fxml;
    opens pe.iep.hsbk.evaluaciones.model;
    exports pe.iep.hsbk.evaluaciones;
}
