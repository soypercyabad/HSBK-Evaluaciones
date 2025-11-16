package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Sello;

import java.sql.SQLException;
import java.util.List;

public interface SelloDao {
    List<Sello> getSellos() throws Exception;
    void guardarSello(Sello s) throws Exception;
    void actualizarSello(Sello s) throws Exception;
    Sello obtenerSelloActivo() throws Exception;
}
