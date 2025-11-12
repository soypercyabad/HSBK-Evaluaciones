package pe.iep.hsbk.evaluaciones.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pe.iep.hsbk.evaluaciones.dao.*;
import pe.iep.hsbk.evaluaciones.dao.impl.*;
import pe.iep.hsbk.evaluaciones.dto.ExamenesDto;
import pe.iep.hsbk.evaluaciones.dto.PracticasDto;
import pe.iep.hsbk.evaluaciones.dto.PromedioDto;
import pe.iep.hsbk.evaluaciones.dto.TareasDto;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Bimestre;
import pe.iep.hsbk.evaluaciones.model.Curso;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.util.*;

public class AlumnoNotasController implements SesionAware {

  // ===================== UI (FXML) =====================
  @FXML private Button btnBack;
  @FXML private Label lblTitleAlumno;
  @FXML private Label lblTitleGrado;
  @FXML private Label lblTitleSeccion;
  @FXML private Label lblCursoSel;
  @FXML private Label lblBimestreSel;
  @FXML private HBox paneBimestres;
  @FXML private VBox paneCurso;
  @FXML private StackPane overlay;
  
  // ===================== Campos de formulario =====================
  @FXML private TextField pract1Field;
  @FXML private TextField pract2Field;
  @FXML private TextField pract3Field;
  @FXML private TextField pract4Field;
  @FXML private TextField practPromField;

  @FXML private TextField libroField;
  @FXML private TextField cuadernoField;
  @FXML private TextField tareaPromField;

  @FXML private TextField exMenField;
  @FXML private TextField exBimField;

  @FXML private TextField pbPracticasField;
  @FXML private TextField pbTareasField;
  @FXML private TextField pbExMensualField;
  @FXML private TextField pbExBimestralField;
  @FXML private TextField pbFinalField;

  // ===================== ToggleGroups =====================
  private final ToggleGroup grpBimestres = new ToggleGroup();
  private final ToggleGroup grpCurso     = new ToggleGroup();

  // ===================== Sesión / Contexto =====================
  private Runnable onBack;                       // acción de volver
  private AuthService.UserSession userSession;   // sesión de usuario
  private Long periodoId;                        // ej. 2025
  private Long nivelId = 1L;                     // 1=Primaria, 2=Secundaria

  // ===================== Selección actual =====================
  private Long bimestreSelId;                    // bimestre seleccionado
  private Long cursoSelId;                       // curso seleccionado
  private Long matriculaId;                      // matrícula del alumno

  // ===================== Datos =====================
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();

  // ===================== DAOs =====================
  private final PeriodoDao periodoDao   = new PeriodoDaoImpl();
  private final BimestreDao bimestreDao = new BimestreDaoImpl();
  private final CursoDao cursoDao       = new CursoDaoImpl();
  private final AlumnoDao alumnoDao     = new AlumnoDaoImpl();
  private final NotasDao notasDao     = new NotasDaoImpl();

  // ===================== Cachés =====================
  private final Map<String, List<Bimestre>> cacheBimestresPorPeriodoNivel = new HashMap<>();
  private final Map<Long,   List<Curso>>    cacheCursoPorBimestre         = new HashMap<>();

  // ===================== Setters =====================
  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
  }

  /** Inyecta alumno + nivel y carga cabeceras. */
  public void setAlumno(Alumno a, Long nivelId) {
    this.nivelId = nivelId;
    if (a != null) {
      this.matriculaId = a.getMatriculaId();
      cargarAlumnoAsync(a, nivelId);
    }else {
      this.matriculaId = null;
      limpiarNotas();
    }
  }

  /** Inyecta la acción de volver. */
  public void setOnBack(Runnable onBack) {
    this.onBack = onBack;
    wireBackButton(); // asegúrate que el botón queda conectado aunque llegue después de initialize()
  }

  // ===================== Ciclo de vida =====================
  @FXML
  public void initialize() {
    // botón Back
    wireBackButton();

    // ESC para volver
    Platform.runLater(() -> {
      if (btnBack != null && btnBack.getScene() != null) {
        btnBack.getScene().setOnKeyPressed(k -> {
          if (k.getCode() == javafx.scene.input.KeyCode.ESCAPE && onBack != null) onBack.run();
        });
      }
    });

    // Inicialización de periodo y carga de bimestres
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
    final String key = periodoId + ":" + nivelId;

    FXAsync.run(
        () -> cacheBimestresPorPeriodoNivel.computeIfAbsent(key, k -> {
          try { return bimestreDao.listarbimestres(periodoId); }
          catch (Exception e) { throw new RuntimeException(e); }
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
            onChangeBimestreDynamic(); // dispara carga de cursos
          } else {
            paneCurso.getChildren().clear();
            grpCurso.getToggles().clear();
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

  private void cargarCursoAsync(Long bimestreId) {
    if (bimestreId == null) return;
    setBusy(true);
    FXAsync.run(
        () -> cacheCursoPorBimestre.computeIfAbsent(bimestreId, bid -> {
          try { return cursoDao.listarCursosActivos(bimestreId); }
          catch (Exception e) { throw new RuntimeException(e); }
        }),
        cursos -> {
          paneCurso.getChildren().clear();
          grpCurso.getToggles().clear();

          for (var c : cursos) {
            ToggleButton tb = new ToggleButton(c.getNombre());
            tb.getStyleClass().add("section-btn");
            tb.setUserData(c);
            tb.setToggleGroup(grpCurso);
            tb.setMaxWidth(Double.MAX_VALUE);
            tb.setOnAction(e -> onChangeCursoDynamic());
            paneCurso.getChildren().add(tb);
          }

          if (!cursos.isEmpty() && !grpCurso.getToggles().isEmpty()) {
            grpCurso.selectToggle(grpCurso.getToggles().get(0));
            onChangeCursoDynamic();
          } else {
            master.clear();
            setBusy(false);
          }
        },
        ex -> {
          setBusy(false);
          Dialogs.errorConStacktrace(null, "Error", "Falló la carga de cursos", ex.getMessage(), ex);
        }
    );
  }

  private void cargarNotasAsync(Long matriculaId, Long cursoId, Long bimestreId) {
    if (matriculaId == null || cursoId == null || bimestreId == null) {
      limpiarNotas();
      return;
    }

    setBusy(true);
    FXAsync.run(
        () -> { // background
          try {
            var pract = notasDao.getPracticas(matriculaId, cursoId, bimestreId);
            var tareas = notasDao.getTareas(matriculaId, cursoId, bimestreId);
            var exam  = notasDao.getExamenes(matriculaId, cursoId, bimestreId);
            var prom  = notasDao.getPromedio(matriculaId, cursoId, bimestreId);
            return new Object[]{pract, tareas, exam, prom};
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        data -> { // UI thread
          var pract = (PracticasDto) data[0];
          var tareas = (TareasDto) data[1];
          var exam = (ExamenesDto) data[2];
          var prom = (PromedioDto) data[3];

          // Practicas
          setText(pract1Field, pract != null ? pract.getP1() : null);
          setText(pract2Field, pract != null ? pract.getP2() : null);
          setText(pract3Field, pract != null ? pract.getP3() : null);
          setText(pract4Field, pract != null ? pract.getP4() : null);
          setText(practPromField, pract != null ? pract.getProm() : null);

          // Tareas
          setText(libroField, tareas != null ? tareas.getLibro() : null);
          setText(cuadernoField, tareas != null ? tareas.getCuaderno() : null);
          setText(tareaPromField, tareas != null ? tareas.getProm() : null);

          // Exámenes
          setText(exMenField, exam != null ? exam.getMensual() : null);
          setText(exBimField, exam != null ? exam.getBimestral() : null);

          // Promedio Bimestral
          setText(pbPracticasField, prom != null ? prom.getPromPracticas() : null);
          setText(pbTareasField, prom != null ? prom.getPromTareas() : null);
          setText(pbExMensualField, prom != null ? prom.getExMensual() : null);
          setText(pbExBimestralField,prom != null ? prom.getExBimestral() : null);
          setText(pbFinalField, prom != null ? prom.getPromedioCurso() : null);

          setBusy(false);
        },
        ex -> {
          setBusy(false);
          limpiarNotas();
          Dialogs.errorConStacktrace(null, "Error", "No se pudieron cargar las notas.", ex.getMessage(), ex);
        }
    );
  }

  // ===================== Eventos UI =====================
  private void onChangeBimestreDynamic() {
    Toggle sel = grpBimestres.getSelectedToggle();
    if (sel == null) return;

    var b = (Bimestre) ((ToggleButton) sel).getUserData();
    this.bimestreSelId = (b != null) ? b.getId() : null;

    if (lblBimestreSel != null && b != null) {
      lblBimestreSel.setText(b.getNumero() + "° Bimestre");
    }

    // Cargar cursos del bimestre seleccionado
    cargarCursoAsync(bimestreSelId);

    if (matriculaId != null && cursoSelId != null && bimestreSelId != null) {
      cargarNotasAsync(matriculaId, cursoSelId, bimestreSelId);
    } else {
      limpiarNotas();
    }
  }

  private void onChangeCursoDynamic() {
    Toggle sel = grpCurso.getSelectedToggle();
    if (sel == null) return;

    var c = (Curso) ((ToggleButton) sel).getUserData();
    if (c != null) {
      this.cursoSelId = c.getId();
      if (lblCursoSel != null) {
        lblCursoSel.setText("Curso: " + c.getNombre());
      }
    }

    if (matriculaId != null && cursoSelId != null && bimestreSelId != null) {
      cargarNotasAsync(matriculaId, cursoSelId, bimestreSelId);
    } else {
      limpiarNotas();
    }

    setBusy(false);
  }

  // ==================== Helpers =====================
  private void limpiarNotas() {
    for (var tf : List.of(
        pract1Field, pract2Field, pract3Field, pract4Field, practPromField,
        libroField, cuadernoField, tareaPromField,
        exMenField, exBimField,
        pbPracticasField, pbTareasField, pbExMensualField, pbExBimestralField, pbFinalField
    )) {
      if (tf != null) tf.setText("");
    }
  }

  private void setText(TextField tf, java.math.BigDecimal v) {
    if (tf == null) return;
    tf.setText(v == null ? "" : v.stripTrailingZeros().toPlainString());
  }
}
