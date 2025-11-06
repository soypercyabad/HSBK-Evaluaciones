package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.BimestreDao;
import pe.iep.hsbk.evaluaciones.model.Bimestre;

import java.util.ArrayList;
import java.util.List;

public class BimestreDaoImpl implements BimestreDao {

  @Override
  public List<Bimestre> listarbimestres(Long periodoId) throws Exception {
    String sql = "CALL sp_get_bimestre_periodo (?)";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {

      ps.setLong(1, periodoId);

      try (var rs = ps.executeQuery()) {

        List<Bimestre> bimestres = new ArrayList<>();

        while (rs.next()) {
          Bimestre b  = new Bimestre();
          b.setId(rs.getLong("id"));
          b.setPeriodoId(rs.getLong("periodo_id"));
          b.setNumero(rs.getInt("numero"));
          b.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
          b.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
          b.setAbierto(rs.getBoolean("abierto"));
          bimestres.add(b);
        }
        return bimestres;
      }
    }
  }
}
