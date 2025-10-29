package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.PlantillaBoleta;

import java.sql.SQLException;
import java.util.List;

public interface PlantillaBoletaDAO {
  List<PlantillaBoleta> getPlantillaBoletas() throws Exception;
  void guardarPlantillaBoleta(PlantillaBoleta plantilla) throws Exception;
  void actualizarPlantillaBoleta(PlantillaBoleta plantilla) throws Exception;
  void actualizarEstadoPlantillaBoleta(PlantillaBoleta plantilla) throws Exception;
}
