package negocio;

import modelo.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class GestorSistemaAgua implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ARCHIVO_DATOS = "sistema_agua_quito.dat";

    private List<Hogar> hogares = new ArrayList<>();
    private List<PuntoConsumo> puntos = new ArrayList<>();
    private List<LecturaMedidor> lecturas = new ArrayList<>();
    private List<PruebaFuga> pruebas = new ArrayList<>();
    private List<AlertaFuga> alertas = new ArrayList<>();
    private List<MetaAhorro> metas = new ArrayList<>();

    private int seqHogar = 1, seqPunto = 1, seqLectura = 1, seqPrueba = 1, seqAlerta = 1, seqMeta = 1;
    private double referenciaOmsLitrosPersonaDia = 100.0; // RF2: 100 L/persona/día
    private double umbralNocturnoDefectoLH = 2.0;        // RF7: Umbral nocturno admisible (L/h)

    public GestorSistemaAgua() {
        cargarDatos();
    }

    @SuppressWarnings("unchecked")
    private void cargarDatos() {
        Path ruta = Paths.get(ARCHIVO_DATOS);
        if (!Files.exists(ruta)) return;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(ruta))) {
            GestorSistemaAgua cargado = (GestorSistemaAgua) ois.readObject();
            this.hogares = cargado.hogares;
            this.puntos = cargado.puntos;
            this.lecturas = cargado.lecturas;
            this.pruebas = cargado.pruebas;
            this.alertas = cargado.alertas;
            this.metas = cargado.metas;
            this.seqHogar = cargado.seqHogar;
            this.seqPunto = cargado.seqPunto;
            this.seqLectura = cargado.seqLectura;
            this.seqPrueba = cargado.seqPrueba;
            this.seqAlerta = cargado.seqAlerta;
            this.seqMeta = cargado.seqMeta;
            this.referenciaOmsLitrosPersonaDia = cargado.referenciaOmsLitrosPersonaDia;
            this.umbralNocturnoDefectoLH = cargado.umbralNocturnoDefectoLH;
        } catch (Exception e) {
            // Si el archivo está corrupto o vacío, inicia limpio sin datos precargados
        }
    }

    private void guardarDatos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(ARCHIVO_DATOS)))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar los datos: " + e.getMessage());
        }
    }

    // --- RF1: GESTIÓN DE VIVIENDAS ---
    public Hogar registrarHogar(String responsable, String direccion, String barrio, String medidor, int habitantes) {
        validarTexto(responsable, "El nombre del responsable es obligatorio.");
        validarTexto(direccion, "La dirección es obligatoria.");
        validarTexto(medidor, "El número de medidor es obligatorio.");
        if (habitantes < 1) throw new IllegalArgumentException("El número de habitantes debe ser al menos 1.");
        for (Hogar h : hogares) {
            if (h.getCodigoMedidor().equalsIgnoreCase(medidor.trim())) {
                throw new IllegalArgumentException("Ya existe una vivienda registrada con ese medidor.");
            }
        }
        Hogar nuevo = new Hogar(seqHogar++, responsable.trim(), direccion.trim(), barrio.trim(), medidor.trim(), habitantes);
        hogares.add(nuevo);
        guardarDatos();
        return nuevo;
    }

    public void actualizarHogar(int id, String responsable, String direccion, String barrio, String medidor, int habitantes) {
        Hogar h = buscarHogar(id);
        if (h == null) throw new IllegalArgumentException("Vivienda no encontrada.");
        validarTexto(responsable, "El nombre del responsable es obligatorio.");
        validarTexto(direccion, "La dirección es obligatoria.");
        validarTexto(medidor, "El número de medidor es obligatorio.");
        if (habitantes < 1) throw new IllegalArgumentException("El número de habitantes debe ser al menos 1.");
        for (Hogar otro : hogares) {
            if (otro.getId() != id && otro.getCodigoMedidor().equalsIgnoreCase(medidor.trim())) {
                throw new IllegalArgumentException("Ese número de medidor pertenece a otra vivienda.");
            }
        }
        h.setNombreResponsable(responsable.trim());
        h.setDireccion(direccion.trim());
        h.setBarrio(barrio.trim());
        h.setCodigoMedidor(medidor.trim());
        h.setHabitantes(habitantes);
        guardarDatos();
    }

    public void eliminarHogar(int id) {
        hogares.removeIf(h -> h.getId() == id);
        puntos.removeIf(p -> p.getIdHogar() == id);
        lecturas.removeIf(l -> l.getIdHogar() == id);
        pruebas.removeIf(p -> p.getIdHogar() == id);
        alertas.removeIf(a -> a.getIdHogar() == id);
        metas.removeIf(m -> m.getIdHogar() == id);
        guardarDatos();
    }

    public Hogar buscarHogar(int id) {
        return hogares.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
    }
    public List<Hogar> getHogares() { return Collections.unmodifiableList(hogares); }

    // --- RF3 Y RF4: PUNTOS DE CONSUMO Y PROYECCIONES ---
    public PuntoConsumo registrarPunto(int idHogar, String tipo, String nombre, String ubicacion,
                                       double frecuencia, double param1, double param2) {
        if (buscarHogar(idHogar) == null) throw new IllegalArgumentException("Seleccione una vivienda válida.");
        validarTexto(nombre, "El nombre del punto es obligatorio.");
        validarTexto(ubicacion, "La ubicación es obligatoria.");
        if (frecuencia <= 0 || param1 <= 0) {
            throw new IllegalArgumentException("La frecuencia y el caudal/volumen deben ser mayores a cero.");
        }
        PuntoConsumo p;
        if (tipo.equals("Ducha") || tipo.equals("Grifo") || tipo.equals("Manguera de jardín")) {
            if (param2 <= 0) throw new IllegalArgumentException("La duración media en minutos debe ser mayor a cero.");
            p = new PuntoCaudalTiempo(seqPunto++, idHogar, tipo, nombre.trim(), ubicacion.trim(), frecuencia, true, param1, param2);
        } else {
            p = new PuntoPorCiclo(seqPunto++, idHogar, tipo, nombre.trim(), ubicacion.trim(), frecuencia, true, param1);
        }
        puntos.add(p);
        guardarDatos();
        return p;
    }

    public void cambiarEstadoPunto(int idPunto) {
        for (PuntoConsumo p : puntos) {
            if (p.getId() == idPunto) {
                p.setActivo(!p.isActivo());
                guardarDatos();
                return;
            }
        }
    }

    public void eliminarPunto(int idPunto) {
        puntos.removeIf(p -> p.getId() == idPunto);
        guardarDatos();
    }

    public List<PuntoConsumo> getPuntosPorHogar(int idHogar) {
        List<PuntoConsumo> res = new ArrayList<>();
        for (PuntoConsumo p : puntos) if (p.getIdHogar() == idHogar) res.add(p);
        return res;
    }

    public double calcularConsumoTotalEstimadoMensualM3(int idHogar) {
        double total = 0;
        for (PuntoConsumo p : puntos) {
            if (p.getIdHogar() == idHogar && p.isActivo()) total += p.calcularConsumoMensualM3();
        }
        return total;
    }

    public double calcularPorcentajeImpactoPunto(PuntoConsumo punto) {
        if (!punto.isActivo()) return 0;
        double total = calcularConsumoTotalEstimadoMensualM3(punto.getIdHogar());
        return total == 0 ? 0 : (punto.calcularConsumoMensualM3() / total) * 100.0;
    }

    // --- RF5 Y RF6: LECTURAS DEL MEDIDOR ---
    public LecturaMedidor registrarLectura(int idHogar, LocalDateTime fechaHora, double acumuladoM3, String obs) {
        if (buscarHogar(idHogar) == null) throw new IllegalArgumentException("Seleccione una vivienda válida.");
        if (acumuladoM3 < 0) throw new IllegalArgumentException("La lectura no puede ser negativa.");
        validarConsistenciaCronologica(idHogar, -1, fechaHora, acumuladoM3);

        LecturaMedidor nueva = new LecturaMedidor(seqLectura++, idHogar, fechaHora, acumuladoM3, obs == null ? "" : obs.trim());
        lecturas.add(nueva);
        lecturas.sort(Comparator.comparing(LecturaMedidor::getFechaHora));
        guardarDatos();
        return nueva;
    }

    public void actualizarLectura(int idLectura, LocalDateTime fechaHora, double acumuladoM3, String obs) {
        LecturaMedidor target = buscarLectura(idLectura);
        if (target == null) throw new IllegalArgumentException("Lectura no encontrada.");
        if (acumuladoM3 < 0) throw new IllegalArgumentException("La lectura no puede ser negativa.");
        validarConsistenciaCronologica(target.getIdHogar(), idLectura, fechaHora, acumuladoM3);

        target.setFechaHora(fechaHora);
        target.setAcumuladoM3(acumuladoM3);
        target.setObservacion(obs == null ? "" : obs.trim());
        lecturas.sort(Comparator.comparing(LecturaMedidor::getFechaHora));
        guardarDatos();
    }

    public void eliminarLectura(int idLectura) {
        lecturas.removeIf(l -> l.getId() == idLectura);
        guardarDatos();
    }

    private void validarConsistenciaCronologica(int idHogar, int idExcluir, LocalDateTime fechaHora, double acumulado) {
        for (LecturaMedidor l : lecturas) {
            if (l.getIdHogar() != idHogar || l.getId() == idExcluir) continue;
            if (l.getFechaHora().equals(fechaHora)) {
                throw new IllegalArgumentException("Ya existe una lectura registrada exactamente en esa fecha y hora.");
            }
            if (l.getFechaHora().isBefore(fechaHora) && acumulado < l.getAcumuladoM3()) {
                throw new IllegalArgumentException("Error de validación: la lectura (" + acumulado +
                        " m³) no puede ser inferior a una lectura anterior (" + l.getAcumuladoM3() + " m³).");
            }
            if (l.getFechaHora().isAfter(fechaHora) && acumulado > l.getAcumuladoM3()) {
                throw new IllegalArgumentException("Error de validación: la lectura (" + acumulado +
                        " m³) no puede ser mayor a una lectura posterior (" + l.getAcumuladoM3() + " m³).");
            }
        }
    }

    public LecturaMedidor buscarLectura(int id) {
        return lecturas.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    public List<LecturaMedidor> getLecturasPorHogar(int idHogar) {
        List<LecturaMedidor> res = new ArrayList<>();
        for (LecturaMedidor l : lecturas) if (l.getIdHogar() == idHogar) res.add(l);
        return res;
    }

    // --- RF2: COMPARAR CONSUMO IDEAL VS REAL ---
    public String evaluarConsumoIdealVsReal(int idHogar, LocalDate desde, LocalDate hasta) {
        Hogar h = buscarHogar(idHogar);
        if (h == null) return "Seleccione una vivienda.";
        List<LecturaMedidor> delPeriodo = new ArrayList<>();
        for (LecturaMedidor l : lecturas) {
            LocalDate f = l.getFechaHora().toLocalDate();
            if (l.getIdHogar() == idHogar && !f.isBefore(desde) && !f.isAfter(hasta)) {
                delPeriodo.add(l);
            }
        }
        if (delPeriodo.size() < 2) {
            return "Datos insuficientes: se necesitan al menos 2 lecturas registradas dentro del rango de fechas.";
        }
        LecturaMedidor primera = delPeriodo.get(0);
        LecturaMedidor ultima = delPeriodo.get(delPeriodo.size() - 1);
        double minutos = Duration.between(primera.getFechaHora(), ultima.getFechaHora()).toMinutes();
        if (minutos <= 0) return "Intervalo de tiempo inválido entre lecturas.";

        double dias = minutos / 1440.0;
        double litrosTotales = (ultima.getAcumuladoM3() - primera.getAcumuladoM3()) * 1000.0;
        double consumoRealDiario = litrosTotales / dias;
        double consumoIdealDiario = h.getHabitantes() * referenciaOmsLitrosPersonaDia;
        double diferencia = consumoRealDiario - consumoIdealDiario;
        String estado = consumoRealDiario <= consumoIdealDiario ? "Consumo eficiente" : "Consumo excesivo";

        return String.format(Locale.ROOT,
                "Vivienda: %s (%d habitantes)\n" +
                        "Consumo ideal diario (OMS %.0f L/pers): %.2f L/día\n" +
                        "Consumo real promedio del periodo: %.2f L/día\n" +
                        "Diferencia: %+.2f L/día\n" +
                        "Dictamen: %s",
                h.getNombreResponsable(), h.getHabitantes(), referenciaOmsLitrosPersonaDia,
                consumoIdealDiario, consumoRealDiario, diferencia, estado);
    }

    // --- RF7: DETECCIÓN DE POSIBLE FUGA CON UMBRAL NOCTURNO ---
    public PruebaFuga ejecutarPruebaFugaNocturna(int idHogar, int idLecInicial, int idLecFinal,
                                                 boolean sinUsoConfirmado, double umbralNocturnoLH, String obs) {
        LecturaMedidor ini = buscarLectura(idLecInicial);
        LecturaMedidor fin = buscarLectura(idLecFinal);
        if (ini == null || fin == null || ini.getIdHogar() != idHogar || fin.getIdHogar() != idHogar) {
            throw new IllegalArgumentException("Seleccione dos lecturas válidas de la misma vivienda.");
        }
        long minutos = Duration.between(ini.getFechaHora(), fin.getFechaHora()).toMinutes();
        if (minutos <= 0) throw new IllegalArgumentException("La lectura final debe ser posterior a la lectura inicial.");
        if (umbralNocturnoLH < 0) throw new IllegalArgumentException("El umbral nocturno no puede ser negativo.");

        // Verifica si las lecturas corresponden a horario nocturno (22:00 a 06:00)
        int horaIni = ini.getFechaHora().getHour();
        int horaFin = fin.getFechaHora().getHour();
        boolean esNocturno = (horaIni >= 22 || horaIni <= 6) && (horaFin >= 22 || horaFin <= 6) && (minutos <= 600);

        PruebaFuga prueba = new PruebaFuga(seqPrueba++, idHogar, idLecInicial, idLecFinal,
                sinUsoConfirmado, esNocturno, umbralNocturnoLH, obs == null ? "" : obs.trim());

        double perdidaLitros = (fin.getAcumuladoM3() - ini.getAcumuladoM3()) * 1000.0;
        double duracionHoras = minutos / 60.0;
        double tasaLH = perdidaLitros / duracionHoras;

        prueba.setPerdidaLitros(perdidaLitros);
        prueba.setDuracionHoras(duracionHoras);
        prueba.setTasaLitrosHora(tasaLH);

        if (!sinUsoConfirmado) {
            prueba.setResultado("No concluyente (hubo uso de agua)");
        } else if (tasaLH > umbralNocturnoLH) {
            prueba.setResultado("Posible fuga");
            String motivo = String.format(Locale.ROOT,
                    "Consumo de %.2f L en %.2f h (%.2f L/h) supera el umbral nocturno de %.2f L/h.%s",
                    perdidaLitros, duracionHoras, tasaLH, umbralNocturnoLH,
                    esNocturno ? "" : " [Advertencia: fuera de franja 22:00-06:00]");
            alertas.add(new AlertaFuga(seqAlerta++, idHogar, prueba.getId(), motivo));
        } else {
            prueba.setResultado("Normal");
        }

        pruebas.add(prueba);
        guardarDatos();
        return prueba;
    }

    public void actualizarAlerta(int idAlerta, String estado, String ubicacion, String accion) {
        for (AlertaFuga a : alertas) {
            if (a.getId() == idAlerta) {
                a.setEstado(estado);
                a.setUbicacionSospechada(ubicacion.trim());
                a.setAccionRealizada(accion.trim());
                guardarDatos();
                return;
            }
        }
    }

    public List<PruebaFuga> getPruebasPorHogar(int idHogar) {
        List<PruebaFuga> res = new ArrayList<>();
        for (PruebaFuga p : pruebas) if (p.getIdHogar() == idHogar) res.add(p);
        return res;
    }

    public List<AlertaFuga> getAlertasPorHogar(int idHogar) {
        List<AlertaFuga> res = new ArrayList<>();
        for (AlertaFuga a : alertas) if (a.getIdHogar() == idHogar) res.add(a);
        return res;
    }

    // --- METAS DE AHORRO ---
    public MetaAhorro crearMeta(int idHogar, LocalDate inicio, LocalDate fin, double baseLPersona, double reduccionPct, String desc) {
        if (buscarHogar(idHogar) == null) throw new IllegalArgumentException("Seleccione una vivienda.");
        if (!fin.isAfter(inicio)) throw new IllegalArgumentException("La fecha fin debe ser posterior a la inicial.");
        if (baseLPersona <= 0 || reduccionPct <= 0 || reduccionPct >= 100) {
            throw new IllegalArgumentException("Valores de base o porcentaje de reducción inválidos.");
        }
        validarTexto(desc, "La descripción de la meta es obligatoria.");
        MetaAhorro m = new MetaAhorro(seqMeta++, idHogar, inicio, fin, baseLPersona, reduccionPct, desc.trim());
        metas.add(m);
        guardarDatos();
        return m;
    }

    public String evaluarMeta(int idMeta) {
        MetaAhorro m = metas.stream().filter(x -> x.getId() == idMeta).findFirst().orElse(null);
        Hogar h = m == null ? null : buscarHogar(m.getIdHogar());
        if (m == null || h == null) throw new IllegalArgumentException("Meta o vivienda no encontrada.");

        List<LecturaMedidor> r = new ArrayList<>();
        for (LecturaMedidor l : lecturas) {
            LocalDate f = l.getFechaHora().toLocalDate();
            if (l.getIdHogar() == h.getId() && !f.isBefore(m.getFechaInicio()) && !f.isAfter(m.getFechaFin())) {
                r.add(l);
            }
        }
        if (r.size() < 2) {
            m.setEstado("Datos insuficientes");
            guardarDatos();
            return "Se requieren al menos 2 lecturas dentro del periodo de la meta.";
        }
        double dias = Duration.between(r.get(0).getFechaHora(), r.get(r.size() - 1).getFechaHora()).toMinutes() / 1440.0;
        if (dias <= 0) return "Periodo de lecturas inválido.";
        double litros = (r.get(r.size() - 1).getAcumuladoM3() - r.get(0).getAcumuladoM3()) * 1000.0;
        double realPersonaDia = (litros / dias) / h.getHabitantes();
        double objetivo = m.calcularObjetivoLitrosPersonaDia();

        m.setEstado(realPersonaDia <= objetivo ? "Cumplida" : "No cumplida");
        guardarDatos();
        return String.format(Locale.ROOT, "Consumo real: %.2f L/persona/día | Objetivo: %.2f L/persona/día -> %s",
                realPersonaDia, objetivo, m.getEstado());
    }

    public void eliminarMeta(int idMeta) {
        metas.removeIf(m -> m.getId() == idMeta);
        guardarDatos();
    }

    public List<MetaAhorro> getMetasPorHogar(int idHogar) {
        List<MetaAhorro> res = new ArrayList<>();
        for (MetaAhorro m : metas) if (m.getIdHogar() == idHogar) res.add(m);
        return res;
    }

    public double getUmbralNocturnoDefectoLH() { return umbralNocturnoDefectoLH; }
    public void setUmbralNocturnoDefectoLH(double umbral) {
        if (umbral < 0) throw new IllegalArgumentException("El umbral debe ser >= 0.");
        this.umbralNocturnoDefectoLH = umbral;
        guardarDatos();
    }

    private void validarTexto(String txt, String msg) {
        if (txt == null || txt.trim().isEmpty()) throw new IllegalArgumentException(msg);
    }
}