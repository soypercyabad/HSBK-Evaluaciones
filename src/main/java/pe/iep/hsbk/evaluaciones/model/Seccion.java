package pe.iep.hsbk.evaluaciones.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Seccion {
  private Long id;
  private Long gradoId;
  private String nombre;

  public Seccion() {
  }

  public Seccion(Long id, Long gradoId, String nombre) {
    this.id = id;
    this.gradoId = gradoId;
    this.nombre = nombre;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getGradoId() {
    return gradoId;
  }

  public void setGradoId(Long gradoId) {
    this.gradoId = gradoId;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  @Override
  public String toString() {
    return nombre;
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Seccion)) return false;
    Seccion other = (Seccion) o;
    return id != null && id.equals(other.id);
  }

  private final Map<Long, List<Seccion>> cacheSeccionesPorGrado = new HashMap<>();
}
