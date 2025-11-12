package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.UsuarioDao;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDaoImpl implements UsuarioDao {

  @Override
  public Optional<Usuario> findByUsername(String username) throws Exception {
    // SP: sp_usuario_find_by_username(p_username)
    String call = "{ call sp_usuario_find_by_username(?) }";
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {
      cs.setString(1, username);
      try (ResultSet rs = cs.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNombre(rs.getString("nombre"));
        u.setActivo(rs.getBoolean("activo"));
        return Optional.of(u);
      }
    }
  }

  @Override
  public List<String> findRolesByUserId(long userId) throws Exception {
    // SP: sp_usuario_roles_by_user_id(p_user_id)
    String call = "{ call sp_usuario_roles_by_user_id(?) }";
    List<String> roles = new ArrayList<>();
    try (Connection con = ConexionDB.getConnection();
         CallableStatement cs = con.prepareCall(call)) {
      cs.setLong(1, userId);
      try (ResultSet rs = cs.executeQuery()) {
        while (rs.next()) roles.add(rs.getString("nombre"));
      }
    }
    return roles;
  }
}
