package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.Sello;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.util.List;

public interface SelloDao {
    void guardarSello(Sello s) throws Exception;
}
