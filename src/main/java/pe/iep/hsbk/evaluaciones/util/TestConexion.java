package pe.iep.hsbk.evaluaciones.util;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConexion {
  public static void main(String[] args) {
    String sql = "SELECT nombre FROM periodo WHERE activo = 1 ORDER BY id";
    try (Connection con = ConexionDB.getConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(sql)) {

      while (rs.next()) {
        String numPeriodo = rs.getString("nombre");
      }

    } catch (SQLException e) {
      System.err.println("Error en la conexión o consulta SQL.");
      System.out.println("SQL State: " + e.getSQLState());
      System.out.println("Error Code: " + e.getErrorCode());
      System.out.println("Message: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Error inesperado.");
      e.printStackTrace();
    }
  }
}
