package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.AsignacionDao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class AsignacionDaoImpl implements AsignacionDao {

  @Override
  public boolean usuarioTieneAsignacionEnNivel(long usuarioId, long nivelId, long periodoId) throws Exception {
    String call = "{ call sp_usuario_tiene_asignacion_nivel(?,?,?) }";
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {
      cs.setLong(1, usuarioId);
      cs.setLong(2, nivelId);
      cs.setLong(3, periodoId);
      try (ResultSet rs = cs.executeQuery()) {
        if (rs.next()) {
          int exists = rs.getInt(1); // o por alias 'tiene_asignacion'
          return exists == 1;
        }
      }
    }
    return false;
  }
}
