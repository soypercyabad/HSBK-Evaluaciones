package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.dao.GradoDao;
import pe.iep.hsbk.evaluaciones.model.Grado;
import pe.iep.hsbk.evaluaciones.config.ConexionDB;

import java.util.ArrayList;
import java.util.List;

public class GradoDaoImpl implements GradoDao {

  @Override
  public List<Grado> listarGradosActivos(Long periodoId, Long nivelId) throws Exception {
    String sql = "SELECT DISTINCT g.id, g.nombre, g.orden, g.nivel_id\n" +
        "FROM grado g\n" +
        "JOIN seccion s   ON s.grado_id = g.id\n" +
        "JOIN matricula m ON m.seccion_id = s.id\n" +
        "WHERE m.periodo_id = ? AND g.nivel_id = ?\n" +
        "ORDER BY g.orden";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {

      ps.setLong(1, periodoId);
      ps.setLong(2, nivelId);

      try (var rs = ps.executeQuery()) {

        List<Grado> grados = new ArrayList<>();

        while (rs.next()) {
          Grado g = new Grado();
          g.setId(rs.getLong("id"));
          g.setNombre(rs.getString("nombre"));
          g.setOrden(rs.getInt("orden"));
          g.setNivelId(rs.getLong("nivel_id"));
          grados.add(g);
        }
        return grados;
      }
    }
  }
}
