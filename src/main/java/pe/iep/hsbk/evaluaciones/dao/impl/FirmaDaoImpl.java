package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.FirmaDao;
import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class FirmaDaoImpl implements FirmaDao {

  @Override
  public List<Firma> getListFirmas() throws Exception {
    String sql = "CALL sp_get_firma();";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql);
         var rs = ps.executeQuery()) {

      List<Firma> list = new ArrayList<>();
      while (rs.next()) {
        Firma f = new Firma();
        f.setId(rs.getLong("id"));
        f.setUsuarioId(rs.getLong("usuario_id"));
        f.setNombre(rs.getString("nombre"));
        f.setRol(rs.getString("rol"));
        f.setImagen(rs.getBytes("imagen"));
        f.setActivo(rs.getBoolean("activo"));
        list.add(f);
      }
      return list;
    }
  }

  @Override
  public List<Usuario> getUsuariosFirmas() throws Exception {
    String sql = "CALL sp_get_firma_usuario();";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql);
         var rs = ps.executeQuery()) {

      List<Usuario> list = new ArrayList<>();
      while (rs.next()) {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        list.add(u);
      }
      return list;
    }
  }

  @Override
  public void guardarFirma(Firma f) throws Exception {
    String sql = "CALL sp_ins_firma(?, ?, ?)";
    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {
      ps.setLong(1, f.getUsuarioId());
      ps.setBytes(2, f.getImagen());             // <- bytes del PNG
      ps.setBoolean(3, f.isActivo());
      ps.executeUpdate();
    }
  }

  @Override
  public void actualizarFirma(Firma f) throws Exception {
    String sql = "CALL sp_upd_firma(?, ?, ?, ?)";
    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {
      ps.setLong(1, f.getId());
      ps.setLong(2, f.getUsuarioId());
      ps.setBytes(3, f.getImagen());
      ps.setBoolean(4, f.isActivo());
      ps.executeUpdate();
    }
  }

  @Override
  public Firma getFirmaPorUsuarioId(long usuarioId) throws Exception {
    String call = "CALL sp_get_firma_por_usuario_id(?);";

    try (var con = ConexionDB.getConnection();
         var cs = con.prepareCall(call)) {

      cs.setLong(1, usuarioId);

      try (var rs = cs.executeQuery()) {
        if (rs.next()) {
          Firma f = new Firma();

          f.setId(rs.getLong("id"));
          f.setUsuarioId(rs.getLong("usuario_id"));
          f.setImagen(rs.getBytes("imagen"));
          f.setActivo(rs.getBoolean("activo"));

          // Estos no vienen en el SP actual, así que los dejamos null por ahora
          f.setNombre(null);
          f.setRol(null);

          return f;
        } else {
          return null;
        }
      }
    }
  }

  @Override
  public Firma getFirmaDirectorActiva() throws Exception {
    String call = "CALL sp_get_firma_director_activo();";

    try (var con = ConexionDB.getConnection();
         var cs = con.prepareCall(call);

         var rs = cs.executeQuery()) {

      if (rs.next()) {
        Firma f = new Firma();
        f.setId(null);
        f.setUsuarioId(null);
        f.setNombre(null);
        f.setRol(null);
        f.setImagen(rs.getBytes("imagen"));
        f.setActivo(false);
        return f;
      } else {
        return null;
      }
    }
  }
}
