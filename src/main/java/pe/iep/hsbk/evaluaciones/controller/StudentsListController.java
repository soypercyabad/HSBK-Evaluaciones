package pe.iep.hsbk.evaluaciones.controller;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import pe.iep.hsbk.evaluaciones.dao.AlumnoDao;
import pe.iep.hsbk.evaluaciones.dao.GradoDao;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.dao.SeccionDao;
import pe.iep.hsbk.evaluaciones.dao.impl.AlumnoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.GradoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.PeriodoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.SeccionDaoImpl;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Grado;
import pe.iep.hsbk.evaluaciones.model.Seccion;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.Locale;

public class StudentsListController implements SesionAware {

  // UI
  @FXML private Label lblTitleUsuario;
  @FXML private Label lblTitlePeriodo;
  @FXML private TextField txtBuscar;

  // Tabla ahora usa Alumno
  @FXML private TableView<Alumno> tblAlumnos;
  @FXML private TableColumn<Alumno, Boolean> colSel;
  @FXML private TableColumn<Alumno, String> colApellidos;
  @FXML private TableColumn<Alumno, String> colNombres;
  @FXML private TableColumn<Alumno, String> colCodigo;
  @FXML private TableColumn<Alumno, Void> colAcciones;

  // Contenedores para toggles dinámicos
  @FXML private HBox paneGrados;
  @FXML private VBox paneSecciones;

  // ToggleGroups
  private final ToggleGroup grpGrados = new ToggleGroup();
  private final ToggleGroup grpSecciones = new ToggleGroup();

  // Sesión
  private UserSession userSession;
  private Long periodoId; // 2025
  private Long nivelId;   // 1=Primaria, 2=Secundaria

  // Selecciones actuales
  private Long gradoSelId;
  private Long seccionSelId;

  // Datos
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();
  private FilteredList<Alumno> filtered;
  private final Map<Alumno, BooleanProperty> selectedMap = new IdentityHashMap<>();

  // DAOs
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();
  private final GradoDao gradoDao = new GradoDaoImpl();
  private final SeccionDao seccionDao = new SeccionDaoImpl();
  private final AlumnoDao alumnoDao = new AlumnoDaoImpl();

  // Caches
  private final Map<String, List<Grado>>   cacheGradosPorPeriodoNivel   = new HashMap<>();
  private final Map<Long,   List<Seccion>> cacheSeccionesPorGrado       = new HashMap<>();
  private final Map<String, List<Alumno>>  cacheAlumnosPorSeccionPeriodo= new HashMap<>();

  @Override
  public void setSession(UserSession s) {
    this.userSession = s;
    if (s != null) {
      lblTitleUsuario.setText("Bienvenido " + s.getNombre() + "!");
      lblTitlePeriodo.setText("Periodo: " + s.getPeriodoNombre());
    }
  }

  @FXML
  public void initialize() {
    configurarTabla();

    // búsqueda texto
    if (txtBuscar != null) {
      txtBuscar.textProperty().addListener((o, a, b) -> refiltrar());
    }

    try {
      String perNombre = (userSession != null && userSession.getPeriodoNombre() != null)
          ? userSession.getPeriodoNombre()
          : "2025";
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      // Solo poblar si YA tenemos nivel (lo setea MainController vía setNivelId)
      if (periodoId != null && nivelId != null) {
        poblarGrados();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudo inicializar grados/secciones.");
    }
  }

  // ======== UI dinámica: grados / secciones ========
  private void poblarGrados() throws Exception {
    paneGrados.getChildren().clear();
    grpGrados.getToggles().clear();

    String key = periodoId + ":" + nivelId;
    var grados = cacheGradosPorPeriodoNivel.computeIfAbsent(key, k -> {
      try { return gradoDao.listarGradosActivos(periodoId, nivelId); }
      catch (Exception e) { throw new RuntimeException(e); }
    });

    for (var g : grados) {
      ToggleButton tb = new ToggleButton(g.getNombre());
      tb.getStyleClass().add("tab");
      tb.setUserData(g);
      tb.setToggleGroup(grpGrados);
      tb.setOnAction(e -> onChangeGradoDynamic());
      paneGrados.getChildren().add(tb);
    }

    if (!grados.isEmpty() && !grpGrados.getToggles().isEmpty()) {
      grpGrados.selectToggle(grpGrados.getToggles().get(0));
      onChangeGradoDynamic();
    } else {
      paneSecciones.getChildren().clear();
      grpSecciones.getToggles().clear();
      master.clear();
    }
  }

  private void onChangeGradoDynamic() {
    Toggle sel = grpGrados.getSelectedToggle();
    if (sel == null) return;
    var g = (Grado) ((ToggleButton) sel).getUserData();
    this.gradoSelId = g.getId();

    try {
      poblarSecciones(gradoSelId);
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudieron cargar secciones.");
    }
  }

  private void poblarSecciones(Long gradoId) throws Exception {
    paneSecciones.getChildren().clear();
    grpSecciones.getToggles().clear();

    var secciones = cacheSeccionesPorGrado.computeIfAbsent(gradoId, gid -> {
      try { return seccionDao.listarSeccionesActivas(periodoId, gid); }
      catch (Exception e) { throw new RuntimeException(e); }
    });

    for (var s : secciones) {
      ToggleButton tb = new ToggleButton("Sección " + s.getNombre());
      tb.getStyleClass().add("section-btn");
      tb.setUserData(s);
      tb.setToggleGroup(grpSecciones);
      tb.setOnAction(e -> onChangeSeccionDynamic());
      paneSecciones.getChildren().add(tb);
    }

    if (!secciones.isEmpty() && !grpSecciones.getToggles().isEmpty()) {
      grpSecciones.selectToggle(grpSecciones.getToggles().get(0));
      onChangeSeccionDynamic();
    } else {
      master.clear();
    }
  }

  private void onChangeSeccionDynamic() {
    Toggle sel = grpSecciones.getSelectedToggle();
    if (sel == null) return;
    var s = (Seccion) ((ToggleButton) sel).getUserData();
    this.seccionSelId = s.getId();
    cargarAlumnos();
  }

  private void cargarAlumnos() {
    try {
      String key = seccionSelId + ":" + periodoId;
      var data = cacheAlumnosPorSeccionPeriodo.computeIfAbsent(key, k -> {
        try {
          return alumnoDao.listarPorSeccionPeriodo(
              Math.toIntExact(seccionSelId), Math.toIntExact(periodoId));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      // Evitar que queden checks de otra sección
      selectedMap.keySet().retainAll(data);
      master.setAll(data);
      refiltrar();
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudieron cargar alumnos.");
    }
  }

  // ======== Tabla / búsqueda ========
  private void configurarTabla() {
    tblAlumnos.setEditable(true);

    // checkbox por fila usando un mapa (no creamos otro modelo)
    colSel.setCellValueFactory(d -> selectedMap.computeIfAbsent(d.getValue(), k -> new SimpleBooleanProperty(false)));
    colSel.setCellFactory(tc -> {
      CheckBoxTableCell<Alumno, Boolean> cell = new CheckBoxTableCell<>(index -> {
        if (index >= 0 && index < tblAlumnos.getItems().size()) {
          Alumno a = tblAlumnos.getItems().get(index);
          return selectedMap.computeIfAbsent(a, k -> new SimpleBooleanProperty(false));
        }
        return new SimpleBooleanProperty(false);
      });
      cell.setAlignment(Pos.CENTER);
      return cell;
    });
    colSel.setSortable(false);
    colSel.setPrefWidth(50);
    CheckBox chkAll = new CheckBox();
    chkAll.setOnAction(e -> {
      boolean v = chkAll.isSelected();
      for (Alumno a : tblAlumnos.getItems()) {
        selectedMap.computeIfAbsent(a, k -> new SimpleBooleanProperty(false)).set(v);
      }
    });
    colSel.setGraphic(chkAll);

    // columnas de texto (usando tu modelo Alumno)
    colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
    colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
    colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
    colApellidos.setStyle("-fx-alignment: CENTER-LEFT;");
    colNombres.setStyle("-fx-alignment: CENTER-LEFT;");
    colCodigo.setStyle("-fx-alignment: CENTER-LEFT;");

    // botón acciones
    colAcciones.setCellFactory(col -> new TableCell<>() {
      private final Button btn = buildEditButton();
      @Override protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
        setAlignment(Pos.CENTER);
      }
      private Button buildEditButton() {
        SVGPath p = new SVGPath();
        p.setContent("M3,14 L10,7 13,10 6,17 3,17z M10,6 L12,4 15,7 13,9z");
        Button b = new Button();
        b.getStyleClass().add("icon-btn");
        b.setGraphic(new HBox(p));
        b.setOnAction(e -> {
          Alumno row = getTableView().getItems().get(getIndex());
          System.out.println("Editar: " + row);
        });
        return b;
      }
    });

    filtered = new FilteredList<>(master, a -> true);
    tblAlumnos.setItems(filtered);
  }

  private void refiltrar() {
    final String q = (txtBuscar == null) ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
    filtered.setPredicate(a ->
        q.isEmpty()
            || (a.getApellidos()!=null && a.getApellidos().toLowerCase().contains(q))
            || (a.getNombres()!=null   && a.getNombres().toLowerCase().contains(q))
            || (a.getCodigo()!=null    && a.getCodigo().toLowerCase().contains(q))
    );
  }

  // ==== Handlers ====
  @FXML private void onBuscar() { refiltrar(); }

  @FXML
  private void onDescargar() {
    try {
      File out = new File(System.getProperty("user.home"), "alumnos_seleccion.csv");
      try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
        w.write("Apellidos,Nombres,Codigo\n");
        for (Alumno a : tblAlumnos.getItems()) {
          if (selectedMap.getOrDefault(a, new SimpleBooleanProperty(false)).get()) {
            w.write(String.format("%s,%s,%s%n", a.getApellidos(), a.getNombres(), a.getCodigo()));
          }
        }
      }
      Dialogs.info(null, "Descarga Completada", "El archivo se ha generado en:\n" + out.getAbsolutePath());
    } catch (Exception e) { e.printStackTrace(); }
  }

  // Llamado por el menú
  public void setNivelId(long nivelId) {
    if (Objects.equals(this.nivelId, nivelId)) return;
    this.nivelId = nivelId;

    // invalidar caches
    cacheGradosPorPeriodoNivel.clear();
    cacheSeccionesPorGrado.clear();
    cacheAlumnosPorSeccionPeriodo.clear();

    if (this.periodoId != null) {
      try {
        poblarGrados();
      } catch (Exception e) {
        e.printStackTrace();
        Dialogs.error(null, "Error", "No se pudo inicializar grados/secciones.");
      }
    }
  }
}
