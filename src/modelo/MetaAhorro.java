package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class MetaAhorro implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int idHogar;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private double baseLitrosPersonaDia;
    private double reduccionPorcentaje;
    private String descripcion;
    private String estado;

    public MetaAhorro(int id, int idHogar, LocalDate fechaInicio, LocalDate fechaFin,
                      double baseLitrosPersonaDia, double reduccionPorcentaje, String descripcion) {
        this.id = id;
        this.idHogar = idHogar;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.baseLitrosPersonaDia = baseLitrosPersonaDia;
        this.reduccionPorcentaje = reduccionPorcentaje;
        this.descripcion = descripcion;
        this.estado = "Pendiente";
    }

    public int getId() { return id; }
    public int getIdHogar() { return idHogar; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public double getBaseLitrosPersonaDia() { return baseLitrosPersonaDia; }
    public double getReduccionPorcentaje() { return reduccionPorcentaje; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double calcularObjetivoLitrosPersonaDia() {
        return baseLitrosPersonaDia * (1.0 - (reduccionPorcentaje / 100.0));
    }
}