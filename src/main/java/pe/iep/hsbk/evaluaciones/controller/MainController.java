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
  @FXML private Button btnPrimaria, btnSecundaria, btnPlantillasBoleta, btnFirmas, btnSellos;

  private AuthService.UserSession userSession;

  public void initSession(AuthService.UserSession session) {
    this.userSession = session;

    // Habilitar/deshabilitar navegación según roles
    boolean puedeDocente = hasRole("DOCENTE");
    boolean puedePlantillaBoletas = hasRole("ADMIN") || hasRole("DIRECTOR");
    boolean puedeFirmas = hasRole("ADMIN") || hasRole("DIRECTOR");
    boolean puedeSellos = hasRole("ADMIN") || hasRole("DIRECTOR");

    // Navegación
    show(btnPrimaria, puedeDocente);
    show(btnSecundaria, puedeDocente);
    show(btnPlantillasBoleta, puedePlantillaBoletas);
    show(btnFirmas, puedeFirmas);
    show(btnSellos, puedeSellos);

    // Vista inicial
    if (btnPrimaria.isVisible()) {
      onPrimaria();
    } else if (btnSecundaria.isVisible()) {
      onSecundaria();
    } else if (btnPlantillasBoleta.isVisible()) {
      onPlantillasBoletas();
    }
    else if (btnFirmas.isVisible()) {
      onFirmas();
    } else if (btnSellos.isVisible()) {
      onSellos();
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
    for (Button b : new Button[]{btnPrimaria, btnSecundaria, btnPlantillasBoleta, btnFirmas, btnSellos}) {
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
    loadStudentsViewWithNivel(1L);
  }

  @FXML private void onSecundaria() {
    marcarActivo(btnSecundaria);
    loadStudentsViewWithNivel(2L);
  }

  @FXML private void onPlantillasBoletas() {
    marcarActivo(btnPlantillasBoleta);
    loadContent("/pe/iep/hsbk/evaluaciones/view/plantilla_boleta_view.fxml");
  }

  @FXML private void onFirmas() {
    marcarActivo(btnFirmas);
    loadContent("/pe/iep/hsbk/evaluaciones/view/firmas_view.fxml");
  }

  @FXML private void onSellos() {
    marcarActivo(btnSellos);
    loadContent("/pe/iep/hsbk/evaluaciones/view/sellos_view.fxml");
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

  private void loadStudentsViewWithNivel(long nivelId) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(
          "/pe/iep/hsbk/evaluaciones/view/students_list_view.fxml"));
      Node node = loader.load();

      Object child = loader.getController();
      if (child instanceof SesionAware) {
        ((SesionAware) child).setSession(userSession);
      }
      if (child instanceof StudentsListController) {
        ((StudentsListController) child).setNivelId(nivelId);
      }
      contentContainer.getChildren().setAll(node);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
