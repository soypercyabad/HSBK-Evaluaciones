package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.SeccionDao;
import pe.iep.hsbk.evaluaciones.model.Seccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeccionDaoImpl implements SeccionDao {

  @Override
  public List<Seccion> listarSeccionesActivasPorUsuario(Long periodoId, Long gradoId, Long usuarioId) throws Exception {
    String call = "{ call sp_secciones_activas_por_usuario(?,?,?) }";
    List<Seccion> out = new ArrayList<>();
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {
      cs.setLong(1, periodoId);
      cs.setLong(2, gradoId);
      cs.setLong(3, usuarioId);
      try (ResultSet rs = cs.executeQuery()) {
        while (rs.next()) {
          Seccion s = new Seccion();
          s.setId(rs.getLong("id"));
          s.setNombre(rs.getString("nombre"));
          out.add(s);
        }
      }
    }
    return out;
  }
}
