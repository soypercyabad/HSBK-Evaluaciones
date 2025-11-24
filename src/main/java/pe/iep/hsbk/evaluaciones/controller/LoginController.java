package pe.iep.hsbk.evaluaciones.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;

import java.io.IOException;

public class LoginController {

  @FXML private TextField txtUsuario;
  @FXML private PasswordField txtPassword;
  @FXML private CheckBox chkRemember;
  @FXML private Button btnIngresar;
  @FXML private ProgressIndicator piLogin;

  // Pane derecho e imagen grande
  @FXML private StackPane rightPane;
  @FXML private ImageView rightImage;

  private final AuthService authService = new AuthService();

  @FXML
  private void initialize() {
    // Eventos del formulario
    btnIngresar.setOnAction(e -> doLogin());
    txtUsuario.setOnAction(e -> doLogin());
    txtPassword.setOnAction(e -> doLogin());

    // Configurar imagen en modo "cover"
    setupRightImageCover();
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
          Dialogs.errorConStacktrace(
              null,
              "Error inesperado",
              "Ocurrió un problema",
              ex.getMessage(),
              ex
          );
        }
    );
  }

  /** Bloquea/desbloquea el formulario y muestra/oculta el spinner. */
  private void setUiBusy(boolean busy) {
    txtUsuario.setDisable(busy);
    txtPassword.setDisable(busy);
    chkRemember.setDisable(busy);
    btnIngresar.setDisable(busy);
    piLogin.setVisible(busy);
    piLogin.setManaged(busy);
  }

  private void setupRightImageCover() {
    if (rightPane == null || rightImage == null) return;

    rightImage.setPreserveRatio(true);
    rightImage.setSmooth(true);
    rightImage.setCache(true);

    Runnable resize = () -> {
      if (rightPane.getWidth() <= 0 || rightPane.getHeight() <= 0) return;
      if (rightImage.getImage() == null) return;

      double paneW = rightPane.getWidth();
      double paneH = rightPane.getHeight();
      double imgW  = rightImage.getImage().getWidth();
      double imgH  = rightImage.getImage().getHeight();

      if (imgW <= 0 || imgH <= 0) return;

      // scale para cubrir completamente el pane (tipo background-size: cover)
      double scale = Math.max(paneW / imgW, paneH / imgH);

      rightImage.setFitWidth(imgW * scale);
      rightImage.setFitHeight(imgH * scale);
    };

    // Recalcular cada vez que cambian las dimensiones del pane
    rightPane.widthProperty().addListener((obs, oldV, newV) -> resize.run());
    rightPane.heightProperty().addListener((obs, oldV, newV) -> resize.run());

    // Primera vez: cuando la escena esté lista
    rightPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
      if (newScene != null) {
        // Forzar un layout inicial y luego recalcular
        rightPane.applyCss();
        rightPane.layout();
        resize.run();
      }
    });
  }
}
