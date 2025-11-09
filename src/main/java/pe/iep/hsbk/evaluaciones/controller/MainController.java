package pe.iep.hsbk.evaluaciones.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pe.iep.hsbk.evaluaciones.App;
import pe.iep.hsbk.evaluaciones.enums.Constantes;
import pe.iep.hsbk.evaluaciones.service.AuthService;
import pe.iep.hsbk.evaluaciones.util.Dialogs;
import pe.iep.hsbk.evaluaciones.util.Navigator;

public class MainController {

  @FXML private StackPane contentContainer;
  @FXML private Button btnPrimaria, btnSecundaria, btnPlantillasBoleta, btnFirmas, btnSellos;

  // Navegador de vistas
  private Navigator nav;
  private AuthService.UserSession userSession;

  public void initSession(AuthService.UserSession session) {
    // Guardar sesión
    this.userSession = session;
    // Inicializar navegador
    this.nav = new Navigator(contentContainer, userSession);
    // Habilitar/deshabilitar navegación según roles
    boolean puedeDocente = hasRole("Docente") || hasRole("Tutor");
    boolean puedePlantillaBoletas = hasRole("Administrador") || hasRole("Director");
    boolean puedeFirmas = hasRole("Administrador") || hasRole("Director");
    boolean puedeSellos = hasRole("Administrador") || hasRole("Director");

    // Navegación
    show(btnPrimaria, puedeDocente);
    show(btnSecundaria, puedeDocente);
    show(btnPlantillasBoleta, puedePlantillaBoletas);
    show(btnFirmas, puedeFirmas);
    show(btnSellos, puedeSellos);

    // Vista inicial
    if (btnPrimaria.isVisible()) {
      onPrimaria();
    } else if (btnSecundaria.isVisible()) {
      onSecundaria();
    } else if (btnPlantillasBoleta.isVisible()) {
      onPlantillasBoletas();
    } else if (btnFirmas.isVisible()) {
      onFirmas();
    } else if (btnSellos.isVisible()) {
      onSellos();
    }
  }

  private boolean hasRole(String role) {
    return userSession != null &&
        userSession.getRoles() != null &&
        userSession.getRoles().contains(role);
  }

  private void show(Node n, boolean visible) {
    n.setVisible(visible);
    n.setManaged(visible);
  }

  private void marcarActivo(Button activo) {
    for (Button b : new Button[]{btnPrimaria, btnSecundaria, btnPlantillasBoleta, btnFirmas, btnSellos}) {
      var sc = b.getStyleClass();
      sc.removeAll("nav-btn-active");
      if (!sc.contains("nav-btn")) sc.add("nav-btn");
    }
    var scActivo = activo.getStyleClass();
    if (!scActivo.contains("nav-btn-active")) scActivo.add("nav-btn-active");
    scActivo.remove("nav-btn");
  }

  @FXML private void onPrimaria() {
    marcarActivo(btnPrimaria);
    // Navegar a lista de estudiantes de primaria
    nav.go(Constantes.Route.STUDENTS, c -> {
      // Pasar nivelId al controller
      if (c instanceof StudentsListController) {
        StudentsListController sc = (StudentsListController) c;
        sc.setNivelId(1L);
        // Configurar navegación interna del controller
        sc.setGoTo((route, afterLoad) -> {
          // Navegar usando el mismo navigator
          nav.go(route, ctrl -> {
            // Pasar sesión si aplica
            if (afterLoad != null) afterLoad.accept(ctrl);
          });
        });
      }
    });
  }

  @FXML private void onSecundaria() {
    marcarActivo(btnSecundaria);
    // Navegar a lista de estudiantes de primaria
    nav.go(Constantes.Route.STUDENTS, c -> {
      // Pasar nivelId al controller
      if (c instanceof StudentsListController) {
        StudentsListController sc = (StudentsListController) c;
        sc.setNivelId(2L);
        // Configurar navegación interna del controller
        sc.setGoTo((route, afterLoad) -> {
          // Navegar usando el mismo navigator
          nav.go(route, ctrl -> {
            // Pasar sesión si aplica
            if (afterLoad != null) afterLoad.accept(ctrl);
          });
        });
      }
    });
  }

  @FXML private void onPlantillasBoletas() {
    marcarActivo(btnPlantillasBoleta);
    nav.go(Constantes.Route.PLANTILLAS, null);
  }

  @FXML private void onFirmas() {
    marcarActivo(btnFirmas);
    nav.go(Constantes.Route.FIRMAS, null);
  }

  @FXML private void onSellos() {
    marcarActivo(btnSellos);
    nav.go(Constantes.Route.SELLOS, null);
  }

  @FXML
  private void onLogout() {
    try {
      Stage stage = (Stage) contentContainer.getScene().getWindow();
      if (Dialogs.confirm(stage, "¿Seguro que desea cerrar sesión?", "")) {
        App.showLogin(stage);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void onOcultar() {
    Stage stage = (Stage) contentContainer.getScene().getWindow();
    App.minimizar(stage);
  }
}
