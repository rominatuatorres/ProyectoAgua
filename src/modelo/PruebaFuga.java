package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PruebaFuga implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int idHogar;
    private int idLecturaInicial;
    private int idLecturaFinal;
    private boolean sinUsoConfirmado;
    private boolean horarioNocturnoValido;
    private double umbralNocturnoLitrosHora;
    private double perdidaLitros;
    private double duracionHoras;
    private double tasaLitrosHora;
    private String resultado;
    private String observaciones;
    private LocalDateTime fechaEvaluacion;

    public PruebaFuga(int id, int idHogar, int idLecturaInicial, int idLecturaFinal,
                      boolean sinUsoConfirmado, boolean horarioNocturnoValido,
                      double umbralNocturnoLitrosHora, String observaciones) {
        this.id = id;
        this.idHogar = idHogar;
        this.idLecturaInicial = idLecturaInicial;
        this.idLecturaFinal = idLecturaFinal;
        this.sinUsoConfirmado = sinUsoConfirmado;
        this.horarioNocturnoValido = horarioNocturnoValido;
        this.umbralNocturnoLitrosHora = umbralNocturnoLitrosHora;
        this.observaciones = observaciones;
        this.fechaEvaluacion = LocalDateTime.now();
    }

    public int getId() { return id; }
    public int getIdHogar() { return idHogar; }
    public int getIdLecturaInicial() { return idLecturaInicial; }
    public int getIdLecturaFinal() { return idLecturaFinal; }
    public boolean isSinUsoConfirmado() { return sinUsoConfirmado; }
    public boolean isHorarioNocturnoValido() { return horarioNocturnoValido; }
    public double getUmbralNocturnoLitrosHora() { return umbralNocturnoLitrosHora; }
    public double getPerdidaLitros() { return perdidaLitros; }
    public void setPerdidaLitros(double perdidaLitros) { this.perdidaLitros = perdidaLitros; }
    public double getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(double duracionHoras) { this.duracionHoras = duracionHoras; }
    public double getTasaLitrosHora() { return tasaLitrosHora; }
    public void setTasaLitrosHora(double tasaLitrosHora) { this.tasaLitrosHora = tasaLitrosHora; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getObservaciones() { return observaciones; }
}