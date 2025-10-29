package pe.iep.hsbk.evaluaciones.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.shape.SVGPath;

import java.util.function.Consumer;

public final class IconButtons {

  private IconButtons() {}

  /** Un path + estilo para un layer del ícono */
  public static final class PathSpec {
    public final String content;   // d="..." del SVG
    public final String style;     // ej: "-fx-fill: #F06292; -fx-stroke: transparent;"
    public PathSpec(String content, String style) {
      this.content = content;
      this.style = style;
    }
  }

  /** Botón suelto (fuera de TableView). */
  public static Button iconButton(
      double minSize, double prefSize, double scale,
      EventHandler<ActionEvent> onAction,
      PathSpec... layers
  ) {
    Button b = baseButton(minSize, prefSize);
    b.setGraphic(buildGraphic(scale, layers));
    if (onAction != null) b.setOnAction(onAction);
    return b;
  }

  /** Botón para usar DENTRO de una TableCell: pasa el item de la fila al handler. */
  public static <T> Button iconButtonForCell(
      TableCell<T, ?> cell,
      double minSize, double prefSize, double scale,
      Consumer<T> onItemAction,
      PathSpec... layers
  ) {
    Button b = baseButton(minSize, prefSize);
    b.setGraphic(buildGraphic(scale, layers));
    b.setOnAction(e -> {
      int idx = cell.getIndex();
      if (idx >= 0 && idx < cell.getTableView().getItems().size()) {
        T item = cell.getTableView().getItems().get(idx);
        if (onItemAction != null) onItemAction.accept(item);
      }
    });
    return b;
  }

  /* -------------------- helpers -------------------- */

  private static Button baseButton(double minSize, double prefSize) {
    Button b = new Button();
    b.getStyleClass().add("icon-btn");
    b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    b.setAlignment(Pos.CENTER);
    b.setMinSize(minSize, minSize);
    b.setPrefSize(prefSize, prefSize);
    return b;
  }

  private static Group buildGraphic(double scale, PathSpec... layers) {
    Group g = new Group();
    if (layers != null) {
      for (PathSpec spec : layers) {
        if (spec == null) continue;
        SVGPath p = new SVGPath();
        p.setContent(spec.content);
        if (spec.style != null && !spec.style.isEmpty()) p.setStyle(spec.style);
        g.getChildren().add(p);
      }
    }
    g.setScaleX(scale);
    g.setScaleY(scale);
    return g;
  }
}
