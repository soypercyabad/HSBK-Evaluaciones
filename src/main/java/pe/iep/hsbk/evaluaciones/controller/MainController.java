package pe.iep.hsbk.evaluaciones.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.util.Dialogs;

public class MainController {

  @FXML private StackPane contentContainer;
  @FXML private Button btnPrimaria, btnSecundaria, btnFirmas;

  private void marcarActivo(Button activo) {
    for (Button b : new Button[]{btnPrimaria, btnSecundaria, btnFirmas}) {
      var sc = b.getStyleClass();
      sc.removeAll("nav-btn-active");
      if (!sc.contains("nav-btn")) sc.add("nav-btn"); // asegura base
    }
    var scActivo = activo.getStyleClass();
    if (!scActivo.contains("nav-btn-active")) scActivo.add("nav-btn-active");
    scActivo.remove("nav-btn"); // si quieres que reemplace a la base
  }

  @FXML
  public void initialize() {
    marcarActivo(btnPrimaria);
    loadContent("/pe/iep/hsbk/evaluaciones/view/students_list_view.fxml");
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
        App.showLogin(stage); // ← vuelve al login tamaño fijo
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Opcional: botón para ocultar (minimizar) la app
  @FXML
  private void onOcultar() {
    Stage stage = (Stage) contentContainer.getScene().getWindow();
    App.minimizar(stage);
  }

  private void loadContent(String fxmlPath) {
    try {
      Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
      contentContainer.getChildren().setAll(node);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
