package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Firma;
import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.util.List;

public interface FirmaDao {
  List<Firma> getListFirmas() throws Exception;
  List<Usuario> getUsuariosFirmas() throws Exception;
  void guardarFirma(Firma f) throws Exception;
  void actualizarFirma(Firma f) throws Exception;           // si editas imagen
  void actualizarEstadoFirma(Long firmaId, boolean activo) throws Exception; // opcional
}
