package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LecturaMedidor implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int idHogar;
    private LocalDateTime fechaHora;
    private double acumuladoM3;
    private String observacion;

    public LecturaMedidor(int id, int idHogar, LocalDateTime fechaHora, double acumuladoM3, String observacion) {
        this.id = id;
        this.idHogar = idHogar;
        this.fechaHora = fechaHora;
        this.acumuladoM3 = acumuladoM3;
        this.observacion = observacion;
    }

    public int getId() { return id; }
    public int getIdHogar() { return idHogar; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public double getAcumuladoM3() { return acumuladoM3; }
    public void setAcumuladoM3(double acumuladoM3) { this.acumuladoM3 = acumuladoM3; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getFechaHoraFormateada() {
        return fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "ID %d | %s | %.3f m³", id, getFechaHoraFormateada(), acumuladoM3);
    }
}