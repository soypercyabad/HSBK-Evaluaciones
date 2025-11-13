package pe.iep.hsbk.evaluaciones.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pe.iep.hsbk.evaluaciones.dao.*;
import pe.iep.hsbk.evaluaciones.dao.impl.*;
import pe.iep.hsbk.evaluaciones.dto.NotasCursoResumenDto;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Bimestre;
import pe.iep.hsbk.evaluaciones.model.Curso;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  @FXML private Button btnRegistrarEvaluacion;
  @FXML private Button btnEditarEvaluacion;

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
  private Runnable onBack;
  private AuthService.UserSession userSession;
  private Long periodoId;
  private Long nivelId;
  private boolean contextInitialized = false;

  // ===================== Selección actual =====================
  private Long bimestreSelId;
  private Long cursoSelId;
  private Long matriculaId;
  private Long seccionId;
  private Long usuarioId;

  // ===================== Datos =====================
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();

  // ===================== DAOs =====================
  private final PeriodoDao periodoDao   = new PeriodoDaoImpl();
  private final BimestreDao bimestreDao = new BimestreDaoImpl();
  private final CursoDao cursoDao       = new CursoDaoImpl();
  private final AlumnoDao alumnoDao     = new AlumnoDaoImpl();
  private final NotasDao notasDao       = new NotasDaoImpl();

  // ===================== Cachés =====================
  private final Map<String, List<Bimestre>> cacheBimestresPorPeriodoNivel = new HashMap<>();
  private final Map<Long,   List<Curso>>    cacheCursoPorBimestre         = new HashMap<>();

  // ===================== Setters =====================
  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
    tryInitContext();
  }

  /** Inyecta alumno + nivel y carga cabeceras. */
  public void setAlumno(Alumno a, Long nivelId, Long seccionId) {
    this.nivelId = nivelId;
    this.seccionId = seccionId;

    if (a != null) {
      this.matriculaId = a.getMatriculaId();
      System.out.println("AlumnoNotasController: setAlumno -> Cargando info del alumno id=" + a.getId() + ", nivelId=" + nivelId);
      cargarInfoAlumnoAsync(a, nivelId);
    } else {
      this.matriculaId = null;
      limpiarNotas();
    }

    System.out.println("AlumnoNotasController: setAlumno -> matriculaId=" + this.matriculaId + ", nivelId=" + this.nivelId + ", seccionId=" + this.seccionId);
    tryInitContext();
  }

  /** Inyecta la acción de volver. */
  public void setOnBack(Runnable onBack) {
    this.onBack = onBack;
    wireBackButton();
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
          if (k.getCode() == KeyCode.ESCAPE && onBack != null) onBack.run();
        });
      }
    });
  }

  // ===================== Init "perezoso" =====================
  private void tryInitContext() {
    // Evitar correr esto varias veces
    if (contextInitialized) return;

    // Aún no tenemos todo lo que necesitamos
    if (userSession == null) return;
    if (nivelId == null) return;

    if (userSession.getPeriodoNombre() == null) {
      Dialogs.error(null, "Error", "No se encontró el período en la sesión de usuario.");
      return;
    }

    try {
      String perNombre = userSession.getPeriodoNombre();
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);
      usuarioId = userSession.getUserId();

      System.out.println("AlumnoNotasController: tryInitContext -> periodoId=" + periodoId + ", usuarioId=" + usuarioId + ", nivelId=" + nivelId);

      if (periodoId != null) {
        contextInitialized = true;
        System.out.println("Iniciando carga de bimestres...");
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
  private void cargarInfoAlumnoAsync(Alumno a, Long nivelId) {
    System.out.println("cargarInfoAlumnoAsync: id=" + a.getId() + ", nivelId=" + nivelId);
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
    System.out.println("cargarBimestresAsync: periodoId=" + periodoId + ", nivelId=" + nivelId);
    setBusy(true);
    final String key = periodoId + ":" + nivelId;

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

          for (Bimestre b : bimestres) {
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

  private void cargarCursoAsync(Long periodoId, Long seccionId, Long usuarioId) {
    System.out.println("cargarCursoAsync: periodoId=" + periodoId + ", seccionId=" + seccionId + ", usuarioId=" + usuarioId);

    if (periodoId == null || seccionId == null || usuarioId == null || bimestreSelId == null) {
      limpiarNotas();
      return;
    }

    setBusy(true);
    Long key = bimestreSelId;

    FXAsync.run(
        () -> cacheCursoPorBimestre.computeIfAbsent(key, k -> {
          try {
            return cursoDao.listarCursosAsignados(periodoId, seccionId, usuarioId);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }),
        cursos -> {
          paneCurso.getChildren().clear();
          grpCurso.getToggles().clear();

          for (Curso c : cursos) {
            ToggleButton tb = new ToggleButton(c.getNombre());
            tb.getStyleClass().add("section-btn");
            tb.setUserData(c);
            tb.setToggleGroup(grpCurso);
            tb.setWrapText(true);
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
    System.out.println("cargarNotasAsync: matriculaId=" + matriculaId + ", cursoId=" + cursoId + ", bimestreId=" + bimestreId);
    if (matriculaId == null || cursoId == null || bimestreId == null) {
      limpiarNotas();
      return;
    }

    setBusy(true);
    FXAsync.run(
        () -> {
          try {
            return notasDao.getNotasCursoResumen(matriculaId, cursoId, bimestreId);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        data -> {
          // Practicas
          setText(pract1Field, data.getP1());
          setText(pract2Field, data.getP2());
          setText(pract3Field, data.getP3());
          setText(pract4Field, data.getP4());
          setText(practPromField, data.getPromPracticas());

          // Tareas
          setText(libroField, data.getTareaLibro());
          setText(cuadernoField, data.getTareaCuaderno());
          setText(tareaPromField, data.getPromTareas());

          // Exámenes
          setText(exMenField, data.getExMensual());
          setText(exBimField, data.getExBimestral());

          // Promedio Bimestral
          setText(pbPracticasField, data.getPromPracticas());
          setText(pbTareasField, data.getPromTareas());
          setText(pbExMensualField, data.getExMensual());
          setText(pbExBimestralField, data.getExBimestral());
          setText(pbFinalField, data.getPromFinal());

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

    Bimestre b = (Bimestre) ((ToggleButton) sel).getUserData();
    this.bimestreSelId = (b != null) ? b.getId() : null;

    if (lblBimestreSel != null && b != null) {
      lblBimestreSel.setText(b.getNumero() + "° Bimestre");
    }

    // Cargar cursos del bimestre seleccionado
    System.out.println("Cargando cursos para periodoId=" + periodoId + "seccionId=" + seccionId + ", usuarioId=" + usuarioId);
    cargarCursoAsync(periodoId, seccionId, usuarioId);

    if (matriculaId != null && cursoSelId != null && bimestreSelId != null) {
      cargarNotasAsync(matriculaId, cursoSelId, bimestreSelId);
    } else {
      limpiarNotas();
    }
  }

  private void onChangeCursoDynamic() {
    Toggle sel = grpCurso.getSelectedToggle();
    if (sel == null) return;

    Curso c = (Curso) ((ToggleButton) sel).getUserData();
    if (c != null) {
      this.cursoSelId = c.getId();
      if (lblCursoSel != null) {
        lblCursoSel.setText("Curso: " + c.getNombre());
      }
    }

    if (matriculaId != null && cursoSelId != null && bimestreSelId != null) {
      System.out.println("carga de notas para matriculaId=" + matriculaId + ", cursoSelId=" + cursoSelId + ", bimestreSelId=" + bimestreSelId);
      cargarNotasAsync(matriculaId, cursoSelId, bimestreSelId);
    } else {
      limpiarNotas();
    }
  }

  // ==================== Helpers =====================
  private void limpiarNotas() {
    for (TextField tf : new TextField[]{
        pract1Field, pract2Field, pract3Field, pract4Field, practPromField,
        libroField, cuadernoField, tareaPromField,
        exMenField, exBimField,
        pbPracticasField, pbTareasField, pbExMensualField, pbExBimestralField, pbFinalField
    }) {
      if (tf != null) tf.setText("");
    }
  }

  private void setText(TextField tf, BigDecimal v) {
    if (tf == null) return;
    tf.setText(v == null ? "" : v.stripTrailingZeros().toPlainString());
  }

  public void validarEdicion(ActionEvent actionEvent) {
    // Habilitar edición de notas
    setEditableNotas(true);
    // Ver boton de registrar
    btnRegistrarEvaluacion.setVisible(true);
    btnRegistrarEvaluacion.setManaged(true);
    // Ocultar boton de editar
    btnEditarEvaluacion.setVisible(false);
    btnEditarEvaluacion.setManaged(false);
  }

  public void registrarEvauación(ActionEvent actionEvent) {
    if (matriculaId == null || cursoSelId == null || bimestreSelId == null) {
      Dialogs.warn(null, "Error", "No se puede registrar las notas. Falta información del alumno, curso o bimestre.");
      return;
    }

    try {
      // 1) Leer valores desde los TextField
      BigDecimal p1 = parseNota0a20(pract1Field);
      BigDecimal p2 = parseNota0a20(pract2Field);
      BigDecimal p3 = parseNota0a20(pract3Field);
      BigDecimal p4 = parseNota0a20(pract4Field);

      BigDecimal libro    = parseNota0a20(libroField);
      BigDecimal cuaderno = parseNota0a20(cuadernoField);

      BigDecimal exMen = parseNota0a20(exMenField);
      BigDecimal exBim = parseNota0a20(exBimField);

      // 3) Armar DTO para guardar
      NotasCursoResumenDto dto = new NotasCursoResumenDto(
          matriculaId,
          cursoSelId,
          bimestreSelId,
          usuarioId,
          p1, p2, p3, p4, null,
          libro, cuaderno, null,
          exMen, exBim, null, null
      );

      // Guardar con FXAsync
      setBusy(true);
      FXAsync.run(
          () -> {
            try {
              notasDao.guardarNotasCurso(dto);
              return null;
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          },
          ok -> {
            setBusy(false);

            // Bloquear edición otra vez
            setEditableNotas(false);
            btnRegistrarEvaluacion.setVisible(false);
            btnRegistrarEvaluacion.setManaged(false);
            btnEditarEvaluacion.setVisible(true);
            btnEditarEvaluacion.setManaged(true);

            // Volver a cargar desde BD para refrescar promedios/letra
            cargarNotasAsync(matriculaId, cursoSelId, bimestreSelId);

            Dialogs.info(null, "Notas guardadas", "Las notas se han registrado correctamente.");
          },
          ex -> {
            setBusy(false);
            Dialogs.errorConStacktrace(null, "Error", "No se pudieron guardar las notas.", ex.getMessage(), ex);
          }
      );

    } catch (IllegalArgumentException ex) {
      Dialogs.warn(null, "Datos inválidos", ex.getMessage());
    }
  }

  private BigDecimal parseNota0a20(TextField tf) {
    BigDecimal v = parseNota(tf);
    if (v == null) return null;
    if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(new BigDecimal("20")) > 0) {
      throw new IllegalArgumentException("La nota debe estar entre 0 y 20: " + v);
    }
    return v;
  }

  private BigDecimal parseNota(TextField tf) {
    if (tf == null) return null;
    String txt = tf.getText();
    if (txt == null || txt.trim().isEmpty()) return null;
    try {
      return new BigDecimal(txt.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Valor inválido: " + txt);
    }
  }

  private void setEditableNotas(boolean editable) {
    for (TextField tf : new TextField[]{
        pract1Field, pract2Field, pract3Field, pract4Field,
        libroField, cuadernoField,
        exMenField, exBimField
    }) {
      if (tf != null) {
        tf.setEditable(editable);
      }
    }
  }
}
