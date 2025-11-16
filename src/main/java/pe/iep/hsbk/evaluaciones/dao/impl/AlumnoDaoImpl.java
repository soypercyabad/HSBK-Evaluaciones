package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.AlumnoDao;
import pe.iep.hsbk.evaluaciones.model.Alumno;

import java.util.ArrayList;
import java.util.List;

public class AlumnoDaoImpl implements AlumnoDao {

  @Override
  public List<Alumno> listarPorSeccionPeriodo(int seccionId, int periodoId) throws Exception {
    String call = "{ call sp_alumnos_por_seccion_periodo(?,?) }";
    List<Alumno> out = new ArrayList<>();
    try (var con = ConexionDB.getConnection();
         var cs = con.prepareCall(call)) {

      cs.setInt(1, seccionId);
      cs.setInt(2, periodoId);

      try (var rs = cs.executeQuery()) {
        while (rs.next()) {
          Alumno a = new Alumno();
          a.setId(rs.getLong("id"));
          a.setApellidos(rs.getString("apellidos"));
          a.setNombres(rs.getString("nombres"));
          a.setCodigo(rs.getString("codigo"));
          out.add(a);
        }
      }
    }
    return out;
  }

  @Override
  public Alumno obtenerPorId(int alumnoId, int nivelId) throws Exception {
    String call = "{ CALL sp_get_alumno_info (?, ?) }";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareCall(call)) {

      ps.setInt(1, alumnoId);
      ps.setInt(2, nivelId);

      try (var rs = ps.executeQuery()) {

        if (rs.next()) {
          Alumno a = new Alumno();
          a.setDni(rs.getString("dni"));
          a.setNombres(rs.getString("nombres"));
          a.setApellidos(rs.getString("apellidos"));
          a.setGrado(rs.getString("grado"));
          a.setNivel(rs.getString("nivel"));
          a.setSeccion(rs.getString("seccion"));
          a.setNumeroOrden(rs.getInt("numero_orden"));
          return a;
        } else {
          return null;
        }
      }
    }
  }

}

