package pe.iep.hsbk.evaluaciones.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;

import java.io.IOException;

public class LoginController {

  @FXML
  private TextField txtUsuario;
  @FXML
  private PasswordField txtPassword;
  @FXML
  private CheckBox chkRemember;
  @FXML
  private Button btnIngresar;
  @FXML
  private ProgressIndicator piLogin;

  // Pane derecho e imagen grande del login
  @FXML
  private StackPane rightPane;
  @FXML
  private ImageView rightImage;

  private final AuthService authService = new AuthService();

  @FXML
  private void initialize() {
    // Eventos del formulario
    btnIngresar.setOnAction(e -> doLogin());
    txtUsuario.setOnAction(e -> doLogin());
    txtPassword.setOnAction(e -> doLogin());

    // Hacer que la imagen de la derecha se adapte al tamaño del StackPane
    if (rightPane != null && rightImage != null) {
      // que el ImageView siempre tenga el mismo tamaño que el StackPane
      rightImage.fitWidthProperty().bind(rightPane.widthProperty());
      rightImage.fitHeightProperty().bind(rightPane.heightProperty());
      rightImage.setPreserveRatio(true); // ya está en FXML, pero lo reforzamos
    }
  }

  @FXML
  private void onIngresar() {
    doLogin();
  }

  private void doLogin() {
    String usr = txtUsuario.getText();
    String pwd = txtPassword.getText();

    if (usr == null || usr.isBlank() || pwd == null || pwd.isBlank()) {
      Dialogs.warn(null, "Campos vacíos", "Por favor ingrese usuario y contraseña.");
      return;
    }

    setUiBusy(true);

    // Ejecutar login en background con FXAsync
    FXAsync.run(
        () -> {
          try {
            return authService.login(usr, pwd);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        },
        session -> {
          setUiBusy(false);

          if (session == null) {
            Dialogs.error(null, "Error de autenticación",
                "Usuario o contraseña incorrectos, o usuario inactivo.");
            return;
          }

          Stage stage = (Stage) btnIngresar.getScene().getWindow();
          try {
            App.showMain(stage, session);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        },
        ex -> {
          setUiBusy(false);
          Dialogs.errorConStacktrace(null, "Error inesperado",
              "Ocurrió un problema", ex.getMessage(), ex);
        }
    );
  }

  // Bloquea/desbloquea el formulario y muestra/oculta el spinner.
  private void setUiBusy(boolean busy) {
    txtUsuario.setDisable(busy);
    txtPassword.setDisable(busy);
    chkRemember.setDisable(busy);
    btnIngresar.setDisable(busy);
    piLogin.setVisible(busy);
    piLogin.setManaged(busy);
  }
}
