package pe.iep.hsbk.evaluaciones.dto;

import java.time.LocalDateTime;

public class RecomendacionAlumnoDto {
    private Long id;
    private Long matriculaId;
    private Long bimestreId;
    private Long recomendacionId;
    private String mensajeCatalogo;   // rc.mensaje
    private String mensajePersonal;   // ra.mensajePersonal (puede ser null)
    private Long usuarioId;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
    }

    public Long getBimestreId() {
        return bimestreId;
    }

    public void setBimestreId(Long bimestreId) {
        this.bimestreId = bimestreId;
    }

    public Long getRecomendacionId() {
        return recomendacionId;
    }

    public void setRecomendacionId(Long recomendacionId) {
        this.recomendacionId = recomendacionId;
    }

    public String getMensajeCatalogo() {
        return mensajeCatalogo;
    }

    public void setMensajeCatalogo(String mensajeCatalogo) {
        this.mensajeCatalogo = mensajeCatalogo;
    }

    public String getMensajePersonal() {
        return mensajePersonal;
    }

    public void setMensajePersonal(String mensajePersonal) {
        this.mensajePersonal = mensajePersonal;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
