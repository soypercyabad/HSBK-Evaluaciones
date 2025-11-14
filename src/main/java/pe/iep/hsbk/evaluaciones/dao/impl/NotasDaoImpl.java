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
              null,
              getBD(rs, "p1"),
              rs.getString("p1_letra"),
              getBD(rs, "p2"),
              rs.getString("p2_letra"),
              getBD(rs, "p3"),
              rs.getString("p3_letra"),
              getBD(rs, "p4"),
              rs.getString("p4_letra"),
              getBD(rs, "prom_practicas"),
              getBD(rs, "tarea_libro"),
              rs.getString("tarea_libro_letra"),
              getBD(rs, "tarea_cuaderno"),
              rs.getString("tarea_cuaderno_letra"),
              getBD(rs, "prom_tareas"),
              getBD(rs, "ex_mensual"),
              rs.getString("ex_mensual_letra"),
              getBD(rs, "ex_bimestral"),
              rs.getString("ex_bimestral_letra"),
              getBD(rs, "prom_final"),
              rs.getString("letra")
          );
        }
        return new NotasCursoResumenDto(
            matriculaId,
            cursoId,
            bimestreId,
            null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null,
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

  @Override
  public void guardarNotasCurso(NotasCursoResumenDto dto) throws Exception {
    String call = "{ call sp_guardar_notas_curso(?,?,?,?,?,?,?,?,?,?,?,?) }";

    try (var con = ConexionDB.getConnection();
         var cs  = con.prepareCall(call)) {

      // Parámetros de entrada
      cs.setLong(1, dto.getMatriculaId());
      cs.setLong(2, dto.getCursoId());
      cs.setLong(3, dto.getBimestreId());
      cs.setLong(4, dto.getUsuarioId());

      // Prácticas
      setBDorNull(cs, 5,  dto.getP1());
      setBDorNull(cs, 6,  dto.getP2());
      setBDorNull(cs, 7,  dto.getP3());
      setBDorNull(cs, 8,  dto.getP4());

      // Tareas
      setBDorNull(cs, 9,  dto.getTareaLibro());
      setBDorNull(cs, 10, dto.getTareaCuaderno());

      // Exámenes
      setBDorNull(cs, 11, dto.getExMensual());
      setBDorNull(cs, 12, dto.getExBimestral());

      cs.executeUpdate();
    }
  }

  private static void setBDorNull(java.sql.CallableStatement cs, int index, BigDecimal value) throws java.sql.SQLException {
    if (value != null) cs.setBigDecimal(index, value);
    else cs.setNull(index, java.sql.Types.DECIMAL);
  }

}
