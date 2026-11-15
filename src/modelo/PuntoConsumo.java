package modelo;

import java.io.Serializable;

public abstract class PuntoConsumo implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int idHogar;
    private String nombre;
    private String ubicacion;
    private double frecuenciaDiaria;
    private boolean activo;

    protected PuntoConsumo(int id, int idHogar, String nombre, String ubicacion,
                           double frecuenciaDiaria, boolean activo) {
        this.id = id;
        this.idHogar = idHogar;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.frecuenciaDiaria = frecuenciaDiaria;
        this.activo = activo;
    }

    public int getId() { return id; }
    public int getIdHogar() { return idHogar; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public double getFrecuenciaDiaria() { return frecuenciaDiaria; }
    public void setFrecuenciaDiaria(double frecuenciaDiaria) { this.frecuenciaDiaria = frecuenciaDiaria; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public abstract String getTipo();
    public abstract double calcularConsumoDiarioLitros();

    public double calcularConsumoMensualM3() {
        return (calcularConsumoDiarioLitros() * 30.0) / 1000.0;
    }
}