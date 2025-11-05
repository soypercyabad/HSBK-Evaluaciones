package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.SelloDao;
import pe.iep.hsbk.evaluaciones.model.Sello;

import java.util.ArrayList;
import java.util.List;

public class SelloDaoImpl implements SelloDao {

    @Override
    public List<Sello> getSellos() throws Exception {
        String sql = "CALL sp_get_sello();";

        try (var con = ConexionDB.getConnection();
             var ps = con.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            List<Sello> lista = new ArrayList<>();
            while (rs.next()) {
                Sello sello = new Sello();
                sello.setId(rs.getLong("id"));
                sello.setNombre(rs.getString("nombre"));
                sello.setSello(rs.getBytes("sello"));
                sello.setActivo(rs.getBoolean("activo"));
                lista.add(sello);
            }
            return lista;
        }
    }

    @Override
    public void guardarSello(Sello s) throws Exception {
        String sql = "CALL sp_ins_sello(?,?, ?)";
        try (var con = ConexionDB.getConnection();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre());
            ps.setBytes(2, s.getSello());             // <- bytes del PNG
            ps.setBoolean(3, s.isActivo());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizarSello(Sello s) throws Exception {
        String sql = "CALL sp_upd_sello(?, ?, ?)";
        try (var con = ConexionDB.getConnection();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre());
            ps.setBytes(2, s.getSello());             // <- bytes del PNG
            ps.setBoolean(3, s.isActivo());
            ps.executeUpdate();
        }
    }

}
