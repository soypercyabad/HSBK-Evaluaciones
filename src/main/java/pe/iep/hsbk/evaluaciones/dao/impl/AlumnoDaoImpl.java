package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.AlumnoDao;
import pe.iep.hsbk.evaluaciones.model.Alumno;

import java.util.ArrayList;
import java.util.List;

public class AlumnoDaoImpl implements AlumnoDao {

  @Override
  public List<Alumno> listarPorSeccionPeriodo(int seccionId, int periodoId) throws Exception {
    String sql = "SELECT a.id, a.dni, a.codigo, a.apellidos, a.nombres, a.numero_orden, a.activo\n" +
        "FROM matricula m\n" +
        "JOIN alumno a ON a.id = m.alumno_id\n" +
        "WHERE m.seccion_id = ? AND m.periodo_id = ?\n" +
        "ORDER BY a.apellidos, a.nombres;";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {

      ps.setInt(1, seccionId);
      ps.setInt(2, periodoId);

      try (var rs = ps.executeQuery()) {

        List<Alumno> alumnos = new ArrayList<>();

        while (rs.next()) {
          Alumno a = new Alumno();
          a.setId(rs.getLong("id"));
          a.setDni(rs.getString("dni"));
          a.setCodigo(rs.getString("codigo"));
          a.setApellidos(rs.getString("apellidos"));
          a.setNombres(rs.getString("nombres"));
          a.setActivo(rs.getBoolean("activo"));
          alumnos.add(a);
        }
        return alumnos;
      }
    }
  }
}

