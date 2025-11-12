package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.SeccionDao;
import pe.iep.hsbk.evaluaciones.dto.RolesEnSeccionDto;
import pe.iep.hsbk.evaluaciones.model.Seccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeccionDaoImpl implements SeccionDao {

  @Override
  public List<Seccion> listarSeccionesActivasPorUsuario(Long periodoId, Long gradoId, Long usuarioId) throws Exception {
    String call = "{ call sp_secciones_activas_por_usuario(?,?,?) }";

    List<Seccion> out = new ArrayList<>();

    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {

      cs.setLong(1, periodoId);
      cs.setLong(2, gradoId);
      cs.setLong(3, usuarioId);

      try (ResultSet rs = cs.executeQuery()) {
        while (rs.next()) {
          Seccion s = new Seccion();
          s.setId(rs.getLong("id"));
          s.setNombre(rs.getString("nombre"));
          out.add(s);
        }
      }
    }
    return out;
  }

  @Override
  public RolesEnSeccionDto getRolesEnSeccion(Long periodoId, Long seccionId, Long usuarioId) throws Exception {
    String call = "{ call sp_roles_usuario_en_seccion(?,?,?) }";
    try (var con = ConexionDB.getConnection();
         var cs  = con.prepareCall(call)) {

      cs.setLong(1, periodoId);
      cs.setLong(2, seccionId);
      cs.setLong(3, usuarioId);

      try (var rs = cs.executeQuery()) {
        if (rs.next()) {
          boolean esDoc = rs.getBoolean("es_docente");
          boolean esTut = rs.getBoolean("es_tutor");
          return new RolesEnSeccionDto(esDoc, esTut);
        }
      }
    }
    return new RolesEnSeccionDto(false, false);
  }

}
