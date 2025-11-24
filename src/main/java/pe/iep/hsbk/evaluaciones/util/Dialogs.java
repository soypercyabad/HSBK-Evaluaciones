// Dialogs.java
package pe.iep.hsbk.evaluaciones.util;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public final class Dialogs {

  private Dialogs() {
  }

  // ======== Métodos rápidos ========
  public static void info(Stage owner, String header, String content) {
    show(owner, Alert.AlertType.INFORMATION, header, content);
  }

  public static void warn(Stage owner, String header, String content) {
    show(owner, Alert.AlertType.WARNING, header, content);
  }

  public static void error(Stage owner, String header, String content) {
    show(owner, Alert.AlertType.ERROR, header, content);
  }

  public static boolean confirm(Stage owner, String header, String question) {
    Alert alert = base(owner, Alert.AlertType.CONFIRMATION, header, question);

    // Botones para confirmación
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

    // Estilos específicos de confirmación (OK primario, Cancel outline)
    styleConfirmButtons(alert);

    Optional<ButtonType> r = alert.showAndWait();
    return r.isPresent() && r.get() == ButtonType.OK;
  }

  public static void show(Stage owner, Alert.AlertType type, String header, String content) {
    Alert alert = base(owner, type, header, content);
    alert.getButtonTypes().setAll(ButtonType.OK);
    alert.showAndWait();
  }

  public static void errorConStacktrace(Stage owner, String header, String mensaje, String message, Throwable ex) {
    Alert alert = base(owner, Alert.AlertType.ERROR, header, mensaje);

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

    alert.getButtonTypes().setAll(ButtonType.OK);
    alert.showAndWait();
  }

  // ======== Común ========
  private static final String APP_ICON = "/pe/iep/hsbk/evaluaciones/assets/icono.png";
  private static final String DIALOG_CSS = "/pe/iep/hsbk/evaluaciones/css/fancy-dialog.css";

  private static Alert base(Stage owner, Alert.AlertType type, String header, String content) {
    Alert alert = new Alert(type);
    if (owner != null) {
      alert.initOwner(owner);
      alert.initModality(Modality.WINDOW_MODAL);
    }

    alert.setTitle("HSBK - Notificación");

    // —— aplicamos nuestro layout y estilos “SweetAlert-like”
    sweeten(alert, type, header, content);

    // icono de la ventana
    Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
    setStageIcon(dialogStage, APP_ICON);

    return alert;
  }

  private static void sweeten(Alert alert, Alert.AlertType type, String header, String content) {
    DialogPane pane = alert.getDialogPane();

    // CSS propio
    pane.getStylesheets().add(Dialogs.class.getResource(DIALOG_CSS).toExternalForm());
    pane.getStyleClass().add("sw");
    pane.getStyleClass().add(cssClassFor(type));

    // Layout: icono + (título + mensaje)
    Label title = new Label(header == null ? "" : header);
    title.getStyleClass().add("sw-title");

    Label msg = new Label(content == null ? "" : content);
    msg.getStyleClass().add("sw-message");
    msg.setWrapText(true);

    Label glyph = new Label(iconFor(type)); // ✓, i, !
    glyph.getStyleClass().add("sw-glyph");

    StackPane icon = new StackPane(glyph);
    icon.getStyleClass().add("sw-icon");

    VBox textBox = new VBox(6, title, msg);
    HBox root = new HBox(14, icon, textBox);
    root.getStyleClass().add("sw-content");

    pane.setHeaderText(null);    // ocultamos header nativo
    pane.setGraphic(null);       // sin gráfico nativo
    pane.setContent(root);

  }

  /**
   * Aplica estilos SOLO al diálogo de confirmación:
   *  - OK     (sw-ok):      primario
   *  - CANCEL (sw-cancel):  secundario outline
   */
  private static void styleConfirmButtons(Alert alert) {
    DialogPane pane = alert.getDialogPane();

    Button ok = (Button) pane.lookupButton(ButtonType.OK);
    if (ok != null) {
      ok.getStyleClass().add("sw-ok");
      ok.setDefaultButton(true); // ENTER -> OK
    }

    Button cancel = (Button) pane.lookupButton(ButtonType.CANCEL);
    if (cancel != null) {
      cancel.getStyleClass().add("sw-cancel");
      cancel.setCancelButton(true); // ESC -> Cancel
    }
  }

  private static String cssClassFor(Alert.AlertType t) {
    String result;
    switch (t) {
      case INFORMATION:
        result = "sw-info";
        break;
      case WARNING:
        result = "sw-warning";
        break;
      case ERROR:
        result = "sw-error";
        break;
      case CONFIRMATION:
        result = "sw-confirm";
        break;
      default:
        result = "sw-info";
        break;
    }
    return result;
  }

  private static String iconFor(Alert.AlertType t) {
    String result;
    switch (t) {
      case INFORMATION:
        result = "i";
        break;
      case WARNING:
        result = "!";
        break;
      case ERROR:
        result = "!";
        break;
      case CONFIRMATION:
        result = "?";
        break;
      default:
        result = "i";
        break;
    }
    return result;
  }

  private static void setStageIcon(Stage stage, String path) {
    try (InputStream is = Dialogs.class.getResourceAsStream(path)) {
      if (is != null) stage.getIcons().add(new Image(is));
    } catch (Exception ignored) {
    }
  }
}
