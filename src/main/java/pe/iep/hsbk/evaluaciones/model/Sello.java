package pe.iep.hsbk.evaluaciones.model;

public class Sello {
    private Long id;
    private String nombre;
    private byte[] imagen;
    private boolean activo;

    public Sello() {}

    public Sello(Long id, String nombre, byte[] imagen, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.imagen = imagen;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
