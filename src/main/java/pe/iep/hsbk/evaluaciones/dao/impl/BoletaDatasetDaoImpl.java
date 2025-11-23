package pe.iep.hsbk.evaluaciones.dao.impl;

import pe.iep.hsbk.evaluaciones.config.ConexionDB;
import pe.iep.hsbk.evaluaciones.dao.BoletaDatasetDao;
import pe.iep.hsbk.evaluaciones.dto.BoletaAlumnoDatasetDto;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoletaDatasetDaoImpl implements BoletaDatasetDao {

  @Override
  public Map<Long, BoletaAlumnoDatasetDto> obtenerDatasetBoleta(
      long periodoId,
      long seccionId,
      int bimestreNum,
      List<Long> alumnosIds) throws Exception {

    if (alumnosIds == null || alumnosIds.isEmpty()) {
      throw new IllegalArgumentException("Lista de alumnos vacía.");
    }

    String csv = alumnosIds.stream()
        .map(String::valueOf)
        .collect(java.util.stream.Collectors.joining(","));

    Map<Long, BoletaAlumnoDatasetDto> map = new HashMap<>();
    String call = "{CALL sp_boletas_bimestre_dataset(?,?,?,?)}";

    try (Connection cn = ConexionDB.getConnection();
         CallableStatement cs = cn.prepareCall(call)) {

      cs.setLong(1, periodoId);
      cs.setLong(2, seccionId);
      cs.setInt(3, bimestreNum);
      cs.setString(4, csv);

      boolean hasResult = cs.execute();
      int rsIndex = 0;

      while (hasResult) {
        try (ResultSet rs = cs.getResultSet()) {
          if (rsIndex == 0) {
            // RESULT SET 1: Cabecera
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = new BoletaAlumnoDatasetDto();
              dto.setMatriculaId(matId);
              dto.setAlumnoId(rs.getLong("alumno_id"));
              dto.setDni(rs.getString("dni"));
              dto.setApellidos(rs.getString("apellidos"));
              dto.setNombres(rs.getString("nombres"));
              dto.setNumeroOrden(rs.getInt("numero_orden"));
              dto.setSeccion(rs.getString("seccion"));
              dto.setGrado(rs.getString("grado"));
              dto.setNivel(rs.getString("nivel"));
              dto.setPeriodo(rs.getString("periodo"));
              dto.setBimestre(rs.getInt("bimestre"));

              dto.setCursos(new java.util.ArrayList<>());
              dto.setAreas(new java.util.ArrayList<>());

              map.put(matId, dto);
            }
          } else if (rsIndex == 1) {
            // RESULT SET 2: Cursos
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = map.get(matId);
              if (dto == null) continue;

              BoletaAlumnoDatasetDto.CursoNota cnDto =
                  new BoletaAlumnoDatasetDto.CursoNota();
              cnDto.setCurso(rs.getString("curso"));
              cnDto.setArea(rs.getString("area"));
              cnDto.setNotaFinal(rs.getInt("nota_final"));
              cnDto.setLetra(rs.getString("letra"));

              dto.getCursos().add(cnDto);
            }
          } else if (rsIndex == 2) {
            // RESULT SET 3: Áreas
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = map.get(matId);
              if (dto == null) continue;

              BoletaAlumnoDatasetDto.AreaPromedio areaDto =
                  new BoletaAlumnoDatasetDto.AreaPromedio();
              areaDto.setArea(rs.getString("area"));
              areaDto.setPromedioArea(rs.getInt("promedio_area"));
              areaDto.setLetra(rs.getString("letra"));
              dto.getAreas().add(areaDto);
            }
          } else if (rsIndex == 3) {
            // RESULT SET 4: Puntaje y puesto
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = map.get(matId);
              if (dto == null) continue;

              dto.setPuntajeTotal(rs.getDouble("puntaje"));
              dto.setPuesto(rs.getInt("puesto"));
            }
          } else if (rsIndex == 4) {
            // RESULT SET 5: Conducta
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = map.get(matId);
              if (dto == null) continue;

              dto.setConductaNota(rs.getInt("coducta_nota"));
              dto.setConductaLetra(rs.getString("conducta_letra"));
            }
          } else if (rsIndex == 5) {
            // RESULT SET 6: Evaluación familiar
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = map.get(matId);
              if (dto == null) continue;

              dto.setUtiles(rs.getString("utiles"));
              dto.setParticipacion(rs.getString("participacion"));
              dto.setReuniones(rs.getString("reuniones"));
              dto.setEscuelaPadres(rs.getString("escuela_padres"));
              dto.setComentariosFam(rs.getString("comentarios"));
            }
          } else if (rsIndex == 6) {
            // RESULT SET 7: Recomendación
            while (rs.next()) {
              Long matId = rs.getLong("matricula_id");
              BoletaAlumnoDatasetDto dto = map.get(matId);
              if (dto == null) continue;

              dto.setRecomendacion(rs.getString("recomendacion"));
            }
          }
        }

        rsIndex++;
        hasResult = cs.getMoreResults();
      }
    }

    return map;
  }

  @Override
  public String obtenerNombreZip(long nivelId, long seccionId, long gradoId) throws Exception {
    String call = "{CALL sp_get_nombre_zip(?,?,?)}";

    try (Connection cn = ConexionDB.getConnection();
         CallableStatement cs = cn.prepareCall(call)) {

      cs.setLong(1, nivelId);
      cs.setLong(2, seccionId);
      cs.setLong(3, gradoId);

      try (ResultSet rs = cs.executeQuery()) {
        if (rs.next()) {
          return rs.getString("nombre_zip");
        } else {
          return null;
        }
      }
    }
  }
}
