package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.SeccionDao;
import pe.iep.hsbk.evaluaciones.model.Seccion;

import java.util.ArrayList;
import java.util.List;

public class SeccionDaoImpl implements SeccionDao {

  @Override
  public List<Seccion> listarSeccionesActivas(Long periodoId, Long gradoId) throws Exception {
    String sql = "SELECT DISTINCT s.id, s.nombre\n" +
        "FROM seccion s\n" +
        "JOIN matricula m ON m.seccion_id = s.id\n" +
        "WHERE s.grado_id = ? AND m.periodo_id = ?\n" +
        "ORDER BY s.nombre;";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {

      ps.setLong(1, gradoId);
      ps.setLong(2, periodoId);

      try (var rs = ps.executeQuery()) {

        List<Seccion> secciones = new ArrayList<>();

        while (rs.next()) {
          Seccion s = new Seccion();
          s.setId(rs.getLong("id"));
          s.setNombre(rs.getString("nombre"));
          secciones.add(s);
        }
        return secciones;
      }
    }

  }
}
