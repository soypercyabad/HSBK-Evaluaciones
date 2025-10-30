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
    String sql = "CALL sp_getListFirmas();";

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
    String sql = "CALL sp_getUsuarioFirma();";

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
    String sql = "INSERT INTO firma (usuario_id, imagen, activo) VALUES (?, ?, ?)";
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
    String sql = "UPDATE firma SET usuario_id=?, imagen=?, activo=? WHERE id=?";
    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {
      ps.setLong(1, f.getUsuarioId());
      ps.setBytes(2, f.getImagen());
      ps.setBoolean(3, f.isActivo());
      ps.setLong(4, f.getId());
      ps.executeUpdate();
    }
  }

  @Override
  public void actualizarEstadoFirma(Long id, boolean activo) throws Exception {
    String sql = "UPDATE firma SET activo=? WHERE id=?";
    try (var con = ConexionDB.getConnection();
         var ps  = con.prepareStatement(sql)) {
      ps.setBoolean(1, activo);
      ps.setLong(2, id);
      ps.executeUpdate();
    }
  }

}
