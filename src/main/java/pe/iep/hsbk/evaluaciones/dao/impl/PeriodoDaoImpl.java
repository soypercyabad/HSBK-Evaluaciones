package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.model.Periodo;

import java.sql.*;
import java.util.Optional;

public class PeriodoDaoImpl implements PeriodoDao {

  @Override
  public Optional<Periodo> getPeriodoActual() throws Exception {
    String call = "{ call sp_periodo_actual() }";
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call);
         ResultSet rs = cs.executeQuery()) {
      if (rs.next()) {
        Periodo p = new Periodo();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setActivo(rs.getBoolean("activo"));
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  @Override
  public Long getPeriodoIdPorNombre(String nombre) throws Exception {
    String call = "{ call sp_periodo_id_por_nombre(?) }";
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {
      cs.setString(1, nombre);
      try (ResultSet rs = cs.executeQuery()) {
        if (rs.next()) return rs.getLong("id");
      }
    }
    return null;
  }
}
