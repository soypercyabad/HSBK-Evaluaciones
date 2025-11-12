package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.RecomendacionDAO;
import pe.iep.hsbk.evaluaciones.model.RecomendacionCatalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecomendacionImpl implements RecomendacionDAO {

    @Override
    public List<RecomendacionCatalogo> getRecomendaciones() throws Exception {
        String sql = "SELECT id, mensaje, activo FROM recomendacion_catalogo WHERE activo = 1";
        List<RecomendacionCatalogo> list = new ArrayList<>();

        try (var con = ConexionDB.getConnection();
             var ps = con.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                RecomendacionCatalogo r = new RecomendacionCatalogo();
                r.setId(rs.getLong("id"));
                r.setMensaje(rs.getString("mensaje"));
                r.setActivo(rs.getInt("activo") == 1);

                list.add(r);
            }
        }
        return list;
    }

}
