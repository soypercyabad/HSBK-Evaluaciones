package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.util.List;

public interface FirmaDao {
  List<Firma> getListFirmas() throws Exception;
  List<Usuario> getUsuariosFirmas() throws Exception;
  void guardarFirma(Firma f) throws Exception;
  void actualizarFirma(Firma f) throws Exception;           // si editas imagen
  // Obtener firma por usuario id
  Firma getFirmaPorUsuarioId(long usuarioId) throws Exception;
  // Obtener firma de director activo
  Firma getFirmaDirectorActiva() throws Exception;
}
