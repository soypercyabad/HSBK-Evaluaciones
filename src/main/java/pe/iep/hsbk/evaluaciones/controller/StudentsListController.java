package pe.iep.hsbk.evaluaciones.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import pe.iep.hsbk.evaluaciones.dao.*;
import pe.iep.hsbk.evaluaciones.dao.impl.*;
import pe.iep.hsbk.evaluaciones.dto.RolesEnSeccionDto;
import pe.iep.hsbk.evaluaciones.enums.Constantes;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Bimestre;
import pe.iep.hsbk.evaluaciones.model.Grado;
import pe.iep.hsbk.evaluaciones.model.Seccion;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;
import pe.iep.hsbk.evaluaciones.service.BoletaPdfService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.IconButtons;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.io.File;
import java.util.*;

import static pe.iep.hsbk.evaluaciones.util.Format.formatRoles;

public class StudentsListController implements SesionAware {

  // ===================== UI (FXML) =====================
  @FXML private Label lblTitleUsuario;
  @FXML private Label lblTitlePeriodo;
  @FXML private Label lblTitleRol;
  @FXML private TextField txtBuscar;
  @FXML private ComboBox<Bimestre> bimestreComboBox;
  @FXML private Button btnDescargarBoleta;

  // ===================== Tabla =====================
  @FXML private TableView<Alumno> tblAlumnos;
  @FXML private TableColumn<Alumno, Boolean> colSel;
  @FXML private TableColumn<Alumno, String> colApellidos;
  @FXML private TableColumn<Alumno, String> colNombres;
  @FXML private TableColumn<Alumno, String> colCodigo;
  @FXML private TableColumn<Alumno, Void> colAcciones;

  // ===================== Contenedores (toggles) =====================
  @FXML private HBox paneGrados;
  @FXML private VBox paneSecciones;

  // ===================== Overlay busy =====================
  @FXML private StackPane overlay;

  // ===================== ToggleGroups =====================
  private final ToggleGroup grpGrados = new ToggleGroup();
  private final ToggleGroup grpSecciones = new ToggleGroup();

  // ===================== Sesión / Contexto =====================
  private UserSession userSession;
  private Long periodoId;
  private Long nivelId;
  private java.util.function.BiConsumer<Constantes.Route, java.util.function.Consumer<Object>> goTo;

  private boolean contextInitialized = false;

  // ===================== Selección actual =====================
  private Long gradoSelId;
  private Long seccionSelId;

  // ===================== Restauración de vista =====================
  private Long gradoIdAReponer;
  private Long seccionIdAReponer;
  private boolean restaurando = false;

  // ===================== Datos Tabla =====================
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();
  private FilteredList<Alumno> filtered;
  private final Map<Alumno, BooleanProperty> selectedMap = new IdentityHashMap<>();

  // ===================== DAOs =====================
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();
  private final GradoDao gradoDao = new GradoDaoImpl();
  private final SeccionDao seccionDao = new SeccionDaoImpl();
  private final AlumnoDao alumnoDao = new AlumnoDaoImpl();
  private final BimestreDao bimestreDao = new BimestreDaoImpl();

  // DAOs específicos para boleta
  private final PlantillaBoletaDao plantillaBoletaDao = new PlantillaDaoImpl();
  private final BoletaDatasetDao boletaDatasetDao = new BoletaDatasetDaoImpl();
  private final FirmaDao firmaDao = new FirmaDaoImpl();
  private final SelloDao selloDao = new SelloDaoImpl();

  // Service de boletas (usa los DAOs de arriba)
  private final BoletaPdfService boletaPdfService =
      new BoletaPdfService(plantillaBoletaDao, boletaDatasetDao, firmaDao, selloDao);

  // ===================== Caches =====================
  private final Map<String, List<Grado>> cacheGradosPorPeriodoNivelUsuario = new HashMap<>();
  private final Map<Long, List<Seccion>> cacheSeccionesPorGradoUsuario = new HashMap<>();
  private final Map<String, List<Alumno>> cacheAlumnosPorSeccionPeriodo = new HashMap<>();
  private final Map<Long, RolesEnSeccionDto> cacheRolesPorSeccion = new HashMap<>();

  // ===================== Ciclo de vida =====================
  @Override
  public void setSession(UserSession s) {
    this.userSession = s;
    if (s != null) {
      lblTitleUsuario.setText("Bienvenido " + s.getNombre() + "!");
      lblTitlePeriodo.setText("Periodo " + s.getPeriodoNombre());
      lblTitleRol.setText(formatRoles(s.getRoles()));
    }
    tryInitContext();
  }

  @FXML
  public void initialize() {
    configurarTabla();
    configurarComboBimestres();

    if (txtBuscar != null) {
      txtBuscar.textProperty().addListener((o, a, b) -> refiltrar());
    }
  }

  // ===================== Init "perezoso" =====================
  private void tryInitContext() {
    if (contextInitialized) return;
    if (userSession == null) return;
    if (nivelId == null) return;

    String perNombre = userSession.getPeriodoNombre();
    if (perNombre == null) {
      Dialogs.error(null, "Error", "No se encontró el período en la sesión de usuario.");
      return;
    }

    try {
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      if (periodoId != null) {
        contextInitialized = true;
        cargarGradosAsync();
        cargarBimestresAsync();  // <-- llena el combo
      }
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudo inicializar grados/secciones.");
    }
  }

  // ===================== Busy / Overlay =====================
  private void setBusy(boolean busy) {
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
    final String key = userSession.getUserId() + ":" + periodoId + ":" + nivelId;

    FXAsync.run(
        () -> cacheGradosPorPeriodoNivelUsuario.computeIfAbsent(key, k -> {
          try {
            // SP: grados por periodo, nivel y usuario
            return gradoDao.listarGradosActivosPorUsuario(periodoId, nivelId, userSession.getUserId());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }),
        grados -> {
          paneGrados.getChildren().clear();
          grpGrados.getToggles().clear();

          for (Grado g : grados) {
            ToggleButton tb = new ToggleButton(g.getNombre());
            tb.getStyleClass().add("tab");
            tb.setUserData(g);
            tb.setToggleGroup(grpGrados);
            tb.setOnAction(e -> onChangeGradoDynamic());
            paneGrados.getChildren().add(tb);
          }

          Toggle toSelect = null;

          // Si estamos restaurando, intentamos seleccionar el grado original
          if (restaurando && gradoIdAReponer != null) {
            for (Toggle t : grpGrados.getToggles()) {
              Grado g = (Grado) ((ToggleButton) t).getUserData();
              if (g != null && Objects.equals(g.getId(), gradoIdAReponer)) {
                toSelect = t;
                break;
              }
            }
          }

          // Si no encontramos el grado deseado, seleccionamos el primero
          if (toSelect == null && !grpGrados.getToggles().isEmpty()) {
            toSelect = grpGrados.getToggles().get(0);
          }

          if (toSelect != null) {
            grpGrados.selectToggle(toSelect);
            onChangeGradoDynamic(); // esto disparará cargarSeccionesAsync()
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
        () -> cacheSeccionesPorGradoUsuario.computeIfAbsent(gradoId, gid -> {
          try {
            // SP: secciones por periodo, grado y usuario
            return seccionDao.listarSeccionesActivasPorUsuario(periodoId, gid, userSession.getUserId());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }),
        secciones -> {
          paneSecciones.getChildren().clear();
          grpSecciones.getToggles().clear();

          for (Seccion s : secciones) {
            ToggleButton tb = new ToggleButton("Sección " + s.getNombre());
            tb.getStyleClass().add("section-btn");
            tb.setUserData(s);
            tb.setToggleGroup(grpSecciones);
            tb.setOnAction(e -> onChangeSeccionDynamic());
            paneSecciones.getChildren().add(tb);
          }

          Toggle toSelect = null;

          // Si estamos restaurando, intentamos seleccionar la sección original
          if (restaurando && seccionIdAReponer != null) {
            for (Toggle t : grpSecciones.getToggles()) {
              Seccion s = (Seccion) ((ToggleButton) t).getUserData();
              if (s != null && Objects.equals(s.getId(), seccionIdAReponer)) {
                toSelect = t;
                break;
              }
            }
          }

          // Si no encontramos la sección deseada, seleccionamos la primera
          if (toSelect == null && !grpSecciones.getToggles().isEmpty()) {
            toSelect = grpSecciones.getToggles().get(0);
          }

          if (toSelect != null) {
            grpSecciones.selectToggle(toSelect);
            onChangeSeccionDynamic();
          } else {
            master.clear();
            setBusy(false);
          }

          // Una vez restaurada la vista, limpiamos flags
          restaurando = false;
          gradoIdAReponer = null;
          seccionIdAReponer = null;
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
            return alumnoDao.listarPorSeccionPeriodo(Math.toIntExact(seccionSelId), Math.toIntExact(periodoId));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }),
        data -> {
          selectedMap.keySet().retainAll(data);
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

  private void cargarBimestresAsync() {
    if (bimestreComboBox == null) return;
    setBusy(true);

    FXAsync.run(
        () -> {
          try {
            return bimestreDao.listarBimestres(periodoId);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        bimestres -> {
          bimestreComboBox.getItems().clear();
          if (bimestres != null) {
            bimestreComboBox.getItems().addAll(bimestres);
          }
          if (!bimestres.isEmpty()) {
            bimestreComboBox.getSelectionModel().select(0);
          }
          setBusy(false);
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga de bimestres", ex.getMessage(), ex);
        }
    );
  }

  // ===================== Precarga de roles =====================
  private void precargarRolesSeccionAsync(Long seccionId) {
    if (seccionId == null || periodoId == null || userSession == null) return;
    if (cacheRolesPorSeccion.containsKey(seccionId)) return; // ya está cacheado

    FXAsync.run(
        () -> {
          try {
            return seccionDao.getRolesEnSeccion(periodoId, seccionId, userSession.getUserId());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        roles -> {
          cacheRolesPorSeccion.put(seccionId, roles);

          boolean esTut = roles != null && roles.isTutor();
          if (bimestreComboBox != null) {
            bimestreComboBox.setVisible(esTut);
            bimestreComboBox.setManaged(esTut);
          }

          if (btnDescargarBoleta != null) {
            btnDescargarBoleta.setVisible(esTut);
            btnDescargarBoleta.setManaged(esTut);
          }
        },
        ex -> {
          // No molestamos al usuario aquí; solo log
          ex.printStackTrace();
        }
    );
  }

  // ===================== Eventos UI =====================
  private void onChangeGradoDynamic() {
    Toggle sel = grpGrados.getSelectedToggle();
    if (sel == null) return;
    Grado g = (Grado) ((ToggleButton) sel).getUserData();
    this.gradoSelId = g.getId();
    cargarSeccionesAsync(gradoSelId);
  }

  private void onChangeSeccionDynamic() {
    Toggle sel = grpSecciones.getSelectedToggle();
    if (sel == null) return;
    Seccion s = (Seccion) ((ToggleButton) sel).getUserData();
    this.seccionSelId = s.getId();

    // Pre-carga roles de la sección en segundo plano
    precargarRolesSeccionAsync(this.seccionSelId);

    // Luego carga alumnos
    cargarAlumnosAsync();
  }

  // ===================== Tabla / Búsqueda =====================
  private void configurarTabla() {
    tblAlumnos.setEditable(true);

    colSel.setCellValueFactory(d ->
        selectedMap.computeIfAbsent(d.getValue(), k -> new SimpleBooleanProperty(false)));

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

    colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
    colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
    colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
    colApellidos.setStyle("-fx-alignment: CENTER-LEFT;");
    colNombres.setStyle("-fx-alignment: CENTER-LEFT;");
    colCodigo.setStyle("-fx-alignment: CENTER;");

    colAcciones.setCellFactory(col -> new TableCell<Alumno, Void>() {
      private final Button btnEdit = IconButtons.iconButtonForCell(
          this, 28, 28, 0.55,
          pb -> {
            if (pb != null) {
              Node graphic = getGraphic();
              abrirSegunRol(pb, (Button) graphic);
            }
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

  private void configurarComboBimestres() {
    if (bimestreComboBox == null) return;

    // Items del desplegable
    bimestreComboBox.setCellFactory(listView -> new ListCell<>() {
      @Override
      protected void updateItem(Bimestre item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item.getNumero() + "º Bimestre");
        }
      }
    });

    // Item seleccionado (lo que se ve en la caja del combo)
    bimestreComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Bimestre item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item.getNumero() + "º Bimestre");
        }
      }
    });

    // Converter por si se usa setValue desde código
    bimestreComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(Bimestre item) {
        if (item == null) return "";
        return item.getNumero() + "º Bimestre";
      }

      @Override
      public Bimestre fromString(String string) {
        // No lo usamos
        return null;
      }
    });
  }

  private void refiltrar() {
    final String q = (txtBuscar == null) ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
    filtered.setPredicate(a ->
        q.isEmpty()
            || (a.getApellidos() != null && a.getApellidos().toLowerCase().contains(q))
            || (a.getNombres() != null && a.getNombres().toLowerCase().contains(q))
            || (a.getCodigo() != null && a.getCodigo().toLowerCase().contains(q))
    );
  }

  // ===================== Navegación (abrir vistas) =====================
  private void abrirSegunRol(Alumno alumno, Button anchor) {
    if (goTo == null) {
      Dialogs.error(null, "Error", "Navegación no disponible.");
      return;
    }
    if (userSession == null) {
      Dialogs.error(null, "Error", "Sesión no disponible.");
      return;
    }
    if (seccionSelId == null || periodoId == null) {
      Dialogs.warn(null, "Falta contexto", "Selecciona una sección.");
      return;
    }

    // Intentar usar cache primero
    RolesEnSeccionDto cached = cacheRolesPorSeccion.get(seccionSelId);
    if (cached != null) {
      manejarSegunRoles(alumno, anchor, cached);
      return;
    }

    // Si no hay cache consultar BD async
    FXAsync.run(
        () -> {
          try {
            return seccionDao.getRolesEnSeccion(periodoId, seccionSelId, userSession.getUserId());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        roles -> {
          cacheRolesPorSeccion.put(seccionSelId, roles);
          manejarSegunRoles(alumno, anchor, roles);
        },
        ex -> {
          Dialogs.errorConStacktrace(null, "Error", "No se pudo obtener tus permisos en esta sección.", ex.getMessage(), ex);
        }
    );
  }

  private void manejarSegunRoles(Alumno alumno, Button anchor, RolesEnSeccionDto roles) {
    boolean esDoc = roles != null && roles.isDocente();
    boolean esTut = roles != null && roles.isTutor();

    if (esDoc && !esTut) {
      abrirDetalleAlumno(alumno, nivelId, seccionSelId);

    } else if (!esDoc && esTut) {
      abrirConductaAlumno(alumno, nivelId);

    } else if (esDoc && esTut) {
      ContextMenu menu = new ContextMenu();
      MenuItem miNotas = new MenuItem("Ver Notas");
      MenuItem miConducta = new MenuItem("Ver Conducta");

      System.out.println("Linea 533 → Alumno seleccionado: " + alumno.getNombres() + " " + alumno.getApellidos());
      System.out.println("Linea 534 → NivelId: " + nivelId + ", SeccionId: " + seccionSelId + ", GradoId: " + gradoSelId);
      miNotas.setOnAction(e -> abrirDetalleAlumno(alumno, nivelId, seccionSelId));
      miConducta.setOnAction(e -> abrirConductaAlumno(alumno, nivelId));

      menu.getItems().addAll(miNotas, miConducta);
      menu.getStyleClass().add("role-menu");
      menu.show(anchor, Side.BOTTOM, 0, 0);
    } else {
      Dialogs.warn(null, "Sin permisos", "No tienes permisos en esta sección.");
    }
  }

  private void abrirDetalleAlumno(Alumno alumno, Long nivelId, Long seccionId) {
    if (goTo == null) {
      Dialogs.error(null, "Error", "Navegación no disponible.");
      return;
    }

    setBusy(true);

    try {
      goTo.accept(Constantes.Route.STUDENT_NOTAS, ctrlObj -> {
        if (ctrlObj instanceof AlumnoNotasController) {
          AlumnoNotasController ctrl = (AlumnoNotasController) ctrlObj;
          ctrl.setSession(userSession);
          System.out.println("Linea 559 → NivelId: " + nivelId + ", SeccionId: " + seccionSelId + ", GradoId: " + gradoSelId);
          ctrl.setAlumno(alumno, nivelId, seccionId);

          final Long gradoIdActual = this.gradoSelId;
          final Long seccionIdActual = this.seccionSelId;
          final java.util.function.BiConsumer<Constantes.Route, java.util.function.Consumer<Object>> goToRef = this.goTo;
          final UserSession sesRef = this.userSession;
          final Long nivelRef = nivelId;

          System.out.println("Linea 568 → Configurando onBack: " + goToRef);
          System.out.println("Linea 569 → gradoIdActual: " + gradoIdActual + ", seccionIdActual: " + seccionIdActual + ", nivelRef: " + nivelRef + ", GradoIdActual: " + gradoIdActual);

          ctrl.setOnBack(() -> {
            goToRef.accept(Constantes.Route.STUDENTS_LIST, backCtrl -> {
              if (backCtrl instanceof StudentsListController) {
                StudentsListController listCtrl = (StudentsListController) backCtrl;
                listCtrl.setGoTo(goToRef);
                listCtrl.setSession(sesRef);
                listCtrl.restaurarVista(nivelRef, gradoIdActual, seccionIdActual);
              }
            });
          });

        } else {
          Dialogs.error(null, "Error", "El controlador no es del tipo esperado.");
        }
        setBusy(false);
      });
    } catch (Exception ex) {
      setBusy(false);
      Dialogs.errorConStacktrace(null, "Error", "No se pudo navegar a la pantalla de notas.", ex.getMessage(), ex);
    }
  }

  private void abrirConductaAlumno(Alumno alumno, Long nivelId) {
    if (goTo == null) {
      Dialogs.error(null, "Error", "Navegación no disponible.");
      return;
    }

    goTo.accept(Constantes.Route.STUDENT_CONDUCTA, ctrlObj -> {
      if (ctrlObj instanceof AlumnoConductaController) {
        AlumnoConductaController ctrl = (AlumnoConductaController) ctrlObj;
        ctrl.setSession(userSession);
        ctrl.setAlumno(alumno, nivelId);

        final Long gradoIdActual = this.gradoSelId;
        final Long seccionIdActual = this.seccionSelId;
        final java.util.function.BiConsumer<Constantes.Route, java.util.function.Consumer<Object>> goToRef = this.goTo;
        final UserSession sesRef = this.userSession;
        final Long nivelRef = nivelId;

        ctrl.setOnBack(() -> {
          goToRef.accept(Constantes.Route.STUDENTS_LIST, backCtrl -> {
            if (backCtrl instanceof StudentsListController) {
              StudentsListController listCtrl = (StudentsListController) backCtrl;
              listCtrl.setGoTo(goToRef);
              listCtrl.setSession(sesRef);
              listCtrl.restaurarVista(nivelRef, gradoIdActual, seccionIdActual);
            }
          });
        });

      } else {
        Dialogs.error(null, "Error", "El controlador no es del tipo esperado.");
      }
    });
  }

  // ===================== Handlers =====================
  @FXML
  private void onBuscar() {
    refiltrar();
  }

  @FXML
  private void onDescargar() {
    try {

      List<Alumno> seleccionados = new ArrayList<>();
      for (Alumno a : tblAlumnos.getItems()) {
        if (selectedMap.getOrDefault(a, new SimpleBooleanProperty(false)).get()) {
          seleccionados.add(a);
        }
      }

      if (seleccionados.isEmpty()) {
        Dialogs.warn(null, "Sin selección", "Selecciona al menos un alumno para generar la boleta.");
        return;
      }

      if (periodoId == null || seccionSelId == null || nivelId == null) {
        Dialogs.warn(null, "Falta contexto", "Selecciona grado/sección y asegúrate que el período esté definido.");
        return;
      }

      int bimestreNum = obtenerBimestreActual();

      Bimestre b = bimestreDao.estadoBimestre( bimestreNum, periodoId);
      if (b == null || b.isAbierto()) {
        Dialogs.warn(null, "Bimestre no cerrado",
            "No se pueden generar boletas para el " + bimestreNum + "º bimestre\n" +
                "porque aún no ha sido cerrado.");
        return;
      }

      FileChooser fc = new FileChooser();
      fc.setTitle("Guardar boleta(s)");

      File destino;
      if (seleccionados.size() == 1) {
        Alumno a = seleccionados.get(0);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        fc.setInitialFileName(formatearNombreArchivoAlumno(a) + ".pdf");
        destino = fc.showSaveDialog(tblAlumnos.getScene().getWindow());
      } else {
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo ZIP", "*.zip"));
        String nombreZip = construirNombreZipAula();
        fc.setInitialFileName(nombreZip + ".zip");
        destino = fc.showSaveDialog(tblAlumnos.getScene().getWindow());
      }

      if (destino == null) {
        return;
      }

      setBusy(true);

      FXAsync.run(
          () -> {
            try {
              boletaPdfService.generarBoletas(
                  periodoId,
                  seccionSelId,
                  nivelId,
                  bimestreNum,
                  seleccionados,
                  userSession,
                  destino
              );
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            return null;
          },
          ok -> {
            setBusy(false);
            Dialogs.info(null, "Boleta(s) generadas",
                "El archivo se ha generado en:\n" + destino.getAbsolutePath());
          },
          ex -> {
            setBusy(false);
            Dialogs.errorConStacktrace(
                null,
                "Error al generar boletas",
                "Ocurrió un problema al generar las boletas.",
                ex.getMessage(), ex
            );
          }
      );

    } catch (Exception e) {
      setBusy(false);
      e.printStackTrace();
      Dialogs.errorConStacktrace(
          null,
          "Error inesperado",
          "No se pudo generar las boletas.",
          e.getMessage(), e
      );
    }
  }

  private String formatearNombreArchivoAlumno(Alumno a) {
    String base = (a.getApellidos() + "_" + a.getNombres())
        .trim()
        .replace(" ", "_")
        .replaceAll("[^A-Za-z0-9_ÁÉÍÓÚÑáéíóú]", "");
    if (base.isEmpty()) base = "alumno";
    return base;
  }

  private String construirNombreZipAula() {
    try {
      String peridoNombre = userSession.getPeriodoNombre();
      String nombreZip = boletaDatasetDao.obtenerNombreZip(nivelId, seccionSelId, gradoSelId);
      return nombreZip + "_" + peridoNombre;
    } catch (Exception e) {
      e.printStackTrace();
      return "boletas_aula_" + System.currentTimeMillis();
    }
  }

  private int obtenerBimestreActual() {
    if (bimestreComboBox == null) return 1;
    Bimestre sel = bimestreComboBox.getSelectionModel().getSelectedItem();
    if (sel != null) {
      return sel.getNumero();
    }
    return 1;
  }

  // ===================== Router =====================
  public void setGoTo(java.util.function.BiConsumer<Constantes.Route, java.util.function.Consumer<Object>> goTo) {
    this.goTo = goTo;
  }

  /** Llamado por el menú para establecer nivel y recargar. */
  public void setNivelId(long nivelId) {
    if (Objects.equals(this.nivelId, nivelId)) {
      // Si el nivel es el mismo, no tocamos caches ni recargamos contexto
      return;
    }
    this.nivelId = nivelId;

    cacheGradosPorPeriodoNivelUsuario.clear();
    cacheSeccionesPorGradoUsuario.clear();
    cacheAlumnosPorSeccionPeriodo.clear();
    cacheRolesPorSeccion.clear();

    contextInitialized = false;
    tryInitContext();
  }

  /** Restaurar la vista al volver desde otra pantalla.  */
  public void restaurarVista(Long nivelId, Long gradoId, Long seccionId) {
    System.out.println("Linea 783 → Restaurando vista: nivelId=" + nivelId + ", gradoId=" + gradoId + ", seccionId=" + seccionId);

    // Marcamos que estamos en modo restauración
    this.restaurando = true;
    this.gradoIdAReponer = gradoId;
    this.seccionIdAReponer = seccionId;

    // Si el nivel es distinto, disparamos el flujo normal de carga (async)
    if (!Objects.equals(this.nivelId, nivelId)) {
      setNivelId(nivelId);
      return;
    }

    // Si el nivel es el mismo y ya estaba inicializado, intentamos seleccionar directamente
    if (contextInitialized) {
      // Seleccionar grado
      Toggle gradoToggle = null;
      for (Toggle t : grpGrados.getToggles()) {
        Grado g = (Grado) ((ToggleButton) t).getUserData();
        if (g != null && Objects.equals(g.getId(), gradoId)) {
          gradoToggle = t;
          break;
        }
      }
      if (gradoToggle != null) {
        grpGrados.selectToggle(gradoToggle);
        onChangeGradoDynamic(); // esto recargará/seleccionará secciones según la lógica de restauración
      } else if (!grpGrados.getToggles().isEmpty()) {
        grpGrados.selectToggle(grpGrados.getToggles().get(0));
        onChangeGradoDynamic();
      }
    } else {
      // Si por alguna razón aún no está inicializado, deja que tryInitContext lo maneje
      tryInitContext();
    }
  }
}
