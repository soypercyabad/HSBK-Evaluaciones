package pe.iep.hsbk.evaluaciones.dao;

public interface MatriculaDao {
    Long getMatriculaId(Long alumnoId, Long periodoId, Long nivelId) throws Exception;
}
