package pe.iep.hsbk.evaluaciones.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

  private static final String DB_URL =  "mysql://root:jNhFQsRfmtlJQrYpPLiAnCLBSDcXjEaM@maglev.proxy.rlwy.net:40876/railway";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "jNhFQsRfmtlJQrYpPLiAnCLBSDcXjEaM";

  public static Connection getConnection() throws SQLException, ClassNotFoundException {
    //Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
  }
}
