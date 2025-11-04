package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.Sello;

public class SelloDaoImpl {

    public void guardarSello(Sello s) throws Exception {
        String sql = "CALL sp_ins_sello(?, ?)";
        try (var con = ConexionDB.getConnection();
             var ps  = con.prepareStatement(sql)) {
            ps.setBytes(1, s.getImagen());             // <- bytes del PNG
            ps.setBoolean(2, s.isActivo());
            ps.executeUpdate();
        }
    }

}
