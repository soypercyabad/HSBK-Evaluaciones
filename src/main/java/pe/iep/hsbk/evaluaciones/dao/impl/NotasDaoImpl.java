package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.NotasDao;
import pe.iep.hsbk.evaluaciones.dto.*;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class NotasDaoImpl implements NotasDao {

  @Override
  public NotasCursoResumenDto getNotasCursoResumen(long matriculaId, long cursoId, long bimestreId) throws Exception {

    String call = "{ call sp_get_notas_curso_resumen(?,?,?) }";

    try (var con = ConexionDB.getConnection();
         var cs  = con.prepareCall(call)) {

      cs.setLong(1, matriculaId);
      cs.setLong(2, cursoId);
      cs.setLong(3, bimestreId);

      try (ResultSet rs = cs.executeQuery()) {
        if (rs.next()) {
          return new NotasCursoResumenDto(
              rs.getLong("matricula_id"),
              rs.getLong("curso_id"),
              rs.getLong("bimestre_id"),
              getBD(rs, "p1"),
              getBD(rs, "p2"),
              getBD(rs, "p3"),
              getBD(rs, "p4"),
              getBD(rs, "prom_practicas"),
              getBD(rs, "tarea_libro"),
              getBD(rs, "tarea_cuaderno"),
              getBD(rs, "prom_tareas"),
              getBD(rs, "ex_mensual"),
              getBD(rs, "ex_bimestral"),
              getBD(rs, "prom_final"),
              rs.getString("letra")
          );
        }
        return new NotasCursoResumenDto(
            matriculaId,
            cursoId,
            bimestreId,
            null, null, null, null, null,
            null, null, null,
            null, null,
            null, null
        );
      }
    }
  }


  // -------- helpers --------
  private static BigDecimal getBD(ResultSet rs, String col) throws java.sql.SQLException {
    BigDecimal v = rs.getBigDecimal(col);
    return rs.wasNull() ? null : v;
  }
}
