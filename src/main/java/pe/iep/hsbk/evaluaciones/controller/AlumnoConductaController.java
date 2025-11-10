package pe.iep.hsbk.evaluaciones.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pe.iep.hsbk.evaluaciones.dao.AlumnoDao;
import pe.iep.hsbk.evaluaciones.dao.BimestreDao;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.dao.impl.AlumnoDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.BimestreDaoImpl;
import pe.iep.hsbk.evaluaciones.dao.impl.PeriodoDaoImpl;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Bimestre;
import pe.iep.hsbk.evaluaciones.model.Curso;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.util.*;

public class AlumnoConductaController implements SesionAware {

  // ===================== UI (FXML) =====================
  @FXML private Label lblTitleAlumno;
  @FXML private Label lblTitleGrado;
  @FXML private Label lblTitleSeccion;
  @FXML private Button btnBack;

  @FXML private Label lblBimestreSel;

  // Contenedores
  @FXML private HBox paneBimestres;

  // Overlay de carga
  @FXML private StackPane overlay;

  // ===================== Grupos de toggles =====================
  private final ToggleGroup grpBimestres = new ToggleGroup();

  // ===================== Sesión / Contexto =====================
  private Runnable onBack; // acción de volver
  private AuthService.UserSession userSession;
  private Long periodoId; // p.ej. 2025
  private Long nivelId = 1L; // 1=Primaria, 2=Secundaria

  // ===================== Selección actual =====================
  private Long bimestreSelId;

  // ===================== Datos  =====================
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();

  // ===================== DAOs =====================
  private final PeriodoDao periodoDao   = new PeriodoDaoImpl();
  private final BimestreDao bimestreDao = new BimestreDaoImpl();
  private final AlumnoDao alumnoDao     = new AlumnoDaoImpl();

  // ===================== Cachés =====================
  private final Map<String, List<Bimestre>> cacheBimestresPorPeriodoNivel = new HashMap<>();
  private final Map<Long,   List<Curso>>    cacheCursoPorBimestre         = new HashMap<>();
  private final Map<String, List<Alumno>>   cacheAlumnosPorCursoPeriodo   = new HashMap<>();

  // ===================== Setters =====================
  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
  }

  /** Inyecta el alumno y nivel y carga cabeceras. */
  public void setAlumno(Alumno a, Long nivelId) {
    this.nivelId = nivelId;
    if (a != null) cargarAlumnoAsync(a, nivelId);
  }

  /** Inyecta la acción de volver. */
  public void setOnBack(Runnable onBack) {
    this.onBack = onBack;
    wireBackButton();
  }

  /** Llamado por el menú externo para cambiar nivel y recargar. */
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

  // ===================== Ciclo de vida =====================
  @FXML
  public void initialize() {
    // botón "volver"
    wireBackButton();

    // ESC para volver
    Platform.runLater(() -> {
      if (btnBack != null && btnBack.getScene() != null) {
        btnBack.getScene().setOnKeyPressed(k -> {
          if (k.getCode() == javafx.scene.input.KeyCode.ESCAPE && onBack != null) onBack.run();
        });
      }
    });

    // Inicialización de periodo y carga inicial de bimestres
    try {
      String perNombre = (userSession != null && userSession.getPeriodoNombre() != null)
          ? userSession.getPeriodoNombre()
          : "2025";
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      if (periodoId != null && nivelId != null) {
        cargarBimestresAsync();
      }
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null, "Error", "No se pudo inicializar bimestres/cursos.");
    }
  }

  // ===================== Atajos de botones =====================
  private void wireBackButton() {
    if (btnBack == null) return;
    btnBack.setOnAction(e -> {
      if (onBack != null) onBack.run();
      else System.out.println("No hay acción de retorno configurada.");
    });
    // Si te gusta que ESC funcione automáticamente:
    btnBack.setCancelButton(true);
  }

  // ===================== Busy / Overlay =====================
  private void setBusy(boolean busy) {
    if (overlay != null) {
      overlay.setVisible(busy);
      overlay.setManaged(busy);
    }
  }

  // ===================== Cargas asíncronas =====================
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

  private void cargarBimestresAsync() {
    setBusy(true);
    final String key = periodoId + ":" + nivelId; // clave estable para la caché

    FXAsync.run(
        () -> cacheBimestresPorPeriodoNivel.computeIfAbsent(key, k -> {
          try {
            return bimestreDao.listarbimestres(periodoId);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }),
        bimestres -> {
          paneBimestres.getChildren().clear();
          grpBimestres.getToggles().clear();

          for (var b : bimestres) {
            ToggleButton tb = new ToggleButton(b.getNumero() + "° Bimestre");
            tb.getStyleClass().add("tab");
            tb.setUserData(b);
            tb.setToggleGroup(grpBimestres);
            tb.setOnAction(e -> onChangeBimestreDynamic());
            paneBimestres.getChildren().add(tb);
          }

          if (!bimestres.isEmpty() && !grpBimestres.getToggles().isEmpty()) {
            grpBimestres.selectToggle(grpBimestres.getToggles().get(0));
            onChangeBimestreDynamic();
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

    var bim = (Bimestre) ((ToggleButton) sel).getUserData();
    this.bimestreSelId = (bim != null) ? bim.getId() : null;

    if (lblBimestreSel != null && bim != null) {
      lblBimestreSel.setText("Bimestre: " + bim.getNumero());
    }
  }
}
