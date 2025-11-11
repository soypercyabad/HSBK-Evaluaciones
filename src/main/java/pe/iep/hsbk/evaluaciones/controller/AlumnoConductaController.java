package pe.iep.hsbk.evaluaciones.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.*;
import pe.iep.hsbk.evaluaciones.dao.impl.*;
import pe.iep.hsbk.evaluaciones.model.*;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.FXAsync;
import pe.iep.hsbk.evaluaciones.util.SesionAware;

import java.util.*;

public class AlumnoConductaController implements SesionAware {

  // ===================== UI (FXML) =====================
  @FXML
  private Label lblTitleAlumno;
  @FXML
  private Label lblTitleGrado;
  @FXML
  private Label lblTitleSeccion;
  @FXML
  private Button btnBack;

  @FXML
  private Label lblBimestreSel;

  // Formularios
  @FXML
  private ComboBox<RecomendacionCatalogo> recomendacionComboBox;
  @FXML private TextField txtutilesEscolares;
  @FXML private TextField txtactividades;
  @FXML private TextField txtreuniones;
  @FXML private TextField txtescuelaPadres;
  @FXML private TextField txtnotaConducta;
  @FXML private TextField txtconductaLetra;

  // Contenedores
  @FXML
  private HBox paneBimestres;

  // Overlay de carga
  @FXML
  private StackPane overlay;

  // ===================== Grupos de toggles =====================
  private final ToggleGroup grpBimestres = new ToggleGroup();

  // ===================== Sesión / Contexto =====================
  private Runnable onBack; // acción de volver
  private AuthService.UserSession userSession;
  private Long periodoId; // p.ej. 2025
  private Long nivelId = 1L; // 1=Primaria, 2=Secundaria

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
  private final MatriculaDao matriculaDao = new MatriculaDaoImpl();

  private Long currentMatriculaId;

  // ===================== Cachés =====================
  private final Map<String, List<Bimestre>> cacheBimestresPorPeriodoNivel = new HashMap<>();
  private final Map<Long, List<Curso>> cacheCursoPorBimestre = new HashMap<>();
  private final Map<String, List<Alumno>> cacheAlumnosPorCursoPeriodo = new HashMap<>();

  // ===================== Setters =====================
  @Override
  public void setSession(AuthService.UserSession s) {
    this.userSession = s;
  }

  /**
   * Inyecta el alumno y nivel y carga cabeceras.
   */
  public void setAlumno(Alumno a, Long nivelId) {
    System.out.println("setAlumno(): a=" + (a!=null ? (a.getId()+" "+a.getNombres()) : "null") + " nivelId=" + nivelId);
    this.nivelId = nivelId;
    this.alumnoActual = a;
    if (a != null && a.getId() != null) {
      cargarAlumnoAsync(a, nivelId);
    } else {
      System.out.println("Alumno o alumno.id es NULL. No cargo.");
    }
  }

  /**
   * Inyecta la acción de volver.
   */
  public void setOnBack(Runnable onBack) {
    this.onBack = onBack;
    wireBackButton();
  }

  /**
   * Llamado por el menú externo para cambiar nivel y recargar.
   */
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

    Platform.runLater(() -> {
      if (btnBack != null && btnBack.getScene() != null) {
        btnBack.getScene().setOnKeyPressed(k -> {
          if (k.getCode() == KeyCode.ESCAPE && onBack != null) onBack.run();
        });
      }
    });

    try {
      String perNombre = (userSession != null && userSession.getPeriodoNombre() != null)
              ? userSession.getPeriodoNombre()
              : "2025";
      periodoId = periodoDao.getPeriodoIdPorNombre(perNombre);

      if (periodoId != null && nivelId != null) {
        cargarBimestresAsync();
      }

      // Cargar recomendaciones SIEMPRE (no dependen de alumno/bimestre)
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
                    return alumnoDao.obtenerPorId(
                            a.getId().intValue(),
                            nivelId.intValue()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            alumno -> {
              setBusy(false);

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

  private void cargarRecomendaciones() {
    FXAsync.run(
            // BACKGROUND
            () -> {
              try {
                return recomendacionDao.getRecomendaciones(); // List<RecomendacionCatalogo>
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            },
            // UI THREAD
            data -> {
              ObservableList<RecomendacionCatalogo> items = FXCollections.observableArrayList(data);
              recomendacionComboBox.setItems(items);

              // Muestra solo el "mensaje" en la lista
              recomendacionComboBox.setCellFactory(list -> new ListCell<RecomendacionCatalogo>() {
                @Override
                protected void updateItem(RecomendacionCatalogo r, boolean empty) {
                  super.updateItem(r, empty);
                  setText(empty || r == null ? null : r.getMensaje());
                }
              });

              // Muestra solo el "mensaje" en el botón del combo
              recomendacionComboBox.setButtonCell(new ListCell<RecomendacionCatalogo>() {
                @Override
                protected void updateItem(RecomendacionCatalogo r, boolean empty) {
                  super.updateItem(r, empty);
                  setText(empty || r == null ? "" : r.getMensaje());
                }
              });
            },
            // ERROR
            ex -> {
              ex.printStackTrace();
              Dialogs.error(null, "Error", "No se pudo cargar las recomendaciones.");
            }
    );
  }


  // ===================== Eventos UI =====================
  private void onChangeBimestreDynamic() {
    System.out.println(">> onChangeBimestreDynamic() called");

    Toggle sel = grpBimestres.getSelectedToggle();
    if (sel == null) {
      System.out.println(">> No hay toggle seleccionado");
      return;
    }

    var bim = (Bimestre) ((ToggleButton) sel).getUserData();
    this.bimestreSelId = (bim != null) ? bim.getId() : null;
    System.out.println(">> bimestreSelId=" + bimestreSelId);

    if (alumnoActual == null) {
      System.out.println(">> alumnoActual es NULL");
      Dialogs.warn(null, "Aviso", "No hay alumno seleccionado.");
      return;
    }
    if (alumnoActual.getId() == null) {
      System.out.println(">> alumnoActual.getId() es NULL");
      Dialogs.warn(null, "Aviso", "El alumno no tiene ID.");
      return;
    }
    if (periodoId == null || nivelId == null || bimestreSelId == null) {
      System.out.printf(">> Contexto incompleto: periodoId=%s, nivelId=%s, bimestreSelId=%s%n",
              String.valueOf(periodoId), String.valueOf(nivelId), String.valueOf(bimestreSelId));
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
                System.out.printf(">> DAO.getResumenByAlumno(alumnoId=%d, periodoId=%d, nivelId=%d, bimestreId=%d)%n",
                        alumnoId, perId, nivId, bimId);
                return conductaPanelDao.getResumenByAlumno(alumnoId, perId, nivId, bimId);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            },
            resumen -> {
              System.out.println(">> Resumen recibido en UI");
              // ========== PINTAR UI ==========
              if (resumen.getConducta() != null) {
                System.out.println(">> UI: conducta id=" + resumen.getConducta().getId());
                txtnotaConducta.setText(String.valueOf(resumen.getConducta().getNota()));
                txtconductaLetra.setText(resumen.getConducta().getLetra());
              } else {
                System.out.println(">> UI: conducta sin datos");
              }

              var eval = resumen.getEvaluacionFamiliar();
              if (eval != null) {
                System.out.println(">> UI: Eval Fam id=" + eval.getId());

                if (txtutilesEscolares != null)
                  txtutilesEscolares.setText(eval.getUtiles());

                if (txtactividades != null)
                  txtactividades.setText(eval.getParticipacion());

                if (txtreuniones != null)
                  txtreuniones.setText(eval.getReuniones());

                if (txtescuelaPadres != null)
                  txtescuelaPadres.setText(eval.getEscuelaPadres());
              } else {
                System.out.println(">> UI: eval fam sin datos (limpiando)");

                if (txtutilesEscolares != null) txtutilesEscolares.clear();
                if (txtactividades != null)     txtactividades.clear();
                if (txtreuniones != null)       txtreuniones.clear();
                if (txtescuelaPadres != null)   txtescuelaPadres.clear();
              }

              // Poblar campos de texto y combo (ver bloque de arriba)
              // ...

              setBusy(false);
            },
            ex -> {
              setBusy(false);
              ex.printStackTrace();
              Dialogs.errorConStacktrace(null, "Error", "No se pudo cargar el resumen de conducta", ex.getMessage(), ex);
            }
    );
  }

}
