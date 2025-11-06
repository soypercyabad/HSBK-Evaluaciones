package pe.iep.hsbk.evaluaciones.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import pe.iep.hsbk.evaluaciones.dao.PlantillaBoletaDao;
import pe.iep.hsbk.evaluaciones.dao.impl.PlantillaDaoImpl;
import pe.iep.hsbk.evaluaciones.enums.Constantes;
import pe.iep.hsbk.evaluaciones.model.PlantillaBoleta;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.IconButtons;
import pe.iep.hsbk.evaluaciones.util.SesionAware;
import pe.iep.hsbk.evaluaciones.util.FXAsync; // <- usa tu helper asíncrono

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import static pe.iep.hsbk.evaluaciones.util.Format.formatRoles;

public class PlantillaBoletaController implements SesionAware {

  // Top labels
  @FXML private Label lblTitleScreen;
  @FXML private Label lblTitleProfile;

  // Paneles
  @FXML private AnchorPane left_pane;
  @FXML private AnchorPane right_pane;

  // Formulario
  @FXML private TextField txtNombrePlantilla;
  @FXML private ComboBox<String> estadoComboBox;
  @FXML private TextField txtRutaHtml;
  @FXML public Button btnBuscar;

  // Búsqueda
  @FXML private TextField txtBuscar;

  // Tabla
  @FXML private TableView<PlantillaBoleta> tblPlantilla;
  @FXML private TableColumn<PlantillaBoleta, String> colNombres;
  @FXML private TableColumn<PlantillaBoleta, String> colEstado;
  @FXML private TableColumn<PlantillaBoleta, Void>   colAcciones;

  // Preview
  @FXML private WebView webView;
  private WebEngine engine;

  // Overlay
  @FXML private StackPane overlay;

  // Datos
  private final ObservableList<PlantillaBoleta> master = FXCollections.observableArrayList();
  private FilteredList<PlantillaBoleta> filtered;

  // DAO
  private final PlantillaBoletaDao dao = new PlantillaDaoImpl();

  // Edición
  private PlantillaBoleta editing = null;
  private UserSession userSession;

  @Override
  public void setSession(UserSession s) {
    this.userSession = s;
    if (s != null) {
      lblTitleScreen.setText(Constantes.PANEL_PLANTILLA_BOLETA);
      lblTitleProfile.setText(formatRoles(s.getRoles()));
    }
  }

  @FXML
  public void initialize() {
    engine = webView.getEngine();
    instalarAutoFit();

    // Estado por defecto
    if (estadoComboBox != null && estadoComboBox.getItems().isEmpty()) {
      estadoComboBox.getItems().addAll(Constantes.ESTADO_ACTIVO, Constantes.ESTADO_INACTIVO);
      estadoComboBox.setValue(Constantes.ESTADO_ACTIVO);
    }

    configurarTabla();
    cargarLista();

    if (txtBuscar != null) {
      txtBuscar.textProperty().addListener((o,a,b)-> refiltrar());
    }

    // Mensaje inicial de preview
    engine.loadContent(Constantes.HTML_PREVIEW, "text/html");
  }

  // ===================== Tabla =====================
  private void configurarTabla() {
    colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
    colEstado.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue().isActivo() ? Constantes.ESTADO_ACTIVO : Constantes.ESTADO_INACTIVO));

    colNombres.setStyle("-fx-alignment: CENTER-LEFT;");
    colEstado.setStyle("-fx-alignment: CENTER;");

    colAcciones.setCellFactory(col -> new TableCell<>() {
      private final Button btnVer = IconButtons.iconButtonForCell(
          this,
          28, 28, 0.45,
          pb -> previsualizar(pb),
          new IconButtons.PathSpec(Constantes.view,         "-fx-fill: #F06292; -fx-stroke: transparent;"),
          new IconButtons.PathSpec(Constantes.view_pupila,  "-fx-fill: white;    -fx-stroke: transparent;")
      );

      private final Button btnEdit = IconButtons.iconButtonForCell(
          this,
          28, 28, 0.55,
          pb -> cargarEnFormulario(pb),
          new IconButtons.PathSpec(Constantes.edit, "-fx-fill: transparent; -fx-stroke: #003B65; -fx-stroke-width: 2;")
      );

      private final HBox box = new HBox(8, btnVer, btnEdit);
      { box.setAlignment(Pos.CENTER); }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : box);
        setAlignment(Pos.CENTER);
      }
    });

    colAcciones.setStyle("-fx-alignment: CENTER;");
    colAcciones.setPrefWidth(125);
    colAcciones.setSortable(false);

    filtered = new FilteredList<>(master, a -> true);
    tblPlantilla.setItems(filtered);
  }

  // ===================== Datos =====================
  private void cargarLista() {
    setBusy(true);
    FXAsync.run(
        () -> {
          try {
            return dao.getPlantillaBoletas();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },  // background
        data -> {                         // UI
          master.setAll(data);
          refiltrar();
          setBusy(false);
        },
        ex -> {
          setBusy(false);
          ex.printStackTrace();
          Dialogs.error(null, "Error", "No se pudo cargar la lista de plantillas.");
        }
    );
  }

  private void refiltrar() {
    final String q = (txtBuscar == null) ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
    filtered.setPredicate(p ->
        q.isEmpty()
            || (p.getNombre()!=null && p.getNombre().toLowerCase().contains(q))
            || (p.isActivo() ? "activo" : "desactivo").contains(q)
    );
  }

  // ===================== Handlers =====================
  @FXML private void onBuscar() { refiltrar(); }

  @FXML private void onRecargar() { cargarLista(); }

  @FXML
  private void onExaminar() {
    var w = txtRutaHtml.getScene().getWindow();
    FileChooser fc = new FileChooser();
    fc.setTitle("Selecciona HTML");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html", "*.htm"));
    File f = fc.showOpenDialog(w);
    if (f != null) {
      txtRutaHtml.setText(f.getAbsolutePath());
    }
  }

  @FXML
  private void onGuardar() {
    String nombre = txtNombrePlantilla.getText() == null ? "" : txtNombrePlantilla.getText().trim();
    String estado = estadoComboBox.getValue();
    String ruta   = txtRutaHtml.getText() == null ? "" : txtRutaHtml.getText().trim();
    final boolean isNew = (editing == null || editing.getId() == null);

    // Validaciones rápidas en el hilo de UI
    if (nombre.isEmpty()) { Dialogs.warn(null, "Validación", "El nombre es obligatorio."); return; }
    if (isNew && ruta.isEmpty()) { Dialogs.warn(null, "Validación", "Selecciona un archivo .html para guardar su contenido."); return; }

    setBusy(true);

    FXAsync.run(
        () -> {
          // BACKGROUND
          String html = null;
          if (!ruta.isEmpty()) {
            try {
              html = Files.readString(new File(ruta).toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }

          final PlantillaBoleta pb = (isNew ? new PlantillaBoleta() : editing);
          pb.setNombre(nombre);
          pb.setActivo(Constantes.ESTADO_ACTIVO.equalsIgnoreCase(estado));

          if (isNew) {
            pb.setContenidoHtml(html);
            try {
              dao.guardarPlantillaBoleta(pb);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          } else {
            if (html == null) {
              try {
                dao.actualizarEstadoPlantillaBoleta(pb);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            } else {
              pb.setContenidoHtml(html);
              try {
                dao.actualizarPlantillaBoleta(pb);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
          }
          return null;
        },
        ok -> {
          // UI
          limpiarForm();
          cargarLista();  // ya es async
          setBusy(false);
        },
        ex -> {
          setBusy(false);
          ex.printStackTrace();
          Dialogs.error(null, "Error", "No se pudo guardar la plantilla.");
        }
    );
  }

  private void cargarEnFormulario(PlantillaBoleta pb) {
    if (pb == null) return;
    editing = pb;
    txtNombrePlantilla.setText(pb.getNombre());
    estadoComboBox.setValue(pb.isActivo() ? Constantes.ESTADO_ACTIVO : Constantes.ESTADO_INACTIVO);
    txtRutaHtml.clear();
  }

  private void previsualizar(PlantillaBoleta pb) {
    if (pb == null) return;
    String html = pb.getContenidoHtml();
    if (html == null || html.isBlank()) {
      engine.loadContent(Constantes.HTML_NOT_FOUND, "text/html");
      return;
    }
    engine.loadContent(html, "text/html");
  }

  private void limpiarForm() {
    editing = null;
    txtNombrePlantilla.clear();
    estadoComboBox.setValue(Constantes.ESTADO_ACTIVO);
    txtRutaHtml.clear();
  }

  // ===================== Busy / Overlay =====================
  private void setBusy(boolean busy) {
    if (left_pane != null) { left_pane.setDisable(busy); }
    if (right_pane != null) { right_pane.setDisable(busy); }

    if (overlay != null) {
      overlay.setVisible(busy);
      overlay.setManaged(busy);
    }
  }

  // ===================== AUTO-FIT / ESCALA DINÁMICA =====================
  /** Ajuste automático con escala y sin scroll horizontal. */
  private void instalarAutoFit() {
    final double EXTRA_SHRINK = 0.90;
    final double FIT_SAFETY   = 0.98;
    final int    FIT_PADDING  = 8;

    engine.getLoadWorker().stateProperty().addListener((obs, old, st) -> {
      if (st == Worker.State.SUCCEEDED) {
        String js =
            "(function(){\n" +
                "  try {\n" +
                "    var EXTRA=" + EXTRA_SHRINK + ", SAF=" + FIT_SAFETY + ", PAD=" + FIT_PADDING + ";\n" +
                "    var d=document, de=d.documentElement, b=d.body;\n" +
                "    var style=d.createElement('style');\n" +
                "    style.textContent='html,body{margin:0;overflow-x:hidden;overflow-y:auto}'+\n" +
                "                     '#__fitWrap{transform-origin:top left;display:inline-block}';\n" +
                "    d.head.appendChild(style);\n" +
                "    var wrap=d.getElementById('__fitWrap');\n" +
                "    if(!wrap){ wrap=d.createElement('div'); wrap.id='__fitWrap';\n" +
                "      while(b.firstChild){ wrap.appendChild(b.firstChild); }\n" +
                "      b.appendChild(wrap);\n" +
                "    }\n" +
                "    function cW(){ return Math.max(wrap.scrollWidth, wrap.getBoundingClientRect().width); }\n" +
                "    function cH(){ return Math.max(wrap.scrollHeight, wrap.getBoundingClientRect().height); }\n" +
                "    function fit(){\n" +
                "      var viewW = Math.max(1, de.clientWidth - PAD);\n" +
                "      var cw = cW(); if(!cw) return;\n" +
                "      var s = Math.min(1, (viewW / cw) * EXTRA * SAF);\n" +
                "      wrap.style.transform = 'scale(' + s + ')';\n" +
                "      b.style.height = (cH() * s) + 'px';\n" +
                "      var tries=0; while((wrap.getBoundingClientRect().width * s) > viewW && tries++<3){\n" +
                "        s *= 0.98; wrap.style.transform = 'scale(' + s + ')'; b.style.height = (cH() * s) + 'px';\n" +
                "      }\n" +
                "      window.__lastFitScale = s; window.__fit = fit;\n" +
                "    }\n" +
                "    window.addEventListener('load', fit);\n" +
                "    window.addEventListener('resize', fit);\n" +
                "    setTimeout(fit, 0); setTimeout(fit, 60); setTimeout(fit, 200);\n" +
                "    if(window.ResizeObserver){ new ResizeObserver(function(){ fit(); }).observe(wrap); }\n" +
                "  } catch(e) { console && console.log('fit error', e); }\n" +
                "})();";
        try { engine.executeScript(js); } catch (Throwable ignore) {}
        webView.setZoom(1.0);
      }
    });

    webView.widthProperty().addListener((o, a, b) -> {
      try { engine.executeScript("window.__fit && window.__fit()"); } catch (Throwable ignore) {}
    });
    webView.sceneProperty().addListener((o, a, b) -> {
      if (b != null) {
        Platform.runLater(() -> {
          try { engine.executeScript("window.__fit && window.__fit()"); } catch (Throwable ignore) {}
        });
      }
    });
  }
}
