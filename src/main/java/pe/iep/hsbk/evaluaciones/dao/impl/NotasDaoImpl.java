package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.NotasDao;
import pe.iep.hsbk.evaluaciones.dto.*;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class NotasDaoImpl implements NotasDao {

  @Override
  public PracticasDto getPracticas(long matriculaId, long cursoId, long bimestreId) throws Exception {

    String sql = "SELECT p1, p2, p3, p4, prom_practicas\n" +
        "FROM vw_prom_practicas\n" +
        "WHERE matricula_id=? AND curso_id=? AND bimestre_id=?;";

    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {

      ps.setLong(1, matriculaId);
      ps.setLong(2, cursoId);
      ps.setLong(3, bimestreId);

      try (var rs = ps.executeQuery()) {
        if (rs.next()) {
          return new PracticasDto(
              getBD(rs, "p1"),
              getBD(rs, "p2"),
              getBD(rs, "p3"),
              getBD(rs, "p4"),
              getBD(rs, "prom_practicas")
          );
        }
        return new PracticasDto();
      }
    }
  }

  @Override
  public TareasDto getTareas(long matriculaId, long cursoId, long bimestreId) throws Exception {

    String sql = "SELECT tarea_libro, tarea_cuaderno, prom_tareas\n" +
        "FROM vw_prom_tareas\n" +
        "WHERE matricula_id=? AND curso_id=? AND bimestre_id=?;";

    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {

      ps.setLong(1, matriculaId);
      ps.setLong(2, cursoId);
      ps.setLong(3, bimestreId);

      try (var rs = ps.executeQuery()) {
        if (rs.next()) {
          return new TareasDto(
              getBD(rs, "tarea_libro"),
              getBD(rs, "tarea_cuaderno"),
              getBD(rs, "prom_tareas")
          );
        }
        return new TareasDto();
      }
    }
  }

  @Override
  public ExamenesDto getExamenes(long matriculaId, long cursoId, long bimestreId) throws Exception {

    String sql = "SELECT ex_mensual, ex_bimestral\n" +
        "FROM vw_examenes\n" +
        "WHERE matricula_id=? AND curso_id=? AND bimestre_id=?;";

    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {

      ps.setLong(1, matriculaId);
      ps.setLong(2, cursoId);
      ps.setLong(3, bimestreId);

      try (var rs = ps.executeQuery()) {
        if (rs.next()) {
          return new ExamenesDto(
              getBD(rs, "ex_mensual"),
              getBD(rs, "ex_bimestral")
          );
        }
        return new ExamenesDto();
      }
    }
  }

  @Override
  public PromedioDto getPromedio(long matriculaId, long cursoId, long bimestreId) throws Exception {

    String sql = "SELECT prom_practicas, prom_tareas, ex_mensual, ex_bimestral, promedio_curso, letra\n" +
        "FROM vw_promedio_curso_bimestre\n" +
        "WHERE matricula_id=? AND curso_id=? AND bimestre_id=?;";

    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {

      ps.setLong(1, matriculaId);
      ps.setLong(2, cursoId);
      ps.setLong(3, bimestreId);

      try (var rs = ps.executeQuery()) {
        if (rs.next()) {
          return new PromedioDto(
              getBD(rs, "prom_practicas"),
              getBD(rs, "prom_tareas"),
              getBD(rs, "ex_mensual"),
              getBD(rs, "ex_bimestral"),
              getBD(rs, "promedio_curso"),
              rs.getString("letra")
          );
        }
        return new PromedioDto();
      }
    }
  }

  // -------- helpers --------
  private static BigDecimal getBD(ResultSet rs, String col) throws java.sql.SQLException {
    BigDecimal v = rs.getBigDecimal(col);
    return rs.wasNull() ? null : v;
  }
}
