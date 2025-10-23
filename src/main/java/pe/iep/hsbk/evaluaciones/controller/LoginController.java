package pe.iep.hsbk.evaluaciones.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnIngresar;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        btnIngresar.setOnAction(e -> doLogin());
    }

    private void doLogin() {
        String usr = txtUsuario.getText();
        String pwd = txtPassword.getText();

        try {
            if (usr == null || usr.isBlank() || pwd == null || pwd.isBlank()) {
                Dialogs.warn(null, "Campos vacíos", "Por favor ingrese usuario y contraseña.");
                return;
            }

            AuthService.UserSession sess = authService.login(usr, pwd);
            if (sess == null) {
                Dialogs.error(null, "Error de autenticación",
                    "Usuario o contraseña incorrectos, o usuario inactivo.");
                return;
            }

            Stage stage = (Stage) btnIngresar.getScene().getWindow();
            App.showMain(stage, sess);

        } catch (Exception ex) {
            Dialogs.errorConStacktrace(null, "Error inesperado",
                "Ocurrió un problema", ex.getMessage(), ex);
        }
    }
}
