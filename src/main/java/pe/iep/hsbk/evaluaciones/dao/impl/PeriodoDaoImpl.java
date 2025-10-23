package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.PeriodoDao;
import pe.iep.hsbk.evaluaciones.model.Periodo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class PeriodoDaoImpl implements PeriodoDao {

  @Override
  public Optional<Periodo> getPeriodoActual() {
    String sql = "SELECT * FROM periodo WHERE activo = 1 ORDER BY id DESC LIMIT 1;";
    try (Connection con = ConexionDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) return Optional.empty();
      Periodo p = new Periodo();
      p.setId(rs.getLong("id"));
      p.setNombre(rs.getString("nombre"));
      p.setActivo(rs.getBoolean("activo"));
      return Optional.of(p);
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Long getPeriodoIdPorNombre(String perNombre) {
    String sql = "SELECT id FROM periodo WHERE nombre = ?;";
    try (Connection con = ConexionDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, perNombre);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("id");
        } else {
          return null;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
