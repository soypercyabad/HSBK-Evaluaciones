package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.MatriculaDao;

public class MatriculaDaoImpl implements MatriculaDao {

    @Override
    public Long getMatriculaId(Long alumnoId, Long periodoId, Long nivelId) throws Exception {

        String sql =
                "SELECT id " +
                        "FROM matricula " +
                        "WHERE alumno_id = ? " +
                        "  AND periodo_id = ? " +
                        "  AND nivel_id   = ? " +
                        "LIMIT 1";

        try (var con = ConexionDB.getConnection();
             var ps  = con.prepareStatement(sql)) {

            ps.setLong(1, alumnoId);
            ps.setLong(2, periodoId);
            ps.setLong(3, nivelId);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("matricula_id");
                }
                return null;
            }
        }
    }

}
