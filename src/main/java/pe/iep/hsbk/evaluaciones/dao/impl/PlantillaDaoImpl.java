package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.PlantillaBoletaDAO;
import pe.iep.hsbk.evaluaciones.model.PlantillaBoleta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlantillaDaoImpl implements PlantillaBoletaDAO {

  @Override
  public List<PlantillaBoleta> getPlantillaBoletas() throws Exception {
    String sql =
        "SELECT pb.id, pb.nombre, pb.contenido_html, pb.activo\n" +
        "FROM plantilla_boleta pb\n" +
        "ORDER BY pb.activo DESC, pb.nombre ASC;";

    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql);
         var rs = ps.executeQuery()) {

      List<PlantillaBoleta> list = new ArrayList<>();
      while (rs.next()) {
        PlantillaBoleta pb = new PlantillaBoleta();
        pb.setId(rs.getLong("id"));
        pb.setNombre(rs.getString("nombre"));
        pb.setContenidoHtml(rs.getString("contenido_html"));
        pb.setActivo(rs.getBoolean("activo"));
        list.add(pb);
      }
      return list;
    }
  }

  @Override
  public void guardarPlantillaBoleta(PlantillaBoleta p) throws Exception {
    String sql =
        "INSERT INTO plantilla_boleta (nombre, contenido_html, activo)\n" +
        "VALUES (?, ?, ?);";
    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {
      ps.setString(1, p.getNombre());
      ps.setString(2, p.getContenidoHtml());
      ps.setBoolean(3, p.isActivo());
      ps.executeUpdate();
    }
  }

  @Override
  public void actualizarPlantillaBoleta(PlantillaBoleta p) throws Exception {
    String sql =
        "UPDATE plantilla_boleta\n" +
        "SET nombre = ?, contenido_html = ?, activo = ?\n" +
        "WHERE id = ?;";
    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {
      ps.setString(1, p.getNombre());
      ps.setString(2, p.getContenidoHtml());
      ps.setBoolean(3, p.isActivo());
      ps.setLong(4, p.getId());
      ps.executeUpdate();
    }
  }

  public void actualizarEstadoPlantillaBoleta(PlantillaBoleta p) throws Exception {
    String sql =
        "UPDATE plantilla_boleta\n" +
            "SET nombre = ?, activo = ?\n" +
            "WHERE id = ?;";
    try (var con = ConexionDB.getConnection();
         var ps = con.prepareStatement(sql)) {
      ps.setString(1, p.getNombre());
      ps.setBoolean(2, p.isActivo());
      ps.setLong(3, p.getId());
      ps.executeUpdate();
    }
  }
}
