package pe.iep.hsbk.evaluaciones.util;

import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public final class Dialogs {

  private Dialogs() {}

  // ======== Métodos rápidos ========

  public static void info(Stage owner, String title, String header, String content) {
    show(owner, Alert.AlertType.INFORMATION, title, header, content);
  }

  public static void warn(Stage owner, String title, String header, String content) {
    show(owner, Alert.AlertType.WARNING, title, header, content);
  }

  public static void error(Stage owner, String title, String header, String content) {
    show(owner, Alert.AlertType.ERROR, title, header, content);
  }

  /** Confirmación: retorna true si el usuario acepta (OK/YES). */
  public static boolean confirm(Stage owner, String title, String header, String question) {
    Alert alert = base(owner, Alert.AlertType.CONFIRMATION, title, header, question);
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    Optional<ButtonType> r = alert.showAndWait();
    return r.isPresent() && r.get() == ButtonType.OK;
  }

  // ======== Genérico ========

  /** Método genérico reutilizable. */
  public static void show(Stage owner,
                          Alert.AlertType type,
                          String title,
                          String header,
                          String content) {
    Alert alert = base(owner, type, title, header, content);
    alert.getButtonTypes().setAll(ButtonType.OK);
    alert.showAndWait();
  }

  // ======== Error con stacktrace ========

  public static void errorConStacktrace(Stage owner, String title, String header, String mensaje, Throwable ex) {
    Alert alert = base(owner, Alert.AlertType.ERROR, title, header, mensaje);

    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    TextArea area = new TextArea(sw.toString());
    area.setEditable(false);
    area.setWrapText(false);
    area.setMaxWidth(Double.MAX_VALUE);
    area.setMaxHeight(Double.MAX_VALUE);

    VBox box = new VBox(new Label("Detalles:"), area);
    VBox.setVgrow(area, Priority.ALWAYS);

    alert.getDialogPane().setExpandableContent(box);
    alert.getDialogPane().setExpanded(false);
    alert.showAndWait();
  }

  // ======== Común ========

  private static Alert base(Stage owner,
                            Alert.AlertType type,
                            String title,
                            String header,
                            String content) {
    Alert alert = new Alert(type);
    if (owner != null) {
      alert.initOwner(owner);
      alert.initModality(Modality.WINDOW_MODAL);
    }
    if (title != null)  alert.setTitle(title);
    if (header != null) alert.setHeaderText(header);
    if (content != null) alert.setContentText(content);
    return alert;
  }
}
