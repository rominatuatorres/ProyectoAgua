package interfaz;

import modelo.*;
import negocio.GestorSistemaAgua;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;
    private final GestorSistemaAgua gestor = new GestorSistemaAgua();
    private final JComboBox<Hogar> comboHogares = new JComboBox<>();

    private DefaultTableModel modHogares, modPuntos, modLecturas, modPruebas, modAlertas, modMetas;
    private JTable tabHogares, tabPuntos, tabLecturas, tabPruebas, tabAlertas, tabMetas;

    public VentanaPrincipal() {
        super("AquaControl Quito — Gestión Hídrica y Detección de Fugas Nocturnas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 700);
        setLocationRelativeTo(null);
        construirUI();
        refrescarTodo();
    }

    private void construirUI() {
        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        barraSuperior.setBackground(new Color(227, 242, 253));
        barraSuperior.add(new JLabel("Vivienda seleccionada:"));
        comboHogares.setPreferredSize(new Dimension(360, 28));
        comboHogares.addActionListener(e -> refrescarTablasDependientes());
        barraSuperior.add(comboHogares);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Viviendas (RF1)", crearPanelHogares());
        tabs.addTab("2. Puntos de Consumo (RF3-RF4)", crearPanelPuntos());
        tabs.addTab("3. Lecturas Medidor (RF5-RF6)", crearPanelLecturas());
        tabs.addTab("4. Ideal vs Real (RF2)", crearPanelComparacion());
        tabs.addTab("5. Fugas Nocturnas (RF7)", crearPanelFugas());
        tabs.addTab("6. Metas de Ahorro", crearPanelMetas());

        add(barraSuperior, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // --- PANEL 1: VIVIENDAS ---
    private JPanel crearPanelHogares() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        modHogares = modeloNoEditable("ID", "Responsable", "Dirección", "Barrio", "N° Medidor", "Habitantes");
        tabHogares = new JTable(modHogares);
        p.add(new JScrollPane(tabHogares), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevo = new JButton("Registrar Vivienda");
        JButton btnEditar = new JButton("Editar Seleccionada");
        JButton btnEliminar = new JButton("Eliminar");

        btnNuevo.addActionListener(e -> dialogoHogar(null));
        btnEditar.addActionListener(e -> {
            int row = tabHogares.getSelectedRow();
            if (row < 0) { error("Seleccione una vivienda de la tabla."); return; }
            dialogoHogar(gestor.buscarHogar((int) modHogares.getValueAt(row, 0)));
        });
        btnEliminar.addActionListener(e -> {
            int row = tabHogares.getSelectedRow();
            if (row < 0) { error("Seleccione una vivienda."); return; }
            int id = (int) modHogares.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar vivienda y todos sus registros?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                gestor.eliminarHogar(id);
                refrescarTodo();
            }
        });
        bot.add(btnNuevo); bot.add(btnEditar); bot.add(btnEliminar);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private void dialogoHogar(Hogar h) {
        JTextField txtResp = new JTextField(h == null ? "" : h.getNombreResponsable());
        JTextField txtDir = new JTextField(h == null ? "" : h.getDireccion());
        JTextField txtBarrio = new JTextField(h == null ? "" : h.getBarrio());
        JTextField txtMed = new JTextField(h == null ? "" : h.getCodigoMedidor());
        JTextField txtHab = new JTextField(h == null ? "" : String.valueOf(h.getHabitantes()));

        Object[] form = {"Responsable:", txtResp, "Dirección:", txtDir, "Barrio:", txtBarrio, "N° Medidor:", txtMed, "Habitantes:", txtHab};
        if (JOptionPane.showConfirmDialog(this, form, h == null ? "Registrar Vivienda" : "Editar Vivienda", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ejecutar(() -> {
                int hab = Integer.parseInt(txtHab.getText().trim());
                if (h == null) gestor.registrarHogar(txtResp.getText(), txtDir.getText(), txtBarrio.getText(), txtMed.getText(), hab);
                else gestor.actualizarHogar(h.getId(), txtResp.getText(), txtDir.getText(), txtBarrio.getText(), txtMed.getText(), hab);
                refrescarTodo();
            });
        }
    }

    // --- PANEL 2: PUNTOS DE CONSUMO ---
    private JPanel crearPanelPuntos() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        modPuntos = modeloNoEditable("ID", "Tipo", "Nombre", "Ubicación", "Frec./día", "Vol. Diario (L)", "Mensual Est. (m³)", "Impacto (%)", "Estado");
        tabPuntos = new JTable(modPuntos);
        p.add(new JScrollPane(tabPuntos), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Agregar Punto de Consumo");
        JButton btnEstado = new JButton("Dar de Baja / Activar");
        JButton btnDel = new JButton("Eliminar");

        btnAdd.addActionListener(e -> dialogoPunto());
        btnEstado.addActionListener(e -> {
            int row = tabPuntos.getSelectedRow();
            if (row < 0) { error("Seleccione un punto."); return; }
            gestor.cambiarEstadoPunto((int) modPuntos.getValueAt(row, 0));
            refrescarTablasDependientes();
        });
        btnDel.addActionListener(e -> {
            int row = tabPuntos.getSelectedRow();
            if (row < 0) { error("Seleccione un punto."); return; }
            gestor.eliminarPunto((int) modPuntos.getValueAt(row, 0));
            refrescarTablasDependientes();
        });
        bot.add(btnAdd); bot.add(btnEstado); bot.add(btnDel);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private void dialogoPunto() {
        int idHogar = idHogarActivo();
        if (idHogar < 0) { error("Primero registre una vivienda."); return; }

        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Ducha", "Grifo", "Manguera de jardín", "Inodoro", "Lavadora"});
        JTextField txtNom = new JTextField();
        JTextField txtUbi = new JTextField();
        JTextField txtFrec = new JTextField();
        JTextField txtP1 = new JTextField();
        JTextField txtP2 = new JTextField();
        JLabel lblP1 = new JLabel("Caudal (L/min):");
        JLabel lblP2 = new JLabel("Duración media (min):");

        cbTipo.addActionListener(e -> {
            String sel = (String) cbTipo.getSelectedItem();
            boolean esCaudal = sel.equals("Ducha") || sel.equals("Grifo") || sel.equals("Manguera de jardín");
            lblP1.setText(esCaudal ? "Caudal (L/min):" : "Litros por descarga/ciclo:");
            lblP2.setVisible(esCaudal);
            txtP2.setVisible(esCaudal);
        });

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Artefacto:")); panel.add(cbTipo);
        panel.add(new JLabel("Nombre:")); panel.add(txtNom);
        panel.add(new JLabel("Ubicación:")); panel.add(txtUbi);
        panel.add(new JLabel("Frecuencia diaria (veces/día):")); panel.add(txtFrec);
        panel.add(lblP1); panel.add(txtP1);
        panel.add(lblP2); panel.add(txtP2);

        if (JOptionPane.showConfirmDialog(this, panel, "Registrar Punto de Consumo", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ejecutar(() -> {
                String tipo = (String) cbTipo.getSelectedItem();
                double f = Double.parseDouble(txtFrec.getText().trim());
                double p1 = Double.parseDouble(txtP1.getText().trim());
                double p2 = txtP2.isVisible() ? Double.parseDouble(txtP2.getText().trim()) : 0;
                gestor.registrarPunto(idHogar, tipo, txtNom.getText(), txtUbi.getText(), f, p1, p2);
                refrescarTablasDependientes();
            });
        }
    }

    // --- PANEL 3: LECTURAS DEL MEDIDOR ---
    private JPanel crearPanelLecturas() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        modLecturas = modeloNoEditable("ID", "Fecha y Hora", "Acumulado (m³)", "Observación");
        tabLecturas = new JTable(modLecturas);
        p.add(new JScrollPane(tabLecturas), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Registrar Lectura");
        JButton btnEdit = new JButton("Modificar");
        JButton btnDel = new JButton("Eliminar");

        btnAdd.addActionListener(e -> dialogoLectura(null));
        btnEdit.addActionListener(e -> {
            int row = tabLecturas.getSelectedRow();
            if (row < 0) { error("Seleccione una lectura."); return; }
            dialogoLectura(gestor.buscarLectura((int) modLecturas.getValueAt(row, 0)));
        });
        btnDel.addActionListener(e -> {
            int row = tabLecturas.getSelectedRow();
            if (row < 0) { error("Seleccione una lectura."); return; }
            gestor.eliminarLectura((int) modLecturas.getValueAt(row, 0));
            refrescarTablasDependientes();
        });
        bot.add(btnAdd); bot.add(btnEdit); bot.add(btnDel);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private void dialogoLectura(LecturaMedidor l) {
        int idHogar = l == null ? idHogarActivo() : l.getIdHogar();
        if (idHogar < 0) { error("Primero registre una vivienda."); return; }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JTextField txtFechaHora = new JTextField(l == null ? LocalDateTime.now().format(fmt) : l.getFechaHoraFormateada());
        JTextField txtM3 = new JTextField(l == null ? "" : String.valueOf(l.getAcumuladoM3()));
        JTextField txtObs = new JTextField(l == null ? "" : l.getObservacion());

        Object[] form = {"Fecha y Hora (yyyy-MM-dd HH:mm):", txtFechaHora, "Lectura Medidor (m³):", txtM3, "Observación:", txtObs};
        if (JOptionPane.showConfirmDialog(this, form, l == null ? "Registrar Lectura" : "Modificar Lectura", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ejecutar(() -> {
                LocalDateTime fh = LocalDateTime.parse(txtFechaHora.getText().trim(), fmt);
                double m3 = Double.parseDouble(txtM3.getText().trim());
                if (l == null) gestor.registrarLectura(idHogar, fh, m3, txtObs.getText());
                else gestor.actualizarLectura(l.getId(), fh, m3, txtObs.getText());
                refrescarTablasDependientes();
            });
        }
    }

    // --- PANEL 4: CONSUMO IDEAL VS REAL ---
    private JPanel crearPanelComparacion() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtDesde = new JTextField(LocalDate.now().minusDays(7).toString(), 10);
        JTextField txtHasta = new JTextField(LocalDate.now().toString(), 10);
        JButton btnCalc = new JButton("Comparar Consumo Real vs Ideal (OMS 100 L/pers/día)");
        top.add(new JLabel("Desde (yyyy-MM-dd):")); top.add(txtDesde);
        top.add(new JLabel("Hasta (yyyy-MM-dd):")); top.add(txtHasta);
        top.add(btnCalc);

        JTextArea areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setFont(new Font("Monospaced", Font.PLAIN, 14));

        btnCalc.addActionListener(e -> ejecutar(() -> {
            int idHogar = idHogarActivo();
            if (idHogar < 0) throw new IllegalArgumentException("Seleccione una vivienda.");
            LocalDate d1 = LocalDate.parse(txtDesde.getText().trim());
            LocalDate d2 = LocalDate.parse(txtHasta.getText().trim());
            areaResultado.setText(gestor.evaluarConsumoIdealVsReal(idHogar, d1, d2));
        }));

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(areaResultado), BorderLayout.CENTER);
        return p;
    }

    // --- PANEL 5: PRUEBAS DE FUGA Y UMBRAL NOCTURNO ---
    private JPanel crearPanelFugas() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        modPruebas = modeloNoEditable("ID", "Lec. Ini", "Lec. Fin", "Horario Nocturno", "Pérdida (L)", "Duración (h)", "Tasa (L/h)", "Umbral (L/h)", "Diagnóstico");
        modAlertas = modeloNoEditable("ID Alerta", "ID Prueba", "Estado", "Motivo", "Ubicación Sospechada", "Acción Realizada");
        tabPruebas = new JTable(modPruebas);
        tabAlertas = new JTable(modAlertas);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabPruebas), new JScrollPane(tabAlertas));
        split.setResizeWeight(0.5);
        p.add(split, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnPrueba = new JButton("Nueva Prueba Nocturna de Fuga");
        JButton btnAlerta = new JButton("Gestionar Alerta Seleccionada");

        btnPrueba.addActionListener(e -> dialogoPruebaFuga());
        btnAlerta.addActionListener(e -> dialogoGestionAlerta());

        bot.add(btnPrueba); bot.add(btnAlerta);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private void dialogoPruebaFuga() {
        int idHogar = idHogarActivo();
        if (idHogar < 0) { error("Seleccione una vivienda."); return; }

        JComboBox<LecturaMedidor> cbIni = new JComboBox<>();
        JComboBox<LecturaMedidor> cbFin = new JComboBox<>();
        for (LecturaMedidor l : gestor.getLecturasPorHogar(idHogar)) {
            cbIni.addItem(l);
            cbFin.addItem(l);
        }
        if (cbIni.getItemCount() < 2) { error("Registre al menos 2 lecturas del medidor."); return; }

        JTextField txtUmbral = new JTextField(String.valueOf(gestor.getUmbralNocturnoDefectoLH()));
        JTextField txtObs = new JTextField();
        JCheckBox chkSinUso = new JCheckBox("Confirmo cero consumo humano durante la prueba (ej. 22:00 a 06:00)", true);

        Object[] form = {
                "Lectura Inicial (Noche):", cbIni,
                "Lectura Final (Madrugada):", cbFin,
                "Umbral Nocturno Admisible (L/h):", txtUmbral,
                "Confirmación:", chkSinUso,
                "Observaciones:", txtObs
        };

        if (JOptionPane.showConfirmDialog(this, form, "Evaluación Nocturna de Fugas", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ejecutar(() -> {
                LecturaMedidor l1 = (LecturaMedidor) cbIni.getSelectedItem();
                LecturaMedidor l2 = (LecturaMedidor) cbFin.getSelectedItem();
                double umbral = Double.parseDouble(txtUmbral.getText().trim());
                PruebaFuga pf = gestor.ejecutarPruebaFugaNocturna(idHogar, l1.getId(), l2.getId(), chkSinUso.isSelected(), umbral, txtObs.getText());
                refrescarTablasDependientes();
                JOptionPane.showMessageDialog(this,
                        String.format(Locale.ROOT, "Resultado: %s\nPérdida detectada: %.2f L en %.2f h (%.2f L/h)\nHorario nocturno (22h-06h): %s",
                                pf.getResultado(), pf.getPerdidaLitros(), pf.getDuracionHoras(), pf.getTasaLitrosHora(),
                                pf.isHorarioNocturnoValido() ? "Sí" : "Fuera de rango nocturno"),
                        "Diagnóstico de Fuga", JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    private void dialogoGestionAlerta() {
        int row = tabAlertas.getSelectedRow();
        if (row < 0) { error("Seleccione una alerta de la tabla inferior."); return; }
        int idAlerta = (int) modAlertas.getValueAt(row, 0);

        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Pendiente", "En revisión", "Cerrada"});
        cbEstado.setSelectedItem(modAlertas.getValueAt(row, 2));
        JTextField txtUbi = new JTextField((String) modAlertas.getValueAt(row, 4));
        JTextField txtAcc = new JTextField((String) modAlertas.getValueAt(row, 5));

        Object[] form = {"Estado:", cbEstado, "Ubicación Sospechada (ej. Inodoro baño 2):", txtUbi, "Acción Realizada:", txtAcc};
        if (JOptionPane.showConfirmDialog(this, form, "Seguimiento de Alerta", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            gestor.actualizarAlerta(idAlerta, (String) cbEstado.getSelectedItem(), txtUbi.getText(), txtAcc.getText());
            refrescarTablasDependientes();
        }
    }

    // --- PANEL 6: METAS DE AHORRO ---
    private JPanel crearPanelMetas() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        modMetas = modeloNoEditable("ID", "Inicio", "Fin", "Base (L/pers/día)", "Reducción (%)", "Objetivo (L/pers/día)", "Descripción", "Estado");
        tabMetas = new JTable(modMetas);
        p.add(new JScrollPane(tabMetas), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Meta");
        JButton btnEval = new JButton("Evaluar Cumplimiento");
        JButton btnDel = new JButton("Eliminar");

        btnNueva.addActionListener(e -> {
            int idHogar = idHogarActivo();
            if (idHogar < 0) { error("Seleccione una vivienda."); return; }
            JTextField txtIni = new JTextField(LocalDate.now().toString());
            JTextField txtFin = new JTextField(LocalDate.now().plusDays(30).toString());
            JTextField txtBase = new JTextField("142"); // Referencia Quito
            JTextField txtRed = new JTextField("15");
            JTextField txtDesc = new JTextField();
            Object[] form = {"Inicio (yyyy-MM-dd):", txtIni, "Fin (yyyy-MM-dd):", txtFin, "Base (L/pers/día):", txtBase, "Reducción (%):", txtRed, "Descripción:", txtDesc};
            if (JOptionPane.showConfirmDialog(this, form, "Crear Meta de Ahorro", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                ejecutar(() -> {
                    gestor.crearMeta(idHogar, LocalDate.parse(txtIni.getText().trim()), LocalDate.parse(txtFin.getText().trim()),
                            Double.parseDouble(txtBase.getText().trim()), Double.parseDouble(txtRed.getText().trim()), txtDesc.getText());
                    refrescarTablasDependientes();
                });
            }
        });

        btnEval.addActionListener(e -> {
            int row = tabMetas.getSelectedRow();
            if (row < 0) { error("Seleccione una meta."); return; }
            ejecutar(() -> {
                String res = gestor.evaluarMeta((int) modMetas.getValueAt(row, 0));
                refrescarTablasDependientes();
                JOptionPane.showMessageDialog(this, res, "Resultado Meta", JOptionPane.INFORMATION_MESSAGE);
            });
        });

        btnDel.addActionListener(e -> {
            int row = tabMetas.getSelectedRow();
            if (row < 0) { error("Seleccione una meta."); return; }
            gestor.eliminarMeta((int) modMetas.getValueAt(row, 0));
            refrescarTablasDependientes();
        });

        bot.add(btnNueva); bot.add(btnEval); bot.add(btnDel);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    // --- UTILIDADES DE INTERFAZ ---
    private void refrescarTodo() {
        Hogar sel = (Hogar) comboHogares.getSelectedItem();
        comboHogares.removeAllItems();
        for (Hogar h : gestor.getHogares()) comboHogares.addItem(h);
        if (sel != null) {
            for (int i = 0; i < comboHogares.getItemCount(); i++) {
                if (comboHogares.getItemAt(i).getId() == sel.getId()) {
                    comboHogares.setSelectedIndex(i);
                    break;
                }
            }
        }
        modHogares.setRowCount(0);
        for (Hogar h : gestor.getHogares()) {
            modHogares.addRow(new Object[]{h.getId(), h.getNombreResponsable(), h.getDireccion(), h.getBarrio(), h.getCodigoMedidor(), h.getHabitantes()});
        }
        refrescarTablasDependientes();
    }

    private void refrescarTablasDependientes() {
        int idHogar = idHogarActivo();
        modPuntos.setRowCount(0); modLecturas.setRowCount(0);
        modPruebas.setRowCount(0); modAlertas.setRowCount(0); modMetas.setRowCount(0);
        if (idHogar < 0) return;

        for (PuntoConsumo p : gestor.getPuntosPorHogar(idHogar)) {
            modPuntos.addRow(new Object[]{p.getId(), p.getTipo(), p.getNombre(), p.getUbicacion(), p.getFrecuenciaDiaria(),
                    String.format(Locale.ROOT, "%.2f", p.calcularConsumoDiarioLitros()),
                    String.format(Locale.ROOT, "%.3f", p.calcularConsumoMensualM3()),
                    String.format(Locale.ROOT, "%.1f%%", gestor.calcularPorcentajeImpactoPunto(p)),
                    p.isActivo() ? "Activo" : "Baja"});
        }
        for (LecturaMedidor l : gestor.getLecturasPorHogar(idHogar)) {
            modLecturas.addRow(new Object[]{l.getId(), l.getFechaHoraFormateada(), String.format(Locale.ROOT, "%.3f", l.getAcumuladoM3()), l.getObservacion()});
        }
        for (PruebaFuga pf : gestor.getPruebasPorHogar(idHogar)) {
            modPruebas.addRow(new Object[]{pf.getId(), pf.getIdLecturaInicial(), pf.getIdLecturaFinal(),
                    pf.isHorarioNocturnoValido() ? "Sí (22h-06h)" : "No",
                    String.format(Locale.ROOT, "%.2f", pf.getPerdidaLitros()),
                    String.format(Locale.ROOT, "%.2f", pf.getDuracionHoras()),
                    String.format(Locale.ROOT, "%.2f", pf.getTasaLitrosHora()),
                    String.format(Locale.ROOT, "%.2f", pf.getUmbralNocturnoLitrosHora()),
                    pf.getResultado()});
        }
        for (AlertaFuga a : gestor.getAlertasPorHogar(idHogar)) {
            modAlertas.addRow(new Object[]{a.getId(), a.getIdPrueba(), a.getEstado(), a.getMotivo(), a.getUbicacionSospechada(), a.getAccionRealizada()});
        }
        for (MetaAhorro m : gestor.getMetasPorHogar(idHogar)) {
            modMetas.addRow(new Object[]{m.getId(), m.getFechaInicio(), m.getFechaFin(), m.getBaseLitrosPersonaDia(),
                    m.getReduccionPorcentaje(), String.format(Locale.ROOT, "%.2f", m.calcularObjetivoLitrosPersonaDia()),
                    m.getDescripcion(), m.getEstado()});
        }
    }

    private int idHogarActivo() {
        Hogar h = (Hogar) comboHogares.getSelectedItem();
        return h == null ? -1 : h.getId();
    }

    private DefaultTableModel modeloNoEditable(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private void ejecutar(Runnable r) {
        try { r.run(); } catch (Exception ex) { error(ex.getMessage()); }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación del Sistema", JOptionPane.ERROR_MESSAGE);
    }
}