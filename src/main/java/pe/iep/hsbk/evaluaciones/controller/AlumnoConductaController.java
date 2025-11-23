package pe.iep.hsbk.evaluaciones.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import pe.iep.hsbk.evaluaciones.dao.*;
import pe.iep.hsbk.evaluaciones.dao.impl.*;
import pe.iep.hsbk.evaluaciones.dto.ConductaResumenDto;
import pe.iep.hsbk.evaluaciones.dto.PromedioAreaBimestreDto;
import pe.iep.hsbk.evaluaciones.dto.PromedioCursoBimestreDto;
import pe.iep.hsbk.evaluaciones.model.*;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AlumnoConductaController implements SesionAware {

  // ===================== UI (FXML) =====================
  @FXML private Label lblTitleAlumno;
  @FXML private Label lblTitleGrado;
  @FXML private Label lblTitleSeccion;
  @FXML private Button btnBack;
  @FXML private Button btnGuardar;
  @FXML private Button btnEditarEvaluacion;
  @FXML private Label lblBimestreSel;

  // Formularios
  @FXML private ComboBox<RecomendacionCatalogo> recomendacionComboBox;
  @FXML private TextField txtutilesEscolares;
  @FXML private TextField txtactividades;
  @FXML private TextField txtreuniones;
  @FXML private TextField txtescuelaPadres;
  @FXML private TextField txtnotaConducta;
  @FXML private TextField txtconductaLetra;

  // Contenedores
  @FXML private HBox paneBimestres;

  // Contenedores dinámicos
  @FXML private FlowPane paneAreas;
  @FXML private FlowPane paneCursosSinArea;

  // Overlay de carga
  @FXML private StackPane overlay;

  // ===================== Grupos de toggles =====================
  private final ToggleGroup grpBimestres = new ToggleGroup();

  // ===================== Sesión / Contexto =====================
  private Runnable onBack; // acción de volver
  private AuthService.UserSession userSession;
  private Long periodoId; // p.ej. 2025
  private Long nivelId = 1L; // 1=Primaria, 2=Secundaria
  private Long recomendacionAlumnoActualId;

  // ===================== Selección actual =====================
  private Long bimestreSelId;
  private Alumno alumnoActual;

  // ===================== Datos  =====================
  private final ObservableList<Alumno> master = FXCollections.observableArrayList();

  // ===================== DAOs =====================
  private final PeriodoDao periodoDao = new PeriodoDaoImpl();
  private final BimestreDao bimestreDao = new BimestreDaoImpl();
  private final AlumnoDao alumnoDao = new AlumnoDaoImpl();
  private final RecomendacionDAO recomendacionDao = new RecomendacionImpl();
  private final ConductaPanelDao conductaPanelDao = new ConductaPanelImpl();
  private final NotasDao notasDao = new NotasDaoImpl();

  // ===================== Cachés =====================
  private final Map<String, List<Bimestre>> cacheBimestresPorPeriodoNivel = new HashMap<>();
  private final Map<Long, List<Curso>> cacheCursoPorBimestre = new HashMap<>();
  private final Map<String, List<Alumno>> cacheAlumnosPorCursoPeriodo = new HashMap<>();

  // ===================== Setters =====================
  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
  }

  /** Inyecta el alumno y nivel y carga cabeceras. */
  public void setAlumno(Alumno a, Long nivelId) {
    this.nivelId = nivelId;
    this.alumnoActual = a;
    if (a != null && a.getId() != null) {
      cargarAlumnoAsync(a, nivelId);
    } else {
      System.out.println("Alumno o alumno.id es NULL. No cargo.");
    }
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
    wireBackButton();

    // ESC para volver
    Platform.runLater(() -> {
      if (btnBack != null && btnBack.getScene() != null) {
        btnBack.getScene().setOnKeyPressed(k -> {
          if (k.getCode() == KeyCode.ESCAPE && onBack != null) onBack.run();
        });
      }
    });

    // Estado inicial: no editable, mostrar Editar, ocultar Guardar
    setEditableConducta(false);
    if (btnGuardar != null) {
      btnGuardar.setVisible(false);
      btnGuardar.setManaged(false);
      btnGuardar.setOnAction(e -> onGuardar());
    }
    if (btnEditarEvaluacion != null) {
      // si prefieres wiring por código en lugar de onAction en FXML
      // btnEditarEvaluacion.setOnAction(this::validarEdicion);
      btnEditarEvaluacion.setVisible(true);
      btnEditarEvaluacion.setManaged(true);
    }

    try {
      String perNombre = (userSession != null && userSession.getPeriodoNombre() != null)
          ? userSession.getPeriodoNombre()
          : "2025";
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      if (periodoId != null && nivelId != null) {
        cargarBimestresAsync();
      }
      cargarRecomendaciones();

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
    // ESC funcione automáticamente:
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
            return alumnoDao.obtenerPorId(
                a.getId().intValue(),
                nivelId.intValue()
            );
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        alumno -> {
          //setBusy(false);

          if (alumno == null) {
            Dialogs.warn(null, "Aviso", "No se pudo cargar datos del alumno.");
            return;
          }

          alumnoActual = (alumno != null) ? alumno : a;

          if (alumnoActual.getId() == null && a != null && a.getId() != null) {
            alumnoActual.setId(a.getId());
          }

          lblTitleAlumno.setText(alumno.getNombres() + " " + alumno.getApellidos());
          lblTitleGrado.setText("Grado: " + alumno.getGrado() + " " + alumno.getNivel());
          lblTitleSeccion.setText("Sección: " + alumno.getSeccion());
        },
        ex -> {
          setBusy(false);
          Dialogs.error(null, "Error", "No se pudo cargar el alumno");
        }
    );
  }

  private void cargarBimestresAsync() {
    setBusy(true);
    final String key = periodoId + ":" + nivelId;

    FXAsync.run(
        () -> cacheBimestresPorPeriodoNivel.computeIfAbsent(key, k -> {
          try {
            return bimestreDao.listarBimestres(periodoId);
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
            tb.setOnAction(e -> {
              try {
                onChangeBimestreDynamic();
              } catch (Exception ex) {
                throw new RuntimeException(ex);
              }
            });
            paneBimestres.getChildren().add(tb);
          }

          if (!bimestres.isEmpty() && !grpBimestres.getToggles().isEmpty()) {
            grpBimestres.selectToggle(grpBimestres.getToggles().get(0));
            try {
              onChangeBimestreDynamic();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
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

  // ===================== Cargar Formulario =====================
  private void cargarEvaluacionFamiliar(EvaluacionFamiliar ef) {
    txtutilesEscolares.setText(ef.getUtiles());
    txtactividades.setText(ef.getParticipacion());
    txtreuniones.setText(ef.getReuniones());
    txtescuelaPadres.setText(ef.getEscuelaPadres());
  }

  private boolean recomendacionesCargadas = false;

  private void cargarRecomendaciones() {
    if (recomendacionComboBox == null) return;

    // Configuración visual SOLO UNA VEZ
    if (!recomendacionesCargadas) {
      recomendacionComboBox.setCellFactory(list -> new ListCell<RecomendacionCatalogo>() {
        @Override
        protected void updateItem(RecomendacionCatalogo r, boolean empty) {
          super.updateItem(r, empty);
          setText(empty || r == null ? null : r.getMensaje());
        }
      });
      recomendacionComboBox.setButtonCell(new ListCell<RecomendacionCatalogo>() {
        @Override
        protected void updateItem(RecomendacionCatalogo r, boolean empty) {
          super.updateItem(r, empty);
          setText(empty || r == null ? "" : r.getMensaje());
        }
      });
    }

    FXAsync.run(
        // BACKGROUND
        () -> {
          try {
            return recomendacionDao.getRecomendaciones();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        // UI THREAD
        data -> {
          if (data == null) data = Collections.emptyList();
          recomendacionComboBox.setItems(FXCollections.observableArrayList(data));
          recomendacionesCargadas = true;

          // si ya tenemos una recomendación del alumno, aplicarla ahora
          if (recomendacionAlumnoActualId != null) {
            seleccionarRecomendacionPorId(recomendacionAlumnoActualId);
          }
        },
        ex -> {
          ex.printStackTrace();
          Dialogs.error(null, "Error", "No se pudo cargar las recomendaciones.");
        }
    );
  }

  private void seleccionarRecomendacionPorId(Long recId) {
    if (recomendacionComboBox == null) return;
    if (recId == null) {
      recomendacionComboBox.getSelectionModel().clearSelection();
      return;
    }
    var items = recomendacionComboBox.getItems();
    if (items == null || items.isEmpty()) return;

    RecomendacionCatalogo match = items.stream()
        .filter(r -> Objects.equals(r.getId(), recId))
        .findFirst()
        .orElse(null);

    if (match != null) {
      recomendacionComboBox.getSelectionModel().select(match);
    } else {
      recomendacionComboBox.getSelectionModel().clearSelection();
    }
  }

  @FXML
  private void onGuardar() {

    if (alumnoActual == null || alumnoActual.getId() == null) {
      Dialogs.warn(null, "Aviso", "Debe seleccionar un alumno antes de guardar.");
      return;
    }

    if (bimestreSelId == null) {
      Dialogs.warn(null, "Aviso", "Debe seleccionar un bimestre antes de guardar.");
      return;
    }

    try {
      Long usuarioId = (userSession != null) ? userSession.getUserId() : null;

      Long recomendacionId = null;
      if (recomendacionComboBox.getValue() != null) {
        recomendacionId = recomendacionComboBox.getValue().getId();
      }

      String comentarios = null;

      conductaPanelDao.saveConductaBimestre(
          alumnoActual.getId(),
          periodoId,
          nivelId,
          bimestreSelId,
          BigDecimal.valueOf(Double.valueOf(txtnotaConducta.getText())),
          txtutilesEscolares.getText(),
          txtactividades.getText(),
          txtreuniones.getText(),
          txtescuelaPadres.getText(),
          comentarios,
          recomendacionId,
          null,
          usuarioId
      );

      // después de guardar, volvemos a modo solo lectura
      setEditableConducta(false);
      if (btnGuardar != null) {
        btnGuardar.setVisible(false);
        btnGuardar.setManaged(false);
      }
      if (btnEditarEvaluacion != null) {
        btnEditarEvaluacion.setVisible(true);
        btnEditarEvaluacion.setManaged(true);
      }

      Dialogs.info(null, "Éxito", "Los datos se han guardado correctamente.");

    } catch (NumberFormatException e) {
      Dialogs.error(null, "Error", "La nota debe ser un número válido.");
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.errorConStacktrace(null, "Error", "No se pudo guardar la conducta.", e.getMessage(), e);
    }
  }

  // ===================== Util: formato nota =====================
  private String formatNota(BigDecimal v) {
    if (v == null) return "";
    return v.stripTrailingZeros().toPlainString();
  }

  // ===================== Render de promedios =====================
  private void renderPromedios(List<PromedioCursoBimestreDto> cursos,
                               List<PromedioAreaBimestreDto> areas) {

    if (paneAreas != null) paneAreas.getChildren().clear();
    if (paneCursosSinArea != null) paneCursosSinArea.getChildren().clear();

    if (cursos == null) cursos = Collections.emptyList();
    if (areas == null) areas = Collections.emptyList();

    // Map área -> promedio área
    Map<Long, BigDecimal> promAreaMap = areas.stream()
        .collect(Collectors.toMap(
            PromedioAreaBimestreDto::getAreaId,
            PromedioAreaBimestreDto::getPromedioBimestre,
            (a, b) -> a
        ));

    // Agrupar cursos por área
    Map<Long, List<PromedioCursoBimestreDto>> cursosPorArea = cursos.stream()
        .filter(c -> c.getAreaId() != null)
        .collect(Collectors.groupingBy(PromedioCursoBimestreDto::getAreaId));

    // Calcular cuántos cursos tiene el área con más cursos (para igualar altura)
    int maxCursos = cursosPorArea.values().stream()
        .mapToInt(List::size)
        .max()
        .orElse(0);

    // Paneles por área
    cursosPorArea.forEach((areaId, listaCursos) -> {
      String areaNombre = listaCursos.get(0).getAreaNombre();
      BigDecimal promArea = promAreaMap.get(areaId);
      GridPane panelArea = buildAreaPanel(areaNombre, listaCursos, promArea, maxCursos);
      paneAreas.getChildren().add(panelArea);
    });

    // Cursos sin área
    cursos.stream()
        .filter(c -> c.getAreaId() == null)
        .forEach(c -> {
          GridPane panel = buildCursoSinAreaPanel(c);
          paneCursosSinArea.getChildren().add(panel);
        });
  }

  // Card de área
  private GridPane buildAreaPanel(String areaNombre,
                                  List<PromedioCursoBimestreDto> cursos,
                                  BigDecimal promedioArea,
                                  int maxCursos) {

    GridPane gp = new GridPane();
    gp.getStyleClass().add("grid-panel");
    gp.setHgap(10);
    gp.setVgap(5);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPrefWidth(90);
    col1.setMinWidth(90);
    col1.setMaxWidth(90);

    ColumnConstraints col2 = new ColumnConstraints();
    col2.setPrefWidth(50);
    col2.setMinWidth(50);
    col2.setMaxWidth(50);

    gp.getColumnConstraints().addAll(col1, col2);

    // Título de área
    Label lblArea = new Label(areaNombre);
    lblArea.getStyleClass().add("top-title-panel");
    gp.add(lblArea, 0, 0, 2, 1);

    int row = 1;

    // Añadir cursos reales
    for (PromedioCursoBimestreDto c : cursos) {
      Label lblCurso = new Label(c.getCursoNombre());
      lblCurso.setWrapText(true);

      TextField txtProm = new TextField();
      txtProm.setEditable(false);
      txtProm.setAlignment(Pos.CENTER);
      txtProm.getStyleClass().add("text-field-disabled");
      txtProm.setText(formatNota(c.getPromedioBimestre()));

      gp.add(lblCurso, 0, row);
      gp.add(txtProm, 1, row);
      row++;
    }

    // Rellenar filas vacías para que todas las cards tengan la misma altura
    for (int i = cursos.size(); i < maxCursos; i++) {
      TextField spacer = new TextField();
      spacer.setEditable(false);
      spacer.setAlignment(Pos.CENTER);
      spacer.getStyleClass().add("text-field-promedio");
      spacer.setVisible(false);
      GridPane.setColumnSpan(spacer, 2);
      gp.add(spacer, 0, row);
      row++;
    }

    // Fila de Promedio (siempre al final)
    Label lblProm = new Label("Promedio");
    lblProm.getStyleClass().add("top-title-panel");
    gp.add(lblProm, 0, row);

    TextField txtPromArea = new TextField();
    txtPromArea.setEditable(false);
    txtPromArea.setAlignment(Pos.CENTER);
    txtPromArea.getStyleClass().add("text-field-promedio");
    txtPromArea.setText(formatNota(promedioArea));

    gp.add(txtPromArea, 1, row);

    return gp;
  }

  // Card de curso sin área
  private GridPane buildCursoSinAreaPanel(PromedioCursoBimestreDto c) {
    GridPane gp = new GridPane();
    gp.getStyleClass().add("grid-panel");
    gp.setHgap(10);
    gp.setVgap(5);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPrefWidth(90);
    col1.setMinWidth(90);
    col1.setMaxWidth(90);

    ColumnConstraints col2 = new ColumnConstraints();
    col2.setPrefWidth(50);
    col2.setMinWidth(50);
    col2.setMaxWidth(50);

    gp.getColumnConstraints().addAll(col1, col2);

    Label lblCurso = new Label(c.getCursoNombre());
    lblCurso.setWrapText(true);

    TextField txtProm = new TextField();
    txtProm.setEditable(false);
    txtProm.setAlignment(Pos.CENTER);
    txtProm.getStyleClass().add("text-field-promedio");
    txtProm.setText(formatNota(c.getPromedioBimestre()));

    gp.add(lblCurso, 0, 0);
    gp.add(txtProm, 1, 0);

    return gp;
  }

  // ===================== Eventos UI =====================
  private void onChangeBimestreDynamic() {

    Toggle sel = grpBimestres.getSelectedToggle();
    if (sel == null) {
      return;
    }

    var bim = (Bimestre) ((ToggleButton) sel).getUserData();
    this.bimestreSelId = (bim != null) ? bim.getId() : null;

    if (lblBimestreSel != null && bim != null) {
      lblBimestreSel.setText(bim.getNumero() + "° Bimestre");
    }

    if (alumnoActual == null || alumnoActual.getId() == null) {
      Dialogs.warn(null, "Aviso", "Debe seleccionar un alumno.");
      return;
    }
    if (periodoId == null || nivelId == null || bimestreSelId == null) {
      return;
    }

    setBusy(true);

    final Long alumnoId = alumnoActual.getId();
    final Long perId = periodoId;
    final Long nivId = nivelId;
    final Long bimId = bimestreSelId;

    FXAsync.run(
        () -> {
          try {
            ConductaResumenDto resumen = conductaPanelDao.getResumenByAlumno(alumnoId, perId, nivId, bimId);
            System.out.println("Parametros para promedios: alumnoId=" + alumnoId + " perId=" + perId + " nivId=" + nivId + " bimId=" + bimId);
            List<PromedioCursoBimestreDto> cursos =
                notasDao.listarPromediosCursoBimestre(alumnoId, perId, nivId, bimId);
            List<PromedioAreaBimestreDto> areas =
                notasDao.listarPromediosAreaBimestre(alumnoId, perId, nivId, bimId);

            return new Object[]{resumen, cursos, areas};
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        result -> {
          ConductaResumenDto resumen = (ConductaResumenDto) result[0];
          List<PromedioCursoBimestreDto> cursos = (List<PromedioCursoBimestreDto>) result[1];
          List<PromedioAreaBimestreDto> areas = (List<PromedioAreaBimestreDto>) result[2];

          // ====== PINTAR CONDUCTA / FAMILIAR =========
          if (resumen.getConducta() != null) {
            txtnotaConducta.setText(String.valueOf(resumen.getConducta().getNota()));
            txtconductaLetra.setText(resumen.getConducta().getLetra());
          } else {
            txtnotaConducta.clear();
            txtconductaLetra.clear();
          }

          var eval = resumen.getEvaluacionFamiliar();
          if (eval != null) {
            if (txtutilesEscolares != null) txtutilesEscolares.setText(eval.getUtiles());
            if (txtactividades != null) txtactividades.setText(eval.getParticipacion());
            if (txtreuniones != null) txtreuniones.setText(eval.getReuniones());
            if (txtescuelaPadres != null) txtescuelaPadres.setText(eval.getEscuelaPadres());
          } else {
            if (txtutilesEscolares != null) txtutilesEscolares.clear();
            if (txtactividades != null) txtactividades.clear();
            if (txtreuniones != null) txtreuniones.clear();
            if (txtescuelaPadres != null) txtescuelaPadres.clear();
          }

          // ====== RECOMENDACIÓN DEL ALUMNO ======
          Long recIdAlumno = null;
          if (resumen.getRecomendacionesAlumno() != null && !resumen.getRecomendacionesAlumno().isEmpty()) {
            var ra = resumen.getRecomendacionesAlumno().get(0);
            recIdAlumno = ra.getRecomendacionId();
          }

          // guardamos siempre el id (aunque sea null) para usarlo luego
          recomendacionAlumnoActualId = recIdAlumno;

          // si el combo ya tiene los ítems cargados, seleccionamos de inmediato
          if (recomendacionesCargadas &&
              recomendacionComboBox.getItems() != null &&
              !recomendacionComboBox.getItems().isEmpty()) {
            seleccionarRecomendacionPorId(recomendacionAlumnoActualId);
          }

          // ====== PINTAR PROMEDIOS =========
          renderPromedios(cursos, areas);

          // Siempre que cambie de bimestre, volvemos a modo lectura
          setEditableConducta(false);
          if (btnGuardar != null) {
            btnGuardar.setVisible(false);
            btnGuardar.setManaged(false);
          }
          if (btnEditarEvaluacion != null) {
            btnEditarEvaluacion.setVisible(true);
            btnEditarEvaluacion.setManaged(true);
          }

          setBusy(false);
        },
        ex -> {
          setBusy(false);
          ex.printStackTrace();
          Dialogs.errorConStacktrace(null, "Error", "No se pudo cargar el resumen de conducta / promedios", ex.getMessage(), ex);
        }
    );
  }

  // ===================== Lógica de edición (igual que AlumnoNotas) =====================
  public void validarEdicion(ActionEvent actionEvent) {
    setEditableConducta(true);

    if (btnGuardar != null) {
      btnGuardar.setVisible(true);
      btnGuardar.setManaged(true);
    }
    if (btnEditarEvaluacion != null) {
      btnEditarEvaluacion.setVisible(false);
      btnEditarEvaluacion.setManaged(false);
    }
  }

  private void setEditableConducta(boolean editable) {
    if (txtutilesEscolares != null) txtutilesEscolares.setEditable(editable);
    if (txtactividades != null)    txtactividades.setEditable(editable);
    if (txtreuniones != null)      txtreuniones.setEditable(editable);
    if (txtescuelaPadres != null)  txtescuelaPadres.setEditable(editable);
    if (txtnotaConducta != null)   txtnotaConducta.setEditable(editable);

    if (recomendacionComboBox != null) {
      setComboReadOnly(recomendacionComboBox, !editable);
    }
  }

  private void setComboReadOnly(ComboBox<?> combo, boolean readOnly) {
    if (combo == null) return;

    if (readOnly) {
      combo.setMouseTransparent(true);
      combo.setFocusTraversable(false);
    } else {
      combo.setMouseTransparent(false);
      combo.setFocusTraversable(true);
    }
  }
}
