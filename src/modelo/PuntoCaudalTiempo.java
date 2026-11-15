package modelo;

public class PuntoCaudalTiempo extends PuntoConsumo {
    private static final long serialVersionUID = 1L;
    private String tipoArtefacto; // Ducha, Grifo, Manguera
    private double caudalLitrosMinuto;
    private double minutosPorUso;

    public PuntoCaudalTiempo(int id, int idHogar, String tipoArtefacto, String nombre, String ubicacion,
                             double frecuenciaDiaria, boolean activo, double caudalLitrosMinuto, double minutosPorUso) {
        super(id, idHogar, nombre, ubicacion, frecuenciaDiaria, activo);
        this.tipoArtefacto = tipoArtefacto;
        this.caudalLitrosMinuto = caudalLitrosMinuto;
        this.minutosPorUso = minutosPorUso;
    }

    public double getCaudalLitrosMinuto() { return caudalLitrosMinuto; }
    public void setCaudalLitrosMinuto(double caudalLitrosMinuto) { this.caudalLitrosMinuto = caudalLitrosMinuto; }
    public double getMinutosPorUso() { return minutosPorUso; }
    public void setMinutosPorUso(double minutosPorUso) { this.minutosPorUso = minutosPorUso; }

    @Override
    public String getTipo() { return tipoArtefacto; }

    @Override
    public double calcularConsumoDiarioLitros() {
        return caudalLitrosMinuto * minutosPorUso * getFrecuenciaDiaria();
    }
}