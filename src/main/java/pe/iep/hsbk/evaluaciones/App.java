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

    /** Login: tamaño fijo */
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

    /** Main Layout: ocupa toda la pantalla por tamaño (sin estado maximized) */
    public static void showMainLayoutFullArea(Stage stage) throws IOException {
        Parent root = loadFXML("main_layout"); // <-- nombre del FXML

        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        stage.setTitle("HSBK – Principal");

        // Calcula área visible (respeta barra de tareas/dock)
        Rectangle2D vb = Screen.getPrimary().getVisualBounds();

        // Llenar la pantalla por tamaño (NO activar maximized)
        stage.setResizable(false);      // permite redimensionar si quieres
        stage.setMaximized(false);
        stage.setFullScreen(false);

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
