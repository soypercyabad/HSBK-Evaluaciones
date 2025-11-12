package pe.iep.hsbk.evaluaciones;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pe.iep.hsbk.evaluaciones.controller.MainController;
import pe.iep.hsbk.evaluaciones.service.AuthService;

import java.io.IOException;

public class App extends Application {
  private static Scene scene;

  @Override
  public void start(Stage stage) throws IOException {
    var is = getClass().getResourceAsStream("/pe/iep/hsbk/evaluaciones/assets/icono.png");
    if (is == null) throw new IllegalStateException("Icono no encontrado: /pe/iep/hsbk/evaluaciones/assets/icono.png");
    stage.getIcons().add(new javafx.scene.image.Image(is));
    showLogin(stage);
    stage.show();
  }

  /**
   * Login (tamaño fijo)
   */
  public static void showLogin(Stage stage) throws IOException {
    Parent root = loadFXML("login");

    if (scene == null) {
      scene = new Scene(root);
      stage.setScene(scene);
    } else {
      scene.setRoot(root);
    }

    stage.setTitle("HSBK – Ingreso");

    // Área visual (respeta barra de tareas)
    Rectangle2D vb = Screen.getPrimary().getVisualBounds();

    stage.setMaximized(false);
    stage.setFullScreen(false);
    stage.setResizable(false);

    stage.setX(vb.getMinX());
    stage.setY(vb.getMinY());
    stage.setWidth(vb.getWidth() * 0.85);
    stage.setHeight(vb.getHeight());
    stage.centerOnScreen();
  }

  /**
   * Main con sesión activa (tamaño dinámico)
   */
  public static void showMain(Stage stage, AuthService.UserSession session) throws IOException {
    // Usar FXMLLoader para obtener controller
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/pe/iep/hsbk/evaluaciones/view/main_layout.fxml"));
    Parent root = loader.load();

    MainController main = loader.getController();

    if (scene == null) {
      scene = new Scene(root);
      stage.setScene(scene);
    } else {
      scene.setRoot(root);
    }

    stage.setTitle("HSBK – Principal");
    main.initSession(session); // Enviar sesión al controller

    // Área visual (respeta barra de tareas)
    Rectangle2D vb = Screen.getPrimary().getVisualBounds();

    stage.setMaximized(false);
    stage.setFullScreen(false);
    stage.setResizable(false); //true

    stage.setX(vb.getMinX());
    stage.setY(vb.getMinY());
    stage.setWidth(vb.getWidth());// * 0.98);
    stage.setHeight(vb.getHeight());
    stage.centerOnScreen();
  }

  /**
   * Minimizar (ocultar) la ventana actual
   */
  public static void minimizar(Stage stage) {
    stage.setIconified(true);
  }

  // ---- helpers existentes ----
  static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFXML(fxml));
  }

  private static Parent loadFXML(String fxml) throws IOException {
    String path = "/pe/iep/hsbk/evaluaciones/view/" + fxml + ".fxml";
    var url = App.class.getResource(path);
    if (url == null) throw new IllegalStateException("FXML no encontrado: " + path);
    return FXMLLoader.load(url);
  }

  public static void main(String[] args) {
    launch();
  }
}
