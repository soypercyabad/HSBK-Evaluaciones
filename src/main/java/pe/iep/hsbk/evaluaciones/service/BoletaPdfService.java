package pe.iep.hsbk.evaluaciones.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import pe.iep.hsbk.evaluaciones.dao.BoletaDatasetDao;
import pe.iep.hsbk.evaluaciones.dao.FirmaDao;
import pe.iep.hsbk.evaluaciones.dao.PlantillaBoletaDao;
import pe.iep.hsbk.evaluaciones.dao.SelloDao;
import pe.iep.hsbk.evaluaciones.dto.BoletaAlumnoDatasetDto;
import pe.iep.hsbk.evaluaciones.model.Alumno;
import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.PlantillaBoleta;
import pe.iep.hsbk.evaluaciones.model.Sello;
import pe.iep.hsbk.evaluaciones.service.AuthService.UserSession;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BoletaPdfService {

  private final PlantillaBoletaDao plantillaBoletaDao;
  private final BoletaDatasetDao boletaDatasetDao;
  private final FirmaDao firmaDao;
  private final SelloDao selloDao;

  public BoletaPdfService(PlantillaBoletaDao plantillaBoletaDao,
                          BoletaDatasetDao boletaDatasetDao,
                          FirmaDao firmaDao,
                          SelloDao selloDao) {
    this.plantillaBoletaDao = plantillaBoletaDao;
    this.boletaDatasetDao = boletaDatasetDao;
    this.firmaDao = firmaDao;
    this.selloDao = selloDao;
  }

  // --------------------------------------------------------
  // METODO PRINCIPAL
  // --------------------------------------------------------
  public void generarBoletas(long periodoId,
                             long seccionId,
                             long nivelId,
                             int bimestreNum,
                             List<Alumno> alumnos,
                             UserSession userSession,
                             File destino) throws Exception {

    if (alumnos == null || alumnos.isEmpty()) {
      throw new IllegalArgumentException("No hay alumnos seleccionados.");
    }

    // Plantilla
    PlantillaBoleta plantilla = plantillaBoletaDao.obtenerPlantillaActiva();
    if (plantilla == null || plantilla.getContenidoHtml() == null) {
      throw new IllegalStateException("No hay plantilla de boleta activa.");
    }
    String plantillaHtml = plantilla.getContenidoHtml();

    // Firmas
    Firma firmaTutor = firmaDao.getFirmaPorUsuarioId(userSession.getUserId());
    FirmaDirectoraYSello firmaDirSello = cargarFirmaDirectoraYSello();

    // ==========================
    // 1) Obtener datasets por bimestre
    // ==========================
    List<Long> alumnosIds = alumnos.stream()
        .map(Alumno::getMatriculaId)
        .collect(Collectors.toList());

    // Map<bimestre, Map<matriculaId, BoletaAlumnoDatasetDto>>
    Map<Integer, Map<Long, BoletaAlumnoDatasetDto>> datasetsPorBimestre =
        cargarDatasetsMultiBimestre(periodoId, seccionId, bimestreNum, alumnosIds);

    // Si solo es 1 alumno → PDF directo
    if (alumnos.size() == 1) {
      Alumno a = alumnos.get(0);
      byte[] pdf = generarPdfParaAlumnoMulti(
          a,
          datasetsPorBimestre,
          bimestreNum,
          userSession,
          plantillaHtml,
          firmaTutor,
          firmaDirSello
      );

      if (pdf == null) {
        throw new IllegalStateException("No se encontró dataset para el alumno seleccionado.");
      }

      try (FileOutputStream fos = new FileOutputStream(destino)) {
        fos.write(pdf);
      }
      return;
    }

    // Varios alumnos → ZIP
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destino))) {
      for (Alumno a : alumnos) {
        byte[] pdf = generarPdfParaAlumnoMulti(
            a,
            datasetsPorBimestre,
            bimestreNum,
            userSession,
            plantillaHtml,
            firmaTutor,
            firmaDirSello
        );
        if (pdf == null) continue; // por si algún alumno no tiene dataset

        ZipEntry entry = new ZipEntry(formatearNombreArchivoAlumno(a) + ".pdf");
        zos.putNextEntry(entry);
        zos.write(pdf);
        zos.closeEntry();
      }
    }
  }

  // Carga datasets para bimestres 1..bimestreSeleccionado
  private Map<Integer, Map<Long, BoletaAlumnoDatasetDto>> cargarDatasetsMultiBimestre(
      long periodoId,
      long seccionId,
      int bimestreSeleccionado,
      List<Long> alumnosIds) throws Exception {

    Map<Integer, Map<Long, BoletaAlumnoDatasetDto>> result = new HashMap<>();

    int maxBimestre = Math.max(1, Math.min(4, bimestreSeleccionado)); // limitar a 1..4

    for (int b = 1; b <= maxBimestre; b++) {
      Map<Long, BoletaAlumnoDatasetDto> map =
          boletaDatasetDao.obtenerDatasetBoleta(periodoId, seccionId, b, alumnosIds);
      result.put(b, map);
    }

    return result;
  }

  // --------------------------------------------------------
  // FIRMA DIRECTORA & SELLO
  // --------------------------------------------------------
  private static class FirmaDirectoraYSello {
    byte[] firmaDirectora;
    byte[] selloInstitucion;
  }

  private FirmaDirectoraYSello cargarFirmaDirectoraYSello() throws Exception {
    FirmaDirectoraYSello dto = new FirmaDirectoraYSello();

    Firma firmaDir = firmaDao.getFirmaDirectorActiva();
    if (firmaDir != null) dto.firmaDirectora = firmaDir.getImagen();

    Sello sello = selloDao.obtenerSelloActivo();
    if (sello != null) dto.selloInstitucion = sello.getSello();

    return dto;
  }

  // --------------------------------------------------------
  // GENERAR PDF POR ALUMNO (MULTI-BIMESTRE)
  // --------------------------------------------------------
  private byte[] generarPdfParaAlumnoMulti(Alumno alumno,
                                           Map<Integer, Map<Long, BoletaAlumnoDatasetDto>> datasetsPorBimestre,
                                           int bimestreSeleccionado,
                                           UserSession userSession,
                                           String plantillaHtml,
                                           Firma firmaTutor,
                                           FirmaDirectoraYSello firmaDirSello) throws Exception {

    Long matId = alumno.getMatriculaId();

    // Construir mapa <bimestre, datasetAlumno>
    Map<Integer, BoletaAlumnoDatasetDto> dsAlumnoPorBimestre = new HashMap<>();
    for (Map.Entry<Integer, Map<Long, BoletaAlumnoDatasetDto>> entry : datasetsPorBimestre.entrySet()) {
      Integer bim = entry.getKey();
      Map<Long, BoletaAlumnoDatasetDto> map = entry.getValue();
      if (map == null) continue;
      BoletaAlumnoDatasetDto ds = map.get(matId);
      if (ds != null) {
        dsAlumnoPorBimestre.put(bim, ds);
      }
    }

    if (dsAlumnoPorBimestre.isEmpty()) {
      // No hay datos para este alumno (por algún motivo)
      return null;
    }

    // Escogemos un dataset "base" para los datos generales (nombre, DNI, etc.)
    BoletaAlumnoDatasetDto dsBase = elegirDatasetBase(dsAlumnoPorBimestre, bimestreSeleccionado);

    Path tempDir = Files.createTempDirectory("boleta-pdf-" + dsBase.getMatriculaId() + "-");

    try {
      String htmlPersonalizado = renderizarHtmlBoletaMulti(
          plantillaHtml,
          alumno,
          dsBase,
          dsAlumnoPorBimestre,
          bimestreSeleccionado,
          userSession,
          firmaTutor,
          firmaDirSello,
          tempDir
      );

      return htmlToPdf(htmlPersonalizado, tempDir);

    } finally {
      deleteDirectoryRecursively(tempDir);
    }
  }

  private BoletaAlumnoDatasetDto elegirDatasetBase(Map<Integer, BoletaAlumnoDatasetDto> dsAlumnoPorBimestre,
                                                   int bimestreSeleccionado) {
    // Preferimos el dataset del bimestre seleccionado;
    // si no hay, buscamos el mayor bimestre disponible.
    BoletaAlumnoDatasetDto base = dsAlumnoPorBimestre.get(bimestreSeleccionado);
    if (base != null) return base;

    int maxKey = dsAlumnoPorBimestre.keySet().stream().max(Integer::compareTo).orElse(1);
    return dsAlumnoPorBimestre.get(maxKey);
  }

  // --------------------------------------------------------
  // BORRAR CARPETA TEMPORAL
  // --------------------------------------------------------
  private void deleteDirectoryRecursively(Path path) {
    try {
      if (Files.notExists(path)) return;

      Files.walk(path)
          .sorted((a, b) -> b.compareTo(a))
          .forEach(p -> {
            try {
              Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
          });

    } catch (Exception ignored) {
    }
  }

  // --------------------------------------------------------
  // RENDER HTML usando datasets de varios bimestres
  // --------------------------------------------------------
  private String renderizarHtmlBoletaMulti(String plantilla,
                                           Alumno alumno,
                                           BoletaAlumnoDatasetDto dsBase,
                                           Map<Integer, BoletaAlumnoDatasetDto> dsAlumnoPorBimestre,
                                           int bimestreSeleccionado,
                                           UserSession userSession,
                                           Firma firmaTutor,
                                           FirmaDirectoraYSello firmaDirSello,
                                           Path tempDir) throws IOException {

    String html = plantilla;

    // ================= CABECERA =================
    String nombreCompleto = (dsBase.getApellidos() + " " + dsBase.getNombres()).toUpperCase(Locale.ROOT);

    html = html.replace("{{ALUMNO}}", escapeHtml(nombreCompleto));
    html = html.replace("{{DNI}}", dsBase.getDni() == null ? "" : dsBase.getDni());
    html = html.replace("{{GRADO_SECCION}}", (dsBase.getGrado() + "-" + dsBase.getSeccion()).trim());
    html = html.replace("{{NIVEL}}", dsBase.getNivel() != null ? dsBase.getNivel().toUpperCase(Locale.ROOT) : "");
    html = html.replace("{{ANIO}}", String.valueOf(dsBase.getAnio() == 0 ? Year.now().getValue() : dsBase.getAnio()));
    html = html.replace("{{NUM_ORDEN}}", dsBase.getNumeroOrden() != null ? dsBase.getNumeroOrden().toString() : "0");

    // ================= CONDUCTA (por bimestre y promedio) =================
    Integer[] conductaNotas = new Integer[4];
    String[] conductaLetras = new String[4];

    for (int b = 1; b <= 4; b++) {
      BoletaAlumnoDatasetDto ds = dsAlumnoPorBimestre.get(b);
      if (ds == null) continue;
      conductaNotas[b - 1] = ds.getConductaNota();
      conductaLetras[b - 1] = ds.getConductaLetra();
    }

    int sumaCond = 0;
    int conteoCond = 0;
    String ultimaLetra = "";
    for (int b = 1; b <= bimestreSeleccionado; b++) {
      Integer nota = conductaNotas[b - 1];
      if (nota != null && nota > 0) {
        sumaCond += nota;
        conteoCond++;
      }
      if (conductaLetras[b - 1] != null && !conductaLetras[b - 1].isEmpty()) {
        ultimaLetra = conductaLetras[b - 1];
      }
    }
    Integer promConducta = (conteoCond > 0) ? Math.round(sumaCond * 1f / conteoCond) : null;

    html = html.replace("{{CN-I}}", safeIntForBimestre(conductaNotas[0], 1, bimestreSeleccionado));
    html = html.replace("{{CL-I}}", safeStrForBimestre(conductaLetras[0], 1, bimestreSeleccionado));

    html = html.replace("{{CN-II}}", safeIntForBimestre(conductaNotas[1], 2, bimestreSeleccionado));
    html = html.replace("{{CL-II}}", safeStrForBimestre(conductaLetras[1], 2, bimestreSeleccionado));

    html = html.replace("{{CN-III}}", safeIntForBimestre(conductaNotas[2], 3, bimestreSeleccionado));
    html = html.replace("{{CL-III}}", safeStrForBimestre(conductaLetras[2], 3, bimestreSeleccionado));

    html = html.replace("{{CN-IV}}", safeIntForBimestre(conductaNotas[3], 4, bimestreSeleccionado));
    html = html.replace("{{CL-IV}}", safeStrForBimestre(conductaLetras[3], 4, bimestreSeleccionado));

    html = html.replace("{{CNT}}", promConducta != null ? String.valueOf(promConducta) : "");
    html = html.replace("{{CLT}}", ultimaLetra != null ? escapeHtml(ultimaLetra) : "");

    // ================= EVALUACIÓN DE PADRES =================
    String[] utiles = new String[4];
    String[] actividades = new String[4];
    String[] reuniones = new String[4];
    String[] escuela = new String[4];

    for (int b = 1; b <= 4; b++) {
      BoletaAlumnoDatasetDto ds = dsAlumnoPorBimestre.get(b);
      if (ds == null) continue;
      utiles[b - 1] = ds.getUtiles();
      actividades[b - 1] = ds.getParticipacion();
      reuniones[b - 1] = ds.getReuniones();
      escuela[b - 1] = ds.getEscuelaPadres();
    }

    html = html.replace("{{E1-I}}", safeStrForBimestre(utiles[0], 1, bimestreSeleccionado));
    html = html.replace("{{E2-I}}", safeStrForBimestre(actividades[0], 1, bimestreSeleccionado));
    html = html.replace("{{E3-I}}", safeStrForBimestre(reuniones[0], 1, bimestreSeleccionado));
    html = html.replace("{{E4-I}}", safeStrForBimestre(escuela[0], 1, bimestreSeleccionado));

    html = html.replace("{{E1-II}}", safeStrForBimestre(utiles[1], 2, bimestreSeleccionado));
    html = html.replace("{{E2-II}}", safeStrForBimestre(actividades[1], 2, bimestreSeleccionado));
    html = html.replace("{{E3-II}}", safeStrForBimestre(reuniones[1], 2, bimestreSeleccionado));
    html = html.replace("{{E4-II}}", safeStrForBimestre(escuela[1], 2, bimestreSeleccionado));

    html = html.replace("{{E1-III}}", safeStrForBimestre(utiles[2], 3, bimestreSeleccionado));
    html = html.replace("{{E2-III}}", safeStrForBimestre(actividades[2], 3, bimestreSeleccionado));
    html = html.replace("{{E3-III}}", safeStrForBimestre(reuniones[2], 3, bimestreSeleccionado));
    html = html.replace("{{E4-III}}", safeStrForBimestre(escuela[2], 3, bimestreSeleccionado));

    html = html.replace("{{E1-IV}}", safeStrForBimestre(utiles[3], 4, bimestreSeleccionado));
    html = html.replace("{{E2-IV}}", safeStrForBimestre(actividades[3], 4, bimestreSeleccionado));
    html = html.replace("{{E3-IV}}", safeStrForBimestre(reuniones[3], 4, bimestreSeleccionado));
    html = html.replace("{{E4-IV}}", safeStrForBimestre(escuela[3], 4, bimestreSeleccionado));

    // ================= RECOMENDACIONES =================
    String[] recomendaciones = new String[4];

    for (int b = 1; b <= 4; b++) {
      BoletaAlumnoDatasetDto ds = dsAlumnoPorBimestre.get(b);
      if (ds == null) continue;
      recomendaciones[b - 1] = ds.getRecomendacion();
    }

    html = html.replace("{{RECOMENDACION-I}}", safeStrForBimestre(recomendaciones[0], 1, bimestreSeleccionado));
    html = html.replace("{{RECOMENDACION-II}}", safeStrForBimestre(recomendaciones[1], 2, bimestreSeleccionado));
    html = html.replace("{{RECOMENDACION-III}}", safeStrForBimestre(recomendaciones[2], 3, bimestreSeleccionado));
    html = html.replace("{{RECOMENDACION-IV}}", safeStrForBimestre(recomendaciones[3], 4, bimestreSeleccionado));

    // ================= PUNTAJE Y ORDEN DE MÉRITO =================
    Double[] puntajes = new Double[4];
    Integer[] puestos = new Integer[4];

    for (int b = 1; b <= 4; b++) {
      BoletaAlumnoDatasetDto ds = dsAlumnoPorBimestre.get(b);
      if (ds == null) continue;
      if (ds.getPuntajeTotal() != null) {
        puntajes[b - 1] = ds.getPuntajeTotal();
      }
      if (ds.getPuesto() != null) {
        puestos[b - 1] = ds.getPuesto();
      }
    }

    double sumaPuntajes = 0d;
    int conteoPuntajes = 0;
    for (int b = 1; b <= bimestreSeleccionado; b++) {
      Double p = puntajes[b - 1];
      if (p != null && p > 0) {
        sumaPuntajes += p;
        conteoPuntajes++;
      }
    }

    String p1 = safeDoubleForBimestre(puntajes[0], 1, bimestreSeleccionado);
    String p2 = safeDoubleForBimestre(puntajes[1], 2, bimestreSeleccionado);
    String p3 = safeDoubleForBimestre(puntajes[2], 3, bimestreSeleccionado);
    String p4 = safeDoubleForBimestre(puntajes[3], 4, bimestreSeleccionado);

    html = html.replace("{{PUNTAJE-I}}", p1);
    html = html.replace("{{PUNTAJE-II}}", p2);
    html = html.replace("{{PUNTAJE-III}}", p3);
    html = html.replace("{{PUNTAJE-IV}}", p4);

    String puntajeTotalStr = conteoPuntajes > 0 ? String.valueOf(Math.round(sumaPuntajes)) : "";
    html = html.replace("{{PUNTAJE-TOTAL}}", puntajeTotalStr);

    // Orden de mérito
    html = html.replace("{{MERITO-I}}",  safeMerito(puestos[0], 1, bimestreSeleccionado));
    html = html.replace("{{MERITO-II}}", safeMerito(puestos[1], 2, bimestreSeleccionado));
    html = html.replace("{{MERITO-III}}", safeMerito(puestos[2], 3, bimestreSeleccionado));
    html = html.replace("{{MERITO-IV}}", safeMerito(puestos[3], 4, bimestreSeleccionado));

    // ================= FIRMAS Y SELLO =================
    if (firmaTutor != null && firmaTutor.getImagen() != null) {
      String fileName = "firma-tutor.png";
      Files.write(tempDir.resolve(fileName), ensurePngFormat(firmaTutor.getImagen()));
      html = html.replace("{{FIRMA_TUTOR}}", fileName);
    } else html = html.replace("{{FIRMA_TUTOR}}", "");

    if (firmaDirSello.firmaDirectora != null) {
      String fileName = "firma-directora.png";
      Files.write(tempDir.resolve(fileName), ensurePngFormat(firmaDirSello.firmaDirectora));
      html = html.replace("{{FIRMA_DIRECTORA}}", fileName);
    } else html = html.replace("{{FIRMA_DIRECTORA}}", "");

    if (firmaDirSello.selloInstitucion != null) {
      String fileName = "sello-institucion.png";
      Files.write(tempDir.resolve(fileName), ensurePngFormat(firmaDirSello.selloInstitucion));
      html = html.replace("{{SELLO_INSTITUCION}}", fileName);
    } else html = html.replace("{{SELLO_INSTITUCION}}", "");

    // =======================================
    // (OPCIONAL) Tabla dinámica de áreas/cursos
    // Cuando cambies la plantilla, puedes hacer algo así:
    //
    // String tablaDinamica = construirTablaAreasYCursos(dsAlumnoPorBimestre, bimestreSeleccionado);
    // html = html.replace("{{TABLA_AREAS_DINAMICA}}", tablaDinamica);
    //
    // De momento no lo activo para no romper tu tabla estática actual.
    // =======================================

    return html;
  }

  // Helpers para mostrar/ocultar valores según el bimestre seleccionado
  private String safeIntForBimestre(Integer valor, int bimestre, int bimestreSeleccionado) {
    if (bimestre > bimestreSeleccionado) return "";
    return (valor != null && valor > 0) ? String.valueOf(valor) : "";
  }

  private String safeStrForBimestre(String valor, int bimestre, int bimestreSeleccionado) {
    if (bimestre > bimestreSeleccionado) return "";
    return valor != null ? escapeHtml(valor) : "";
  }

  private String safeDoubleForBimestre(Double valor, int bimestre, int bimestreSeleccionado) {
    if (bimestre > bimestreSeleccionado) return "";
    if (valor == null) return "";
    return String.valueOf(Math.round(valor));
  }

  private String safeMerito(Integer valor, int bimestre, int bimestreSeleccionado) {
    String v = safeIntForBimestre(valor, bimestre, bimestreSeleccionado);
    return v.isEmpty() ? "" : v + "º";
  }


  private String escapeHtml(String s) {
    return s == null ? "" :
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
  }

  // --------------------------------------------------------
  // HTML → PDF
  // --------------------------------------------------------
  private byte[] htmlToPdf(String html, Path baseDir) throws IOException {

    Path debugHtml = baseDir.resolve("boleta-debug.html");
    Files.writeString(debugHtml, html, StandardCharsets.UTF_8);

    html = html.replace("&nbsp;", "&#160;");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      String baseUri = baseDir.toUri().toString();
      if (!baseUri.endsWith("/")) baseUri += "/";

      builder.withHtmlContent(html, baseUri);
      builder.toStream(baos);
      builder.run();

    } catch (Exception ex) {
      throw new IOException("Error al renderizar PDF", ex);
    }

    return baos.toByteArray();
  }

  // --------------------------------------------------------
  // UTILS
  // --------------------------------------------------------
  private String formatearNombreArchivoAlumno(Alumno a) {
    String base = (a.getApellidos() + "_" + a.getNombres())
        .replace(" ", "_")
        .replaceAll("[^A-Za-z0-9_ÁÉÍÓÚÑáéíóú]", "");
    return base.isEmpty() ? "alumno" : base;
  }

  private byte[] ensurePngFormat(byte[] originalBytes) throws IOException {
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(originalBytes));
    if (img == null) throw new IOException("Bytes no representan una imagen válida.");

    BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
    rgb.getGraphics().drawImage(img, 0, 0, java.awt.Color.WHITE, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(rgb, "png", baos);
    return baos.toByteArray();
  }

  // --------------------------------------------------------
  // (OPCIONAL) Tabla dinámica de áreas/cursos
  // --------------------------------------------------------
  // private String construirTablaAreasYCursos(Map<Integer, BoletaAlumnoDatasetDto> dsAlumnoPorBimestre,
  //                                           int bimestreSeleccionado) {
  //   // Aquí podrías construir un <table> en HTML usando dsAlumnoPorBimestre.get(b).getCursos()
  //   // y dsAlumnoPorBimestre.get(b).getAreas(), combinando por nombre de curso/área
  //   // y calculando promedios anuales de área.
  //   //
  //   // Lo dejo como plantilla para que lo adaptes según cómo vengan exactamente tus datos.
  //   return "";
  // }
}
