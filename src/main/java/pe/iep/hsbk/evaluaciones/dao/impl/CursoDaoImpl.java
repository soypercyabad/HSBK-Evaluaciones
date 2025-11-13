package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.CursoDao;
import pe.iep.hsbk.evaluaciones.model.Curso;

import java.util.ArrayList;
import java.util.List;

public class CursoDaoImpl implements CursoDao {

  @Override
  public List<Curso> listarCursosAsignados(Long periodoId, Long seccionId, Long usuarioId) throws Exception {
    String call = "CALL sp_get_cursos_asignados(?,?,?)";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareCall(call)) {

      ps.setLong(1, periodoId);
      ps.setLong(2, seccionId);
      ps.setLong(3, usuarioId);

      try (var rs = ps.executeQuery()) {
        List<Curso> cursos = new ArrayList<>();

        while (rs.next()) {
          Curso c = new Curso();
          c.setId(rs.getLong("curso_id"));
          c.setNivelId(rs.getLong("nivel_id"));
          c.setNombre(rs.getString("curso_nombre"));
          c.setActivo(rs.getBoolean("activo"));
          cursos.add(c);
        }
        return cursos;
      }
    }
  }
}
