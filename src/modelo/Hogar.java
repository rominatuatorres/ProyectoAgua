package modelo;

import java.io.Serializable;

public class Hogar implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nombreResponsable;
    private String direccion;
    private String barrio;
    private String codigoMedidor;
    private int habitantes;

    public Hogar(int id, String nombreResponsable, String direccion, String barrio,
                 String codigoMedidor, int habitantes) {
        this.id = id;
        this.nombreResponsable = nombreResponsable;
        this.direccion = direccion;
        this.barrio = barrio;
        this.codigoMedidor = codigoMedidor;
        this.habitantes = habitantes;
    }

    public int getId() { return id; }
    public String getNombreResponsable() { return nombreResponsable; }
    public void setNombreResponsable(String nombreResponsable) { this.nombreResponsable = nombreResponsable; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }
    public String getCodigoMedidor() { return codigoMedidor; }
    public void setCodigoMedidor(String codigoMedidor) { this.codigoMedidor = codigoMedidor; }
    public int getHabitantes() { return habitantes; }
    public void setHabitantes(int habitantes) { this.habitantes = habitantes; }

    @Override
    public String toString() {
        return nombreResponsable + " | Medidor: " + codigoMedidor + " (" + habitantes + " hab.)";
    }
}