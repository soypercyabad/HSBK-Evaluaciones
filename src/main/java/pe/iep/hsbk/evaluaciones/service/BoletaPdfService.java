package pe.iep.hsbk.evaluaciones.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BoletaPdfService {

  private final PlantillaBoletaDao plantillaBoletaDao;
  private final FirmaDao firmaDao;
  private final SelloDao selloDao;

  public BoletaPdfService(PlantillaBoletaDao plantillaBoletaDao,
                          FirmaDao firmaDao,
                          SelloDao selloDao) {
    this.plantillaBoletaDao = plantillaBoletaDao;
    this.firmaDao = firmaDao;
    this.selloDao = selloDao;
  }

  /**
   * Genera boletas en PDF para uno o varios alumnos.
   * Si hay 1 alumno => genera un solo PDF.
   * Si hay varios  => genera un ZIP con un PDF por alumno.
   */
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

    // 1) Plantilla activa de boleta
    PlantillaBoleta plantilla = plantillaBoletaDao.obtenerPlantillaActiva();
    if (plantilla == null || plantilla.getContenidoHtml() == null) {
      throw new IllegalStateException("No hay plantilla de boleta activa.");
    }
    String plantillaHtml = plantilla.getContenidoHtml();

    // 2) Firma tutor (usuario logueado que genera la boleta)
    Firma firmaTutor = firmaDao.getFirmaPorUsuarioId(userSession.getUserId());

    // 3) Firma directora + sello institución
    FirmaDirectoraYSello firmaDirSello = cargarFirmaDirectoraYSello();

    // 4) Procesar uno o varios alumnos
    if (alumnos.size() == 1) {
      Alumno a = alumnos.get(0);

      byte[] pdf = generarPdfParaAlumno(
          periodoId,
          seccionId,
          nivelId,
          bimestreNum,
          a,
          userSession,
          plantillaHtml,
          firmaTutor,
          firmaDirSello
      );

      try (FileOutputStream fos = new FileOutputStream(destino)) {
        fos.write(pdf);
      }

    } else {
      try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destino))) {
        for (Alumno a : alumnos) {
          byte[] pdf = generarPdfParaAlumno(
              periodoId,
              seccionId,
              nivelId,
              bimestreNum,
              a,
              userSession,
              plantillaHtml,
              firmaTutor,
              firmaDirSello
          );

          String nombreEntry = formatearNombreArchivoAlumno(a) + ".pdf";
          ZipEntry entry = new ZipEntry(nombreEntry);
          zos.putNextEntry(entry);
          zos.write(pdf);
          zos.closeEntry();
        }
      }
    }
  }

  // ================== Firma directora + sello ==================

  private static class FirmaDirectoraYSello {
    byte[] firmaDirectora;
    String nombreDirectora;
    byte[] selloInstitucion;
    String nombreInstitucion;
  }

  private FirmaDirectoraYSello cargarFirmaDirectoraYSello() throws Exception {
    FirmaDirectoraYSello dto = new FirmaDirectoraYSello();

    // Firma directora activa
    Firma firmaDir = firmaDao.getFirmaDirectorActiva();
    if (firmaDir != null) {
      dto.firmaDirectora = firmaDir.getImagen();
    }

    // Sello institución activo
    Sello sello = selloDao.obtenerSelloActivo();
    if (sello != null) {
      dto.selloInstitucion = sello.getSello();
    }

    return dto;
  }

  // ================== PDF por alumno ==================

  private byte[] generarPdfParaAlumno(long periodoId,
                                      long seccionId,
                                      long nivelId,
                                      int bimestreNum,
                                      Alumno alumno,
                                      UserSession userSession,
                                      String plantillaHtml,
                                      Firma firmaTutor,
                                      FirmaDirectoraYSello firmaDirSello) throws Exception {

    BoletaAlumnoDatasetDto ds = construirDatasetBasico(periodoId, seccionId, nivelId, bimestreNum, alumno);

    // === 1) Crear carpeta temporal para este alumno (imágenes + debug html) ===
    Path tempDir = Files.createTempDirectory("boleta-pdf-" + alumno.getId() + "-");

    // === 2) Renderizar HTML reemplazando placeholders por NOMBRES DE ARCHIVO ===
    String htmlPersonalizado = renderizarHtmlBoleta(
        plantillaHtml,
        alumno,
        userSession,
        ds,
        firmaTutor,
        firmaDirSello,
        tempDir
    );

    // === 3) Generar el PDF usando esa carpeta como baseUri ===
    return htmlToPdf(htmlPersonalizado, tempDir);
  }

  /**
   * Por ahora devolvemos datos "mock" mientras conectas el SP real.
   */
  private BoletaAlumnoDatasetDto construirDatasetBasico(long periodoId,
                                                        long seccionId,
                                                        long nivelId,
                                                        int bimestreNum,
                                                        Alumno alumno) {

    BoletaAlumnoDatasetDto ds = new BoletaAlumnoDatasetDto();

    // Luego estos campos los vas a rellenar desde la BD.
    ds.setNivelNombre(nivelId == 1L ? "PRIMARIA" : "SECUNDARIA");
    ds.setAnio(Year.now().getValue());

    return ds;
  }

  // ================== Rellenar HTML ==================

  private String renderizarHtmlBoleta(String plantilla,
                                      Alumno alumno,
                                      UserSession userSession,
                                      BoletaAlumnoDatasetDto ds,
                                      Firma firmaTutor,
                                      FirmaDirectoraYSello firmaDirSello,
                                      Path tempDir) throws IOException {

    String html = plantilla;

    String apeNom = (alumno.getApellidos() + " " + alumno.getNombres()).trim();
    html = html.replace("{{ALUMNO}}", escapeHtml(apeNom));
    html = html.replace("{{DNI}}", alumno.getDni() == null ? "" : alumno.getDni());

    if (ds != null) {
      String gradoSeccion =
          (alumno.getGrado() != null ? alumno.getGrado() : "") + "-" +
              (alumno.getSeccion() != null ? alumno.getSeccion() : "");
      html = html.replace("{{GRADO_SECCION}}", gradoSeccion.trim());
      html = html.replace("{{NIVEL}}", alumno.getNivel() != null ? alumno.getNivel() : "");
      html = html.replace("{{ANIO}}", String.valueOf(ds.getAnio()));
      html = html.replace("{{NUM_ORDEN}}", alumno.getNumeroOrden() != null ? alumno.getNumeroOrden().toString() : "0");
    } else {
      html = html.replace("{{GRADO_SECCION}}", "");
      html = html.replace("{{NIVEL}}", "");
      html = html.replace("{{ANIO}}", "");
      html = html.replace("{{NUM_ORDEN}}", "0");
    }

    // ========= Firmas y sello =========

    // Firma tutor
    if (firmaTutor != null && firmaTutor.getImagen() != null) {
      String fileName = "firma-tutor.png";
      Path imgPath = tempDir.resolve(fileName);

      byte[] pngBytes = ensurePngFormat(firmaTutor.getImagen());
      Files.write(imgPath, pngBytes);

      System.out.println("Firma tutor BD bytes: " + firmaTutor.getImagen().length +
          ", PNG final: " + pngBytes.length);

      html = html.replace("{{FIRMA_TUTOR}}", fileName);
    } else {
      html = html.replace("{{FIRMA_TUTOR}}", "");
    }

    // Firma directora
    if (firmaDirSello != null && firmaDirSello.firmaDirectora != null) {
      String fileName = "firma-directora.png";
      Path imgPath = tempDir.resolve(fileName);

      byte[] pngBytes = ensurePngFormat(firmaDirSello.firmaDirectora);
      Files.write(imgPath, pngBytes);

      System.out.println("Firma directora BD bytes: " + firmaDirSello.firmaDirectora.length +
          ", PNG final: " + pngBytes.length);

      html = html.replace("{{FIRMA_DIRECTORA}}", fileName);
    } else {
      html = html.replace("{{FIRMA_DIRECTORA}}", "");
    }

    // Sello institución
    if (firmaDirSello != null && firmaDirSello.selloInstitucion != null) {
      String fileName = "sello-institucion.png";
      Path imgPath = tempDir.resolve(fileName);

      byte[] pngBytes = ensurePngFormat(firmaDirSello.selloInstitucion);
      Files.write(imgPath, pngBytes);

      System.out.println("Sello BD bytes: " + firmaDirSello.selloInstitucion.length +
          ", PNG final: " + pngBytes.length);

      html = html.replace("{{SELLO_INSTITUCION}}", fileName);
    } else {
      html = html.replace("{{SELLO_INSTITUCION}}", "");
    }

    return html;
  }

  private String escapeHtml(String s) {
    if (s == null) return "";
    return s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

  // ================== HTML → PDF ==================
  private byte[] htmlToPdf(String html, Path baseDir) throws IOException {

    Path debugHtml = baseDir.resolve("boleta-debug.html");
    Files.writeString(debugHtml, html, StandardCharsets.UTF_8);

    html = html.replace("&nbsp;", "&#160;");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      PdfRendererBuilder builder = new PdfRendererBuilder();

      String baseUri = baseDir.toUri().toString();
      if (!baseUri.endsWith("/")) baseUri += "/";

      System.out.println("Base URI: " + baseUri);
      System.out.println("Existe firma tutor? " + Files.exists(baseDir.resolve("firma-tutor.png")));

      builder.withHtmlContent(html, baseUri);
      builder.toStream(baos);
      builder.run();

    } catch (Exception e) {
      throw new IOException("Error al renderizar PDF", e);
    }
    return baos.toByteArray();
  }

  private String formatearNombreArchivoAlumno(Alumno a) {
    String base = (a.getApellidos() + "_" + a.getNombres())
        .trim()
        .replace(" ", "_")
        .replaceAll("[^A-Za-z0-9_ÁÉÍÓÚÑáéíóú]", "");
    if (base.isEmpty()) base = "alumno";
    return base;
  }

  /**
   * Asegura que los bytes representen un PNG válido, sin importar el formato original
   * (JPG, BMP, GIF, etc.), siempre que ImageIO lo pueda leer.
   */
  private byte[] ensurePngFormat(byte[] originalBytes) throws IOException {
    if (originalBytes == null || originalBytes.length == 0) {
      throw new IOException("Imagen vacía.");
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes);
    BufferedImage img = ImageIO.read(bais);

    if (img == null) {
      throw new IOException("Los bytes no representan una imagen válida.");
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    return baos.toByteArray();
  }
}
