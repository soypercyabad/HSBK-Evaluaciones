package pe.iep.hsbk.evaluaciones.util;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FXAsync {
  private static final ExecutorService EXEC = Executors.newCachedThreadPool(r -> {
    Thread t = new Thread(r, "bg-worker"); // Nombre del hilo
    t.setDaemon(true);
    return t;
  });

  /** Ejecuta supplier en background y notifica en UI. */
  public static <T> void run(Supplier<T> supplier,
                             Consumer<T> onSuccess,
                             Consumer<Throwable> onError) {
    Task<T> task = new Task<>() {
      @Override protected T call() throws Exception { return supplier.get(); }
    };
    task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
    task.setOnFailed(e -> onError.accept(task.getException()));
    EXEC.submit(task);
  }

  /** Crea un pequeño loader vertical reutilizable. */
  public static VBox buildLoader(String texto) {
    ProgressIndicator pi = new ProgressIndicator();
    pi.setMaxSize(48, 48);
    Label lbl = new Label(texto);
    VBox box = new VBox(12, pi, lbl);
    box.getStyleClass().add("overlay-loader"); // opcional CSS
    box.setPickOnBounds(true);
    box.setVisible(false);
    box.setManaged(false);
    box.setMouseTransparent(false);
    box.setStyle("-fx-background-color: rgba(0,0,0,0.25); -fx-alignment: center; -fx-padding: 24;");
    return box;
  }

  public static void show(Node n) { n.setVisible(true); n.setManaged(true); }
  public static void hide(Node n) { n.setVisible(false); n.setManaged(false); }
}
