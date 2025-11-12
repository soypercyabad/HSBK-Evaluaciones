package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.ConductaPanelDao;
import pe.iep.hsbk.evaluaciones.dto.ConductaResumenDto;
import pe.iep.hsbk.evaluaciones.dto.RecomendacionAlumnoDto;
import pe.iep.hsbk.evaluaciones.model.Conducta;
import pe.iep.hsbk.evaluaciones.model.EvaluacionFamiliar;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ConductaPanelImpl implements ConductaPanelDao {

    @Override
    public ConductaResumenDto getResumenByAlumno(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId) throws Exception {
        String sql = "{ call sp_get_conducta_resumen_full(?, ?, ?, ?) }";
        ConductaResumenDto out = new ConductaResumenDto();

        try (Connection cn = ConexionDB.getConnection();
             CallableStatement cs = cn.prepareCall(sql)) {

            System.out.printf("[DAO] Llamando sp_get_conducta_resumen(alumnoId=%d, periodoId=%d, nivelId=%d, bimestreId=%d)%n",
                    alumnoId, periodoId, nivelId, bimestreId);

            cs.setLong(1, alumnoId);
            cs.setLong(2, periodoId);
            cs.setLong(3, nivelId);
            cs.setLong(4, bimestreId);

            boolean isResultSet = cs.execute();
            int rsIndex = 0;

            while (true) {
                if (isResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        System.out.println("[DAO] ResultSet #" + rsIndex);
                        if (rsIndex == 0) {
                            // (1) CONDUCTA
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
                        } else if (rsIndex == 1) {
                            // (2) EVALUACIÓN FAMILIAR
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
                        } else if (rsIndex == 2) {
                            List<RecomendacionAlumnoDto> list = new ArrayList<>();
                            while (rs.next()) {
                                RecomendacionAlumnoDto ra = new RecomendacionAlumnoDto();
                                ra.setId(rs.getLong("id"));
                                ra.setMatriculaId(rs.getLong("matricula_id"));
                                ra.setBimestreId(rs.getLong("bimestre_id"));
                                ra.setRecomendacionId(rs.getLong("recomendacion_id")); // <-- IMPORTANTE
                                ra.setMensajeCatalogo(rs.getString("mensaje_catalogo"));
                                ra.setMensajePersonal(rs.getString("mensaje_personal"));
                                Timestamp fr = rs.getTimestamp("fecha_registro");
                                Timestamp fa = rs.getTimestamp("fecha_actualizacion");
                                ra.setFechaRegistro(fr != null ? fr.toLocalDateTime() : null);
                                ra.setFechaActualizacion(fa != null ? fa.toLocalDateTime() : null);

                                System.out.println("[DAO] RA: recId=" + ra.getRecomendacionId() + ", msg=" + ra.getMensajeCatalogo());
                                list.add(ra);
                            }
                            out.setRecomendacionesAlumno(list);
                        }
                        rsIndex++; // <<-- solo cuando hubo ResultSet
                    }
                } else {
                    int updateCount = cs.getUpdateCount();
                    if (updateCount == -1) break; // no más resultados
                    // hay un updateCount (p.ej. 0), no incrementes rsIndex
                    System.out.println("[DAO] updateCount=" + updateCount);
                }

                isResultSet = cs.getMoreResults();
            }

        if (out.getRecomendaciones() == null) out.setRecomendaciones(new ArrayList<>());
        return out;
    }
    }

    @Override
    public void saveConductaBimestre(Long alumnoId, Long periodoId, Long nivelId, Long bimestreId, BigDecimal notaConducta, String utiles, String participacion, String reuniones, String escuelaPadres, String comentarios, Long recomendacionId, String mensajePersonal, Long usuarioId) throws Exception {

            String sql = "{ call sp_save_conducta_bimestre(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";

            try (Connection cn = ConexionDB.getConnection();
                 CallableStatement cs = cn.prepareCall(sql)) {

                cs.setLong(1, alumnoId);
                cs.setLong(2, periodoId);
                cs.setLong(3, nivelId);
                cs.setLong(4, bimestreId);

                if (notaConducta == null) cs.setNull(5, java.sql.Types.DECIMAL);
                else cs.setBigDecimal(5, java.math.BigDecimal.valueOf(notaConducta.doubleValue()));

                cs.setString(6, utiles);
                cs.setString(7, participacion);
                cs.setString(8, reuniones);
                cs.setString(9, escuelaPadres);
                cs.setString(10, comentarios);

                if (recomendacionId == null) cs.setNull(11, java.sql.Types.BIGINT);
                else cs.setLong(11, recomendacionId);

                cs.setString(12, mensajePersonal);
                cs.setLong(13, usuarioId);

                cs.execute();
            }
        }
    }



