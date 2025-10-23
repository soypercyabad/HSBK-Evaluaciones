package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.UsuarioDao;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDaoImpl implements UsuarioDao {

  @Override
  public Optional<Usuario> findByUsername(String username) throws Exception {
    String sql = "SELECT id, username, password_hash, nombre, activo FROM usuario WHERE username = ?";
    try (Connection con = ConexionDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
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
    String sql = "SELECT r.nombre FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id WHERE ur.usuario_id = ?";
    List<String> roles = new ArrayList<>();
    try (Connection con = ConexionDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) roles.add(rs.getString(1));
      }
    }
    return roles;
  }
}
