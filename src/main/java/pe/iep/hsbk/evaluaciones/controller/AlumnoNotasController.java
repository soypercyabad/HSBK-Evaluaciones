package pe.iep.hsbk.evaluaciones.controller;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pe.iep.hsbk.evaluaciones.dao.AlumnoDao;
import pe.iep.hsbk.evaluaciones.dao.BimestreDao;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.dao.SeccionDao;
import pe.iep.hsbk.evaluaciones.dao.impl.AlumnoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.BimestreDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.PeriodoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.SeccionDaoImpl;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Bimestre;
import pe.iep.hsbk.evaluaciones.model.Seccion;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.util.*;

import static pe.iep.hsbk.evaluaciones.util.Format.formatRoles;

public class AlumnoNotasController implements SesionAware {
  // UI
  @FXML
  private Label lblTitleUsuario;
  @FXML private Label lblTitlePeriodo;
  @FXML private Label lblTitleRol;

  // Contenedores para toggles
  @FXML private HBox paneBimestres;
  @FXML private VBox paneSecciones;

  // Overlay de carga (debe existir en el FXML)
  @FXML private StackPane overlay;

  // ToggleGroups
  private final ToggleGroup grpBimestres = new ToggleGroup();
  private final ToggleGroup grpSecciones = new ToggleGroup();

  // Sesión / contexto
  private AuthService.UserSession userSession;
  private Long periodoId; // ej. 2025
  private Long nivelId = 1L;   // 1=Primaria, 2=Secundaria

  // Selección actual
  private Long bimestreSelId;
  private Long seccionSelId = 1L;

  // Datos tabla
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();
  private FilteredList<Alumno> filtered;
  private final Map<Alumno, BooleanProperty> selectedMap = new IdentityHashMap<>();

  // DAOs
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();
  private final BimestreDao bimestreDao     = new BimestreDaoImpl();
  private final SeccionDao seccionDao = new SeccionDaoImpl();
  private final AlumnoDao alumnoDao   = new AlumnoDaoImpl();

  // Caches
  private final Map<String, List<Bimestre>>   cacheBimestresPorPeriodoNivel    = new HashMap<>();
  private final Map<Long,   List<Seccion>> cacheSeccionesPorBimestre        = new HashMap<>();
  private final Map<String, List<Alumno>>  cacheAlumnosPorSeccionPeriodo = new HashMap<>();

  // ===================== Ciclo de vida =====================

  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
    if (s != null) {
      lblTitleUsuario.setText("Bienvenido " + s.getNombre() + "!");
      lblTitlePeriodo.setText("Periodo " + s.getPeriodoNombre());
      lblTitleRol.setText(formatRoles(s.getRoles()));
    }
  }

  @FXML
  public void initialize() {

    try {
      String perNombre = (userSession != null && userSession.getPeriodoNombre() != null)
          ? userSession.getPeriodoNombre()
          : "2025";
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      // Solo cargamos cuando también tengamos nivel (lo setea MainController vía setNivelId)
      if (periodoId != null && nivelId != null) {
        cargarBimestresAsync();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudo inicializar bimestres/secciones.");
    }
  }

  // ===================== Busy / Overlay =====================

  private void setBusy(boolean busy) {
    // Deshabilita inputs principales para evitar clics mientras carga


    if (overlay != null) {
      overlay.setVisible(busy);
      overlay.setManaged(busy);
    }
  }

  // ===================== Cargas asíncronas =====================

  private void cargarBimestresAsync() {
    setBusy(true);
    String key = "";

    FXAsync.run(
        () -> cacheBimestresPorPeriodoNivel.computeIfAbsent(key, k -> {
          try { return bimestreDao.listarbimestres(periodoId); }
          catch (Exception e) { throw new RuntimeException(e); }
        }),
        bimestres -> {
          paneBimestres.getChildren().clear();
          grpBimestres.getToggles().clear();

          for (var g : bimestres) {
            ToggleButton tb = new ToggleButton(g.getNumero() + "° Bim");
            tb.getStyleClass().add("tab");
            tb.setUserData(g);
            tb.setToggleGroup(grpBimestres);
            tb.setOnAction(e -> onChangeBimestreDynamic());
            paneBimestres.getChildren().add(tb);
          }

          if (!bimestres.isEmpty() && !grpBimestres.getToggles().isEmpty()) {
            grpBimestres.selectToggle(grpBimestres.getToggles().get(0));
            onChangeBimestreDynamic(); // dispara carga de secciones
          } else {
            paneSecciones.getChildren().clear();
            grpSecciones.getToggles().clear();
            master.clear();
            setBusy(false);
          }
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga de bimestres", ex.getMessage(), ex);
        }
    );
  }

  private void cargarSeccionesAsync(Long bimestreId) {
    setBusy(true);
    FXAsync.run(
        () -> cacheSeccionesPorBimestre.computeIfAbsent(bimestreId, gid -> {
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

  // ===================== Eventos UI =====================

  private void onChangeBimestreDynamic() {
    Toggle sel = grpBimestres.getSelectedToggle();
    if (sel == null) return;
    var g = (Bimestre) ((ToggleButton) sel).getUserData();
    this.bimestreSelId = g.getId();
    cargarSeccionesAsync(bimestreSelId);
  }

  private void onChangeSeccionDynamic() {
    Toggle sel = grpSecciones.getSelectedToggle();
    if (sel == null) return;
    var s = (Seccion) ((ToggleButton) sel).getUserData();
    this.seccionSelId = s.getId();
    System.out.println("Sección seleccionada: " + s.getNombre());
    setBusy(false);
    //cargarAlumnosAsync();
  }

  // ===================== Handlers =====================


  // Llamado por el menú
  public void setNivelId(long nivelId) {
    if (Objects.equals(this.nivelId, nivelId)) return;
    this.nivelId = nivelId;

    cacheBimestresPorPeriodoNivel.clear();
    cacheSeccionesPorBimestre.clear();
    cacheAlumnosPorSeccionPeriodo.clear();

    if (this.periodoId != null) {
      cargarBimestresAsync();
    }
  }
}

