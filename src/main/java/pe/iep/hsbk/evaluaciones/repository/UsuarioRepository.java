package pe.iep.hsbk.evaluaciones.repository;

import pe.iep.hsbk.evaluaciones.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioRepository {
  public static class UsuarioRow {
    public int id;
    public String username;
    public String passwordHash;
    public String estado; // ACTIVO/INACTIVO/BLOQUEADO
    public int rolId;
  }

  public UsuarioRow findByUsername(String username) throws Exception {
    String sql = "SELECT id, username, password_hash, estado, rol_id FROM usuario WHERE username = ?";
    try (Connection cn = DatabaseConfig.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        UsuarioRow u = new UsuarioRow();
        u.id = rs.getInt("id");
        u.username = rs.getString("username");
        u.passwordHash = rs.getString("password_hash");
        u.estado = rs.getString("estado");
        u.rolId = rs.getInt("rol_id");
        return u;
      }
    }
  }

  // (Opcional) Crear usuario con hash (útil para tu seed desde la app)
  public int insertUsuarioBcrypt(String username, String plainPassword, String estado, int rolId) throws Exception {
    String sql = "INSERT INTO usuario (username, password_hash, estado, rol_id) VALUES (?,?,?,?)";
    try (Connection cn = DatabaseConfig.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      String hash = org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(10));
      ps.setString(1, username);
      ps.setString(2, hash);
      ps.setString(3, estado);
      ps.setInt(4, rolId);
      return ps.executeUpdate();
    }
  }
}
