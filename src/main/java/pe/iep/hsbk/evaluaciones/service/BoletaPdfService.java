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
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    // 1) Obtener dataset del SP
    // ==========================
    List<Long> alumnosIds = alumnos.stream()
        .map(Alumno::getMatriculaId)
        .collect(Collectors.toList());

    Map<Long, BoletaAlumnoDatasetDto> datasetPorMatricula =
        boletaDatasetDao.obtenerDatasetBoleta(
            periodoId,
            seccionId,
            bimestreNum,
            alumnosIds
        );

    // Solo 1 alumno → PDF directo
    if (alumnos.size() == 1) {
      Alumno a = alumnos.get(0);
      Long matId = a.getMatriculaId();
      BoletaAlumnoDatasetDto ds = datasetPorMatricula.get(matId);

      if (ds == null) {
        throw new IllegalStateException("No se encontró dataset para la matrícula " + matId);
      }

      byte[] pdf = generarPdfParaAlumno(
          a,
          ds,
          userSession,
          plantillaHtml,
          firmaTutor,
          firmaDirSello
      );

      try (FileOutputStream fos = new FileOutputStream(destino)) {
        fos.write(pdf);
      }
      return;
    }

    // Varios alumnos → ZIP
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destino))) {
      for (Alumno a : alumnos) {
        Long matId = a.getMatriculaId();
        BoletaAlumnoDatasetDto ds = datasetPorMatricula.get(matId);
        if (ds == null) {
          // si no hay dataset para ese alumno, lo saltas o lanzas excepción, como prefieras
          continue;
        }

        byte[] pdf = generarPdfParaAlumno(
            a,
            ds,
            userSession,
            plantillaHtml,
            firmaTutor,
            firmaDirSello
        );

        ZipEntry entry = new ZipEntry(formatearNombreArchivoAlumno(a) + ".pdf");
        zos.putNextEntry(entry);
        zos.write(pdf);
        zos.closeEntry();
      }
    }
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
  // GENERAR PDF POR ALUMNO
  // --------------------------------------------------------
  private byte[] generarPdfParaAlumno(Alumno alumno,
                                      BoletaAlumnoDatasetDto ds,
                                      UserSession userSession,
                                      String plantillaHtml,
                                      Firma firmaTutor,
                                      FirmaDirectoraYSello firmaDirSello) throws Exception {

    Path tempDir = Files.createTempDirectory("boleta-pdf-" + ds.getMatriculaId() + "-");

    try {
      String htmlPersonalizado = renderizarHtmlBoleta(
          plantillaHtml,
          alumno,
          ds,
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
            } catch (IOException ignored) {}
          });

    } catch (Exception ignored) {}
  }

  // --------------------------------------------------------
  // RENDER HTML usando el dataset del SP
  // --------------------------------------------------------
  private String renderizarHtmlBoleta(String plantilla,
                                      Alumno alumno,
                                      BoletaAlumnoDatasetDto ds,
                                      UserSession userSession,
                                      Firma firmaTutor,
                                      FirmaDirectoraYSello firmaDirSello,
                                      Path tempDir) throws IOException {

    String html = plantilla;

    // Cabecera desde dataset (por si algún día cambias algo en el SP)
    String nombreCompleto = (ds.getApellidos() + " " + ds.getNombres()).toUpperCase(Locale.ROOT);

    html = html.replace("{{ALUMNO}}", escapeHtml(nombreCompleto));
    html = html.replace("{{DNI}}", ds.getDni() == null ? "" : ds.getDni());
    html = html.replace("{{GRADO_SECCION}}",
        (ds.getGrado() + "-" + ds.getSeccion()).trim());
    html = html.replace("{{NIVEL}}",
        ds.getNivel() != null ? ds.getNivel().toUpperCase(Locale.ROOT) : "");
    html = html.replace("{{ANIO}}", String.valueOf(ds.getAnio() == 0 ? Year.now().getValue() : ds.getAnio()));
    html = html.replace("{{NUM_ORDEN}}",
        ds.getNumeroOrden() != null ? ds.getNumeroOrden().toString() : "0");

    // Si tu plantilla tiene más placeholders (PUNTAJE, PUESTO, CONDUCTA, etc.), puedes hacer:
    // html = html.replace("{{PUNTAJE_TOTAL}}", ds.getPuntajeTotal() != null ? ds.getPuntajeTotal().toString() : "");
    // html = html.replace("{{PUESTO}}", ds.getPuesto() != null ? ds.getPuesto().toString() : "");
    // html = html.replace("{{CONDUCTA_LETRA}}", ds.getConductaLetra() != null ? ds.getConductaLetra() : "");

    // Firma Tutor
    if (firmaTutor != null && firmaTutor.getImagen() != null) {
      String fileName = "firma-tutor.png";
      Files.write(tempDir.resolve(fileName), ensurePngFormat(firmaTutor.getImagen()));
      html = html.replace("{{FIRMA_TUTOR}}", fileName);
    } else html = html.replace("{{FIRMA_TUTOR}}", "");

    // Firma Directora
    if (firmaDirSello.firmaDirectora != null) {
      String fileName = "firma-directora.png";
      Files.write(tempDir.resolve(fileName), ensurePngFormat(firmaDirSello.firmaDirectora));
      html = html.replace("{{FIRMA_DIRECTORA}}", fileName);
    } else html = html.replace("{{FIRMA_DIRECTORA}}", "");

    // Sello
    if (firmaDirSello.selloInstitucion != null) {
      String fileName = "sello-institucion.png";
      Files.write(tempDir.resolve(fileName), ensurePngFormat(firmaDirSello.selloInstitucion));
      html = html.replace("{{SELLO_INSTITUCION}}", fileName);
    } else html = html.replace("{{SELLO_INSTITUCION}}", "");

    return html;
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
}
