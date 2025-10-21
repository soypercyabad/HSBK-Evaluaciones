package pe.iep.hsbk.evaluaciones.model;

public class CursoArea {
  private Long cursoId;
  private Long areaId;

  public CursoArea() {
  }

  public CursoArea(Long cursoId, Long areaId) {
    this.cursoId = cursoId;
    this.areaId = areaId;
  }

  public Long getCursoId() {
    return cursoId;
  }

  public void setCursoId(Long cursoId) {
    this.cursoId = cursoId;
  }

  public Long getAreaId() {
    return areaId;
  }

  public void setAreaId(Long areaId) {
    this.areaId = areaId;
  }

  @Override
  public String toString() {
    return "CursoArea{cursoId=" + cursoId + ", areaId=" + areaId + "}";
  }

  @Override
  public int hashCode() {
    return (cursoId == null ? 0 : cursoId.hashCode()) * 31 + (areaId == null ? 0 : areaId.hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CursoArea)) return false;
    CursoArea other = (CursoArea) o;
    return (cursoId != null && cursoId.equals(other.cursoId)) &&
        (areaId != null && areaId.equals(other.areaId));
  }
}
