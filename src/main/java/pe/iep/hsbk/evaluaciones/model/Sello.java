package pe.iep.hsbk.evaluaciones.model;

public class Sello {
    private Long id;
    private String nombre;
    private byte[] sello;
    private boolean activo;

    public Sello() {}

    public Sello(Long id, String nombre, byte[] sello, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.sello = sello;
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

    public byte[] getSello() {
        return sello;
    }

    public void setSello(byte[] sello) {
        this.sello = sello;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
