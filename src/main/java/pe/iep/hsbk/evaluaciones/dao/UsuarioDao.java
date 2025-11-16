package pe.iep.hsbk.evaluaciones.dao;

import pe.iep.hsbk.evaluaciones.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
  Optional<Usuario> findByUsername(String username) throws  Exception;
  List<String> findRolesByUserId(long userId) throws Exception;

}
