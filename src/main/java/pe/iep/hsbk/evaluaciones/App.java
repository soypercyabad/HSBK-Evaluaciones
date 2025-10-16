package pe.iep.hsbk.evaluaciones;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        showLogin(stage);
        stage.show();
    }

    /** Login */
    public static void showLogin(Stage stage) throws IOException {
        Parent root = loadFXML("login");
        double width  = 1100;
        double height = 680;

        if (scene == null) {
            scene = new Scene(root, width, height);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            stage.setWidth(width);
            stage.setHeight(height);
        }

        stage.setTitle("HSBK – Ingreso");
        stage.setResizable(false);  // quita el botón de maximizar
        stage.setMinWidth(width);  stage.setMaxWidth(width);
        stage.setMinHeight(height); stage.setMaxHeight(height);
        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.centerOnScreen();
    }

    /** Main Layout */
    public static void showMainLayoutFullArea(Stage stage) throws IOException {
        Parent root = loadFXML("main_layout");
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        stage.setTitle("HSBK – Principal");

        // Resetear límites del login
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);

        // Calcular el área visible (respeta barra de tareas/dock)
        Rectangle2D vb = Screen.getPrimary().getVisualBounds();

        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.setResizable(false);

        stage.setX(vb.getMinX());
        stage.setY(vb.getMinY());
        stage.setWidth(vb.getWidth());
        stage.setHeight(vb.getHeight());
    }

    /** Minimizar (ocultar) la ventana actual */
    public static void minimizar(Stage stage) {
        stage.setIconified(true);
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String path = "/pe/iep/hsbk/evaluaciones/view/" + fxml + ".fxml";
        var url = App.class.getResource(path);
        if (url == null) throw new IllegalStateException("FXML no encontrado: " + path);
        return FXMLLoader.load(url);
    }

    public static void main(String[] args) { launch(); }
}
