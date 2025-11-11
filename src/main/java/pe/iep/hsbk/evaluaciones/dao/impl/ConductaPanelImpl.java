package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.ConductaPanelDao;
import pe.iep.hsbk.evaluaciones.dto.ConductaResumen;
import pe.iep.hsbk.evaluaciones.model.Conducta;
import pe.iep.hsbk.evaluaciones.model.EvaluacionFamiliar;
import pe.iep.hsbk.evaluaciones.model.RecomendacionCatalogo;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ConductaPanelImpl implements ConductaPanelDao {

    @Override
    public ConductaResumen getResumenByAlumno(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception {
        String sql = "{ call sp_get_conducta_resumen(?, ?, ?, ?) }";
        ConductaResumen out = new ConductaResumen();

        try (Connection cn = ConexionDB.getConnection();
             CallableStatement cs = cn.prepareCall(sql)) {

            System.out.printf("[DAO] Llamando sp_get_conducta_resumen(alumnoId=%d, periodoId=%d, nivelId=%d, bimestreId=%d)%n",
                    alumnoId, periodoId, nivelId, bimestreId);

            cs.setLong(1, alumnoId);
            cs.setLong(2, periodoId);
            cs.setLong(3, nivelId);
            cs.setLong(4, bimestreId);

            boolean hasResults = cs.execute();
            int idx = 0;

            while (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (idx == 0) {
                        if (rs.next()) {
                            Conducta c = new Conducta();
                            c.setId(rs.getLong("id"));
                            c.setMatriculaId(rs.getLong("matricula_id"));
                            c.setBimestreId(rs.getLong("bimestre_id"));
                            c.setNota(rs.getDouble("nota"));
                            c.setLetra(rs.getString("letra"));
                            Timestamp fr = rs.getTimestamp("fecha_registro");
                            Timestamp fa = rs.getTimestamp("fecha_actualizacion");
                            c.setFechaRegistro(fr != null ? fr.toLocalDateTime() : null);
                            c.setFechaActualizacion(fa != null ? fa.toLocalDateTime() : null);
                            out.setConducta(c);
                        }
                    } else if (idx == 1) {
                        if (rs.next()) {
                            EvaluacionFamiliar e = new EvaluacionFamiliar();
                            e.setId(rs.getLong("id"));
                            e.setMatriculaId(rs.getLong("matricula_id"));
                            e.setBimestreId(rs.getLong("bimestre_id"));
                            e.setUtiles(rs.getString("utiles"));
                            e.setParticipacion(rs.getString("participacion"));
                            e.setReuniones(rs.getString("reuniones"));
                            e.setEscuelaPadres(rs.getString("escuela_padres"));
                            e.setComentarios(rs.getString("comentarios"));
                            Timestamp fr = rs.getTimestamp("fecha_registro");
                            Timestamp fa = rs.getTimestamp("fecha_actualizacion");
                            e.setFechaRegistro(fr != null ? fr.toLocalDateTime() : null);
                            e.setFechaActualizacion(fa != null ? fa.toLocalDateTime() : null);
                            out.setEvaluacionFamiliar(e);
                        }
                    } else if (idx == 2) {
                        List<RecomendacionCatalogo> list = new ArrayList<>();
                        while (rs.next()) {
                            RecomendacionCatalogo r = new RecomendacionCatalogo();
                            r.setId(rs.getLong("id"));
                            r.setMensaje(rs.getString("mensaje"));
                            r.setActivo(rs.getBoolean("activo"));
                            list.add(r);
                        }
                        out.setRecomendaciones(list);
                    }
                }

                hasResults = cs.getMoreResults();
                idx++;
            }
        }

        if (out.getRecomendaciones() == null) out.setRecomendaciones(new ArrayList<>());
        return out;
    }

}

