package modelo;

public class PuntoPorCiclo extends PuntoConsumo {
    private static final long serialVersionUID = 1L;
    private String tipoArtefacto; // Inodoro, Lavadora
    private double litrosPorCiclo;

    public PuntoPorCiclo(int id, int idHogar, String tipoArtefacto, String nombre, String ubicacion,
                         double frecuenciaDiaria, boolean activo, double litrosPorCiclo) {
        super(id, idHogar, nombre, ubicacion, frecuenciaDiaria, activo);
        this.tipoArtefacto = tipoArtefacto;
        this.litrosPorCiclo = litrosPorCiclo;
    }

    public double getLitrosPorCiclo() { return litrosPorCiclo; }
    public void setLitrosPorCiclo(double litrosPorCiclo) { this.litrosPorCiclo = litrosPorCiclo; }

    @Override
    public String getTipo() { return tipoArtefacto; }

    @Override
    public double calcularConsumoDiarioLitros() {
        return litrosPorCiclo * getFrecuenciaDiaria();
    }
}
