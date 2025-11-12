package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.GradoDao;
import pe.iep.hsbk.evaluaciones.model.Grado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradoDaoImpl implements GradoDao {

  @Override
  public List<Grado> listarGradosActivosPorUsuario(Long periodoId, Long nivelId, Long usuarioId) throws Exception {
    String call = "{ call sp_grados_activos_por_usuario(?,?,?) }";
    List<Grado> out = new ArrayList<>();
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {
      cs.setLong(1, periodoId);
      cs.setLong(2, nivelId);
      cs.setLong(3, usuarioId);
      try (ResultSet rs = cs.executeQuery()) {
        while (rs.next()) {
          Grado g = new Grado();
          g.setId(rs.getLong("id"));
          g.setNombre(rs.getString("nombre"));
          out.add(g);
        }
      }
    }
    return out;
  }
}
