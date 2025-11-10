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
import pe.iep.hsbk.evaluaciones.dao.CursoDao;
import pe.iep.hsbk.evaluaciones.dao.impl.AlumnoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.BimestreDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.PeriodoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.CursoDaoImpl;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Bimestre;
import pe.iep.hsbk.evaluaciones.model.Curso;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.util.*;

public class AlumnoConductaController implements SesionAware {
  // UI
  @FXML private Label lblTitleAlumno;
  @FXML private Label lblTitleGrado;
  @FXML private Label lblTitleSeccion;

  @FXML private Label lblCursoSel;
  @FXML private Label lblBimestreSel;

  // Contenedores para toggles
  @FXML private HBox paneBimestres;

  // Overlay de carga (debe existir en el FXML)
  @FXML private StackPane overlay;

  // ToggleGroups
  private final ToggleGroup grpBimestres = new ToggleGroup();

  // Sesión / contexto
  private AuthService.UserSession userSession;
  private Long periodoId; // ej. 2025
  private Long nivelId = 1L;   // 1=Primaria, 2=Secundaria

  // Selección actual
  private Long bimestreSelId;
  private Long cursoSelId = 1L;

  // Datos tabla
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();
  private FilteredList<Alumno> filtered;
  private final Map<Alumno, BooleanProperty> selectedMap = new IdentityHashMap<>();

  // DAOs
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();
  private final BimestreDao bimestreDao     = new BimestreDaoImpl();
  private final CursoDao cursoDao = new CursoDaoImpl();
  private final AlumnoDao alumnoDao   = new AlumnoDaoImpl();

  // Caches
  private final Map<String, List<Bimestre>>   cacheBimestresPorPeriodoNivel    = new HashMap<>();
  private final Map<Long,   List<Curso>> cacheCursoPorBimestre        = new HashMap<>();
  private final Map<String, List<Alumno>>  cacheAlumnosPorCursoPeriodo = new HashMap<>();

  // ===================== Ciclo de vida =====================

  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
  }

  public void setAlumno(Alumno a, Long nivelId) {
    this.nivelId = nivelId;

    if (a != null) {
      cargarAlumnoAsync(a, nivelId);
    }
  }

  private void cargarAlumnoAsync(Alumno a, Long nivelId) {
    setBusy(true);
    FXAsync.run(
        () -> {
          try {
            return alumnoDao.obtenerPorId(Math.toIntExact(a.getId()), Math.toIntExact(nivelId));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        alumno -> {
          lblTitleAlumno.setText(alumno.getNombres() + " " + alumno.getApellidos());
          lblTitleGrado.setText("Grado: " + alumno.getGrado() + ' ' + alumno.getNivel());
          lblTitleSeccion.setText("Sección: " + alumno.getSeccion());
          setBusy(false);
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga del alumno", ex.getMessage(), ex);
        }
    );
  }

  @FXML
  public void initialize() {

    try {
      String perNombre = (userSession != null && userSession.getPeriodoNombre() != null)
          ? userSession.getPeriodoNombre()
          : "2025";
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      // Solo cargamos cuando también tengamos nivel (lo setea MainController vía setNivelId)
      System.out.println("Inicializando AlumnoNotasController con periodoId=" + periodoId + " y nivelId=" + nivelId);
      if (periodoId != null && nivelId != null) {
        cargarBimestresAsync();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudo inicializar bimestres/cursoes.");
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
            ToggleButton tb = new ToggleButton(g.getNumero() + "° Bimestre");
            tb.getStyleClass().add("tab");
            tb.setUserData(g);
            tb.setToggleGroup(grpBimestres);
            tb.setOnAction(e -> onChangeBimestreDynamic());
            paneBimestres.getChildren().add(tb);
          }

          if (!bimestres.isEmpty() && !grpBimestres.getToggles().isEmpty()) {
            grpBimestres.selectToggle(grpBimestres.getToggles().get(0));
            onChangeBimestreDynamic(); // dispara carga de cursoes
          } else {
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

  // ===================== Eventos UI =====================

  private void onChangeBimestreDynamic() {
    Toggle sel = grpBimestres.getSelectedToggle();
    if (sel == null) return;
    var g = (Bimestre) ((ToggleButton) sel).getUserData();
  }

  // ===================== Handlers =====================


  // Llamado por el menú
  public void setNivelId(long nivelId) {
    if (Objects.equals(this.nivelId, nivelId)) return;
    this.nivelId = nivelId;

    cacheBimestresPorPeriodoNivel.clear();
    cacheCursoPorBimestre.clear();
    cacheAlumnosPorCursoPeriodo.clear();

    if (this.periodoId != null) {
      cargarBimestresAsync();
    }
  }
}

