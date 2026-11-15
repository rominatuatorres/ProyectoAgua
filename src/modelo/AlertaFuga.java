package modelo;

import java.io.Serializable;

public class AlertaFuga implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int idHogar;
    private int idPrueba;
    private String estado; // Pendiente, En revisión, Cerrada
    private String motivo;
    private String ubicacionSospechada;
    private String accionRealizada;

    public AlertaFuga(int id, int idHogar, int idPrueba, String motivo) {
        this.id = id;
        this.idHogar = idHogar;
        this.idPrueba = idPrueba;
        this.estado = "Pendiente";
        this.motivo = motivo;
        this.ubicacionSospechada = "";
        this.accionRealizada = "";
    }

    public int getId() { return id; }
    public int getIdHogar() { return idHogar; }
    public int getIdPrueba() { return idPrueba; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getUbicacionSospechada() { return ubicacionSospechada; }
    public void setUbicacionSospechada(String ubicacionSospechada) { this.ubicacionSospechada = ubicacionSospechada; }
    public String getAccionRealizada() { return accionRealizada; }
    public void setAccionRealizada(String accionRealizada) { this.accionRealizada = accionRealizada; }
}