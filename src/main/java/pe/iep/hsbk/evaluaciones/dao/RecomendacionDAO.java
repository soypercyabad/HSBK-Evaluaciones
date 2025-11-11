package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.RecomendacionCatalogo;

import java.util.List;

public interface RecomendacionDAO {
    List<RecomendacionCatalogo> getRecomendaciones()  throws Exception;
}
