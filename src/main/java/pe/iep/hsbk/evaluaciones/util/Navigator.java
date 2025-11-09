package pe.iep.hsbk.evaluaciones.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import pe.iep.hsbk.evaluaciones.enums.Constantes;
import pe.iep.hsbk.evaluaciones.service.AuthService;

import java.util.function.Consumer;

public class Navigator {

  private final StackPane host;
  private AuthService.UserSession session;

  public Navigator(StackPane host, AuthService.UserSession session) {
    this.host = host;
    this.session = session;
  }

  public void setSession(AuthService.UserSession session) {
    this.session = session;
  }

  /** Carga un FXML, inyecta sesión si aplica y lo muestra en el host. Devuelve el controller. */
  public <T> T go(Constantes.Route route, Consumer<T> afterLoad) {
    try {
      var url = getClass().getResource(route.fxml); // Obtener URL del recurso FXML
      if (url == null) {
        throw new IllegalStateException("No se encontró el recurso FXML: " + route.fxml);
      }

      FXMLLoader loader = new FXMLLoader(url); // Cargar FXML
      Node node = loader.load(); // Cargar nodo raíz

      @SuppressWarnings("unchecked")
      T controller = loader.getController(); // Obtener controller

      // Inyectar sesión si implementa SesionAware
      if (controller instanceof SesionAware) {
        ((SesionAware) controller).setSession(session);
      }

      // Hook para pasar parámetros específicos
      if (afterLoad != null) afterLoad.accept(controller);

      // Mostrar en el contenedor
      host.getChildren().setAll(node);
      return controller;
    } catch (Exception e) {
      e.printStackTrace();
      Dialogs.error(null , "Error", "Error al cargar la vista: " + route.name());
      return null;
    }
  }
}
