package pe.iep.hsbk.evaluaciones.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.util.Dialogs;

import java.time.Instant;

public class LoginController {

    private static final int MAX_INTENTOS = 3;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 10;

    private int intentosFallidos = 0;
    private Instant tiempoBloqueo = null;

    @FXML
    private void onLogin(ActionEvent event) {
        System.out.println("Login click");

        // TODO: validar credenciales; si fallan, muestra alert y return

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            App.showMainLayoutFullArea(stage); // ← main layout a pantalla por tamaño
        } catch (Exception ex) {
            ex.printStackTrace();

            // Info
            Dialogs.info(stage, null, "Operación completada.");

            // Warning
            Dialogs.warn(stage, "Datos faltantes", "Debes ingresar el usuario.");

            // Error
            Dialogs.error(stage, null, "No se pudo guardar los cambios.");

            // Confirmación
            if (Dialogs.confirm(stage, null, "¿Deseas eliminar el registro?")) {
                // ejecutar acción
            }

            // Error con stacktrace
            Dialogs.errorConStacktrace(stage, "Error inesperado", "Ocurrió un problema", ex.getMessage(), ex);
        }
    }

    private boolean estaBloqueado() {
        if (tiempoBloqueo == null) return false;
        var ahora = Instant.now();
        if (ahora.isAfter(tiempoBloqueo.plusSeconds(TIEMPO_BLOQUEO_MINUTOS * 60))) {
            tiempoBloqueo = null;
            intentosFallidos = 0;
            return false;
        }
        return true;
    }
}
