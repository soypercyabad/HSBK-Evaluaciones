package pe.iep.hsbk.evaluaciones.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

public class MainController {

  @FXML private StackPane contentContainer;
  @FXML private Button btnPrimaria, btnSecundaria, btnFirmas;

  private AuthService.UserSession userSession;

  /** Llamado por App.showMain() apenas cargue el FXML. */
  public void initSession(AuthService.UserSession session) {
    this.userSession = session;

    // Habilitar/deshabilitar navegación según roles
    boolean puedeDocente = hasRole("DOCENTE");
    boolean puedeFirmas = hasRole("ADMIN") || hasRole("DIRECTOR");

    // Navegación
    show(btnPrimaria, puedeDocente);
    show(btnSecundaria, puedeDocente);
    show(btnFirmas, puedeFirmas);

    // Vista inicial
    if (btnPrimaria.isVisible()) {
      onPrimaria();
    } else if (btnSecundaria.isVisible()) {
      onSecundaria();
    } else if (btnFirmas.isVisible()) {
      onFirmas();
    }
  }

  private boolean hasRole(String role) {
    return userSession != null &&
        userSession.getRoles() != null &&
        userSession.getRoles().contains(role);
  }

  private void show(Node n, boolean visible) {
    n.setVisible(visible);
    n.setManaged(visible);
  }

  private void marcarActivo(Button activo) {
    for (Button b : new Button[]{btnPrimaria, btnSecundaria, btnFirmas}) {
      var sc = b.getStyleClass();
      sc.removeAll("nav-btn-active");
      if (!sc.contains("nav-btn")) sc.add("nav-btn");
    }
    var scActivo = activo.getStyleClass();
    if (!scActivo.contains("nav-btn-active")) scActivo.add("nav-btn-active");
    scActivo.remove("nav-btn");
  }

  @FXML private void onPrimaria() {
    marcarActivo(btnPrimaria);
    loadContent("/pe/iep/hsbk/evaluaciones/view/students_list_view.fxml");
  }

  @FXML private void onSecundaria() {
    marcarActivo(btnSecundaria);
    loadContent("/pe/iep/hsbk/evaluaciones/view/students_list_view.fxml");
  }

  @FXML private void onFirmas() {
    marcarActivo(btnFirmas);
    loadContent("/pe/iep/hsbk/evaluaciones/view/firmas_view.fxml");
  }

  @FXML
  private void onLogout() {
    try {
      Stage stage = (Stage) contentContainer.getScene().getWindow();
      if (Dialogs.confirm(stage, "¿Seguro que desea cerrar sesión?", "")) {
        App.showLogin(stage);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void onOcultar() {
    Stage stage = (Stage) contentContainer.getScene().getWindow();
    App.minimizar(stage);
  }

  /** Carga FXML y, si implementa SesionAware, le pasa la sesión. */
  private void loadContent(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Node node = loader.load();

      Object child = loader.getController();
      if (child instanceof SesionAware) {
        ((SesionAware) child).setSession(userSession);
      }
      contentContainer.getChildren().setAll(node);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
