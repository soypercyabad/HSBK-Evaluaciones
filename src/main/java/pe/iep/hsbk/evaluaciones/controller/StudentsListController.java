package pe.iep.hsbk.evaluaciones.controller;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import pe.iep.hsbk.evaluaciones.dao.AlumnoDao;
import pe.iep.hsbk.evaluaciones.dao.GradoDao;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.dao.SeccionDao;
import pe.iep.hsbk.evaluaciones.dao.impl.AlumnoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.GradoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.PeriodoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.SeccionDaoImpl;
import pe.iep.hsbk.evaluaciones.enums.Constantes;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Grado;
import pe.iep.hsbk.evaluaciones.model.Seccion;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.IconButtons;
import pe.iep.hsbk.evaluaciones.util.SesionAware;
import pe.iep.hsbk.evaluaciones.util.FXAsync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.Locale;

import static pe.iep.hsbk.evaluaciones.util.Format.formatRoles;

public class StudentsListController implements SesionAware {

  // UI
  @FXML private Label lblTitleUsuario;
  @FXML private Label lblTitlePeriodo;
  @FXML private Label lblTitleRol;
  @FXML private TextField txtBuscar;

  // Tabla
  @FXML private TableView<Alumno> tblAlumnos;
  @FXML private TableColumn<Alumno, Boolean> colSel;
  @FXML private TableColumn<Alumno, String> colApellidos;
  @FXML private TableColumn<Alumno, String> colNombres;
  @FXML private TableColumn<Alumno, String> colCodigo;
  @FXML private TableColumn<Alumno, Void> colAcciones;

  // Contenedores para toggles
  @FXML private HBox paneGrados;
  @FXML private VBox paneSecciones;

  // Overlay de carga (debe existir en el FXML)
  @FXML private StackPane overlay;

  // ToggleGroups
  private final ToggleGroup grpGrados = new ToggleGroup();
  private final ToggleGroup grpSecciones = new ToggleGroup();

  // Sesión / contexto
  private UserSession userSession;
  private Long periodoId; // ej. 2025
  private Long nivelId;   // 1=Primaria, 2=Secundaria

  // Selección actual
  private Long gradoSelId;
  private Long seccionSelId;

  // Datos tabla
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();
  private FilteredList<Alumno> filtered;
  private final Map<Alumno, BooleanProperty> selectedMap = new IdentityHashMap<>();

  // DAOs
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();
  private final GradoDao gradoDao     = new GradoDaoImpl();
  private final SeccionDao seccionDao = new SeccionDaoImpl();
  private final AlumnoDao alumnoDao   = new AlumnoDaoImpl();

  // Caches
  private final Map<String, List<Grado>>   cacheGradosPorPeriodoNivel    = new HashMap<>();
  private final Map<Long,   List<Seccion>> cacheSeccionesPorGrado        = new HashMap<>();
  private final Map<String, List<Alumno>>  cacheAlumnosPorSeccionPeriodo = new HashMap<>();

  // ===================== Ciclo de vida =====================

  @Override
  public void setSession(UserSession s) {
    this.userSession = s;
    if (s != null) {
      lblTitleUsuario.setText("Bienvenido " + s.getNombre() + "!");
      lblTitlePeriodo.setText("Periodo " + s.getPeriodoNombre());
      lblTitleRol.setText(formatRoles(s.getRoles()));
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

      // Solo cargamos cuando también tengamos nivel (lo setea MainController vía setNivelId)
      if (periodoId != null && nivelId != null) {
        cargarGradosAsync();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudo inicializar grados/secciones.");
    }
  }

  // ===================== Busy / Overlay =====================

  private void setBusy(boolean busy) {
    // Deshabilita inputs principales para evitar clics mientras carga
    if (txtBuscar != null) txtBuscar.setDisable(busy);
    if (tblAlumnos != null) tblAlumnos.setDisable(busy);

    if (overlay != null) {
      overlay.setVisible(busy);
      overlay.setManaged(busy);
    }
  }

  // ===================== Cargas asíncronas =====================

  private void cargarGradosAsync() {
    setBusy(true);
    final String key = periodoId + ":" + nivelId;

    FXAsync.run(
        () -> cacheGradosPorPeriodoNivel.computeIfAbsent(key, k -> {
          try { return gradoDao.listarGradosActivos(periodoId, nivelId); }
          catch (Exception e) { throw new RuntimeException(e); }
        }),
        grados -> {
          paneGrados.getChildren().clear();
          grpGrados.getToggles().clear();

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
            onChangeGradoDynamic(); // dispara carga de secciones
          } else {
            paneSecciones.getChildren().clear();
            grpSecciones.getToggles().clear();
            master.clear();
            setBusy(false);
          }
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga de grados", ex.getMessage(), ex);
        }
    );
  }

  private void cargarSeccionesAsync(Long gradoId) {
    setBusy(true);
    FXAsync.run(
        () -> cacheSeccionesPorGrado.computeIfAbsent(gradoId, gid -> {
          try { return seccionDao.listarSeccionesActivas(periodoId, gid); }
          catch (Exception e) { throw new RuntimeException(e); }
        }),
        secciones -> {
          paneSecciones.getChildren().clear();
          grpSecciones.getToggles().clear();

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
            onChangeSeccionDynamic(); // dispara carga de alumnos
          } else {
            master.clear();
            setBusy(false);
          }
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga de secciones", ex.getMessage(), ex);
        }
    );
  }

  private void cargarAlumnosAsync() {
    setBusy(true);
    final String key = seccionSelId + ":" + periodoId;

    FXAsync.run(
        () -> cacheAlumnosPorSeccionPeriodo.computeIfAbsent(key, k -> {
          try {
            return alumnoDao.listarPorSeccionPeriodo(
                Math.toIntExact(seccionSelId),
                Math.toIntExact(periodoId)
            );
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }),
        data -> {
          selectedMap.keySet().retainAll(data); // limpia checks de otra sección
          master.setAll(data);
          refiltrar();
          setBusy(false);
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga de alumnos", ex.getMessage(), ex);
        }
    );
  }

  // ===================== Eventos UI =====================

  private void onChangeGradoDynamic() {
    Toggle sel = grpGrados.getSelectedToggle();
    if (sel == null) return;
    var g = (Grado) ((ToggleButton) sel).getUserData();
    this.gradoSelId = g.getId();
    cargarSeccionesAsync(gradoSelId);
  }

  private void onChangeSeccionDynamic() {
    Toggle sel = grpSecciones.getSelectedToggle();
    if (sel == null) return;
    var s = (Seccion) ((ToggleButton) sel).getUserData();
    this.seccionSelId = s.getId();
    cargarAlumnosAsync();
  }

  // ===================== Tabla / búsqueda =====================

  private void configurarTabla() {
    tblAlumnos.setEditable(true);

    colSel.setCellValueFactory(d ->
        selectedMap.computeIfAbsent(d.getValue(), k -> new SimpleBooleanProperty(false)));

    colSel.setCellFactory(tc -> {
      CheckBoxTableCell<Alumno, Boolean> cell =
          new CheckBoxTableCell<>(index -> {
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

    colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
    colNombres  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
    colCodigo   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
    colApellidos.setStyle("-fx-alignment: CENTER-LEFT;");
    colNombres  .setStyle("-fx-alignment: CENTER-LEFT;");
    colCodigo   .setStyle("-fx-alignment: CENTER-LEFT;");

    colAcciones.setCellFactory(col -> new TableCell<>() {
      private final Button btnEdit = IconButtons.iconButtonForCell(
          this,
          28, 28, 0.55,
          pb -> {
            System.out.println("Editar: " + pb);
            // abrir nueva ventana de edición

            Dialogs.info(null, "Editar Alumno", "Se esta editando al alumno: " + pb.getNombres() + " " + pb.getApellidos() + ". - Código: " + pb.getId());
          },
          new IconButtons.PathSpec(Constantes.edit, "-fx-fill: transparent; -fx-stroke: #003B65; -fx-stroke-width: 2;")
      );

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btnEdit);
        setAlignment(Pos.CENTER);
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

  // ===================== Handlers =====================

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

    cacheGradosPorPeriodoNivel.clear();
    cacheSeccionesPorGrado.clear();
    cacheAlumnosPorSeccionPeriodo.clear();

    if (this.periodoId != null) {
      cargarGradosAsync();
    }
  }
}
