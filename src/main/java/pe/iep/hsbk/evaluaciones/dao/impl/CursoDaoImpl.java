package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.CursoDao;
import pe.iep.hsbk.evaluaciones.model.Curso;

import java.util.ArrayList;
import java.util.List;

public class CursoDaoImpl implements CursoDao {

  @Override
  public List<Curso> listarCursosActivos(Long nivelId) throws Exception {
    String sql = "CALL sp_get_curso_nivel (?)";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {

      ps.setLong(1, nivelId);

      try (var rs = ps.executeQuery()) {

        List<Curso> cursos = new ArrayList<>();

        while (rs.next()) {
          Curso c = new Curso();
          c.setId(rs.getLong("id"));
          c.setNivelId(rs.getLong("nivel_id"));
          c.setNombre(rs.getString("nombre"));
          c.setActivo(rs.getBoolean("activo"));
          cursos.add(c);
        }
        return cursos;
      }
    }
  }
}
