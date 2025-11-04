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
import pe.iep.hsbk.evaluaciones.dao.FirmaDao;
import pe.iep.hsbk.evaluaciones.dao.impl.FirmaDaoImpl;
import pe.iep.hsbk.evaluaciones.enums.Constantes;
import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.Usuario;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;
import pe.iep.hsbk.evaluaciones.util.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static pe.iep.hsbk.evaluaciones.util.Format.formatRoles;

public class SelloController implements SesionAware {

  // Top labels
  @FXML private Label lblTitleScreen;
  @FXML private Label lblTitleProfile;

  // Paneles
  @FXML private AnchorPane left_pane;
  @FXML private AnchorPane right_pane;

  // Formulario
  @FXML private ComboBox<Usuario> usuarioComboBox;
  @FXML private ComboBox<String> estadoComboBox;
  @FXML private Button btnExaminar;
  @FXML private TextField txtRutaPng;
  @FXML private Button btnRegistrar;

  // Búsqueda
  @FXML private TextField txtBuscar;

  // Tabla
  @FXML private TableView<Firma> tblFirma;
  @FXML private TableColumn<Firma, String> colNombres;
  @FXML private TableColumn<Firma, String> colRol;
  @FXML private TableColumn<Firma, String> colEstado;
  @FXML private TableColumn<Firma, Void>   colAcciones;

  // Preview
  @FXML private WebView webView;
  private WebEngine engine;

  // Overlay
  @FXML private StackPane overlay;
  @FXML private ProgressIndicator piMain;

  // Datos
  private final ObservableList<Firma> master = FXCollections.observableArrayList();
  private FilteredList<Firma> filtered;

  // DAO
  private final FirmaDao dao = new FirmaDaoImpl();

  // Edición
  private Firma editing = null;
  private UserSession userSession;

  @Override
  public void setSession(UserSession s) {
    this.userSession = s;
    if (s != null) {
      lblTitleScreen.setText(Constantes.PANEL_SELLO);
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
    engine.loadContent(Constantes.IMG_PREVIEW, "text/html");
  }

  // ===================== Tabla =====================
  private void configurarTabla() {
    colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
    colRol.setCellValueFactory(d -> new SimpleStringProperty((d.getValue().getRol())));
    colEstado.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue().isActivo() ? Constantes.ESTADO_ACTIVO : Constantes.ESTADO_INACTIVO));

    colNombres.setStyle("-fx-alignment: CENTER-LEFT;");
    colEstado.setStyle("-fx-alignment: CENTER;");
    colRol.setStyle("-fx-alignment: CENTER;");

    colAcciones.setCellFactory(col -> new TableCell<>() {

      private final Button btnVer = IconButtons.iconButtonForCell(
          this,
          28, 28, 0.45,
          f -> previsualizarFirma(f),
          new IconButtons.PathSpec(Constantes.view,         "-fx-fill: #F06292; -fx-stroke: transparent;"),
          new IconButtons.PathSpec(Constantes.view_pupila,  "-fx-fill: white;    -fx-stroke: transparent;")
      );

      private final Button btnEdit = IconButtons.iconButtonForCell(
          this,
          28, 28, 0.55,
          f -> cargarEnFormulario(f),
          new IconButtons.PathSpec(Constantes.edit, "-fx-fill: transparent; -fx-stroke: #003B65; -fx-stroke-width: 2;")
      );

      private final HBox box = new HBox(8, btnVer ,btnEdit);
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
    tblFirma.setItems(filtered);
  }

  // ===================== Datos =====================
  private void cargarLista() {
    setBusy(true);
    FXAsync.run(
        () -> {
          try {
            return dao.getListFirmas();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },  // background
        data -> {
          master.setAll(data);
          refiltrar();
          setBusy(false);
        },
        ex -> {
          setBusy(false);
          ex.printStackTrace();
          Dialogs.error(null, "Error", "No se pudo cargar la lista de Firmas.");
        }
    );
  }



  private void refiltrar() {
    final String q = (txtBuscar == null) ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
    filtered.setPredicate(p ->
        q.isEmpty()
            || (p.getNombre()!=null && p.getNombre().toLowerCase().contains(q))
            || (p.getRol()!=null && p.getRol().toLowerCase().contains(q))
            || (p.isActivo() ? "activo" : "desactivo").contains(q)
    );
  }

  // ===================== Handlers =====================
  @FXML private void onBuscar() { refiltrar(); }

  @FXML private void onRecargar() { cargarLista(); }

  @FXML
  private void onExaminar() {
    var w = txtRutaPng.getScene().getWindow();
    FileChooser fc = new FileChooser();
    fc.setTitle("Selecciona imagen de firma (.png)");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
    File f = fc.showOpenDialog(w);
    if (f != null) {
      txtRutaPng.setText(f.getAbsolutePath());
    }
  }

  @FXML
  private void onGuardar() {
    final Usuario sel = usuarioComboBox.getValue();
    final String estado = estadoComboBox.getValue();
    final String ruta   = txtRutaPng.getText() == null ? "" : txtRutaPng.getText().trim();
    final boolean isNew = (editing == null || editing.getId() == null);

    if (sel == null) { Dialogs.warn(null, "Validación", "Selecciona un usuario."); return; }
    if (ruta.isEmpty()) { Dialogs.warn(null, "Validación", "Selecciona la imagen .png de la firma."); return; }

    setBusy(true);

    FXAsync.run(
        () -> {
          // BACKGROUND
          byte[] bytesPng;
          try {
            bytesPng = java.nio.file.Files.readAllBytes(new java.io.File(ruta).toPath());
          } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo PNG", e);
          }

          final Firma f = isNew ? new Firma() : editing;
          f.setUsuarioId(sel.getId());
          f.setActivo(Constantes.ESTADO_ACTIVO.equalsIgnoreCase(estado));
          f.setImagen(bytesPng);

          try {
            if (isNew) dao.guardarFirma(f);
            else dao.actualizarFirma(f);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return f;
        },
        ok -> {
          limpiarForm();
          cargarLista();     // recarga la tabla
          setBusy(false);
          Dialogs.info(null, "Firma registrada", "La firma se guardo correctamente.");
        },
        ex -> {
          setBusy(false);
          ex.printStackTrace();
          Dialogs.error(null, "Error", "No se pudo guardar la firma.");
        }
    );
  }

  private void cargarEnFormulario(Firma f) {
    if (f == null) return;
    editing = f;
    estadoComboBox.setValue(f.isActivo() ? Constantes.ESTADO_ACTIVO : Constantes.ESTADO_INACTIVO);
    txtRutaPng.clear();
  }

  private void previsualizarFirma(Firma f) {
    if (f == null || f.getImagen() == null || f.getImagen().length == 0) {
      engine.loadContent(Constantes.IMG_NOT_FOUND, "text/html");
      return;
    }
    String b64 = java.util.Base64.getEncoder().encodeToString(f.getImagen());
    String html =
        "<html><head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;display:flex;align-items:center;justify-content:center;background:#f6f7f9;height:100vh'>" +
            "<img style='max-width:100%;max-height:100%;image-rendering:crisp-edges' src='data:image/png;base64," + b64 + "'/>" +
            "</body></html>";
    engine.loadContent(html, "text/html");
  }

  private void limpiarForm() {
    editing = null;
    estadoComboBox.setValue(Constantes.ESTADO_ACTIVO);
    txtRutaPng.clear();
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
