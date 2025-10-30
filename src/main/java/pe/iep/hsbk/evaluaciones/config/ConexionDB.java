package pe.iep.hsbk.evaluaciones.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

  private static final String HOST = "maglev.proxy.rlwy.net";
  private static final String PORT = "40876";
  private static final String DB_NAME = "hbk_db";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "jNhFQsRfmtlJQrYpPLiAnCLBSDcXjEaM";

  private static final String DB_URL =
      "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
          + "?useUnicode=true"
          + "&characterEncoding=utf8"
          + "&serverTimezone=America/Lima"
          + "&useSSL=true"
          + "&requireSSL=true"
          + "&allowPublicKeyRetrieval=true";


  public static Connection getConnection() throws SQLException, ClassNotFoundException {
    return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
  }
}
