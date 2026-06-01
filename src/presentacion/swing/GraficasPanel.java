package presentacion.swing;

import modelo.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GraficasPanel extends JPanel {

    private Cafe cafe;

    public GraficasPanel(Cafe cafe) {
        this.cafe = cafe;
        setLayout(new GridLayout(2, 2, 8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        construirGraficas();
    }

    private void construirGraficas() {
        removeAll();
        add(crearPanelPastel());
        add(crearPanelBarras());
        add(crearPanelLineas());
        add(crearPanelInfo());
        revalidate();
        repaint();
    }

    // ── PASTEL ───────────────────────────────────────────────────────
    private JPanel crearPanelPastel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Disponibilidad de juego"));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cmbJuego = new JComboBox<>();
        for (InventarioJuegos inv : cafe.getInventariosJuegos()) {
            cmbJuego.addItem(inv.getJuego().getNombre());
        }
        JButton btnVer = new JButton("Ver");
        estilizarBtn(btnVer);
        controles.add(new JLabel("Juego:"));
        controles.add(cmbJuego);
        controles.add(btnVer);

        JPanel contenedor = new JPanel(new BorderLayout());

        Runnable actualizar = () -> {
            int idx = cmbJuego.getSelectedIndex();
            if (idx < 0 || cafe.getInventariosJuegos().isEmpty()) return;
            InventarioJuegos inv = cafe.getInventariosJuegos().get(idx);
            JFreeChart chart = generarPastel(inv);
            contenedor.removeAll();
            contenedor.add(new ChartPanel(chart), BorderLayout.CENTER);
            contenedor.revalidate();
            contenedor.repaint();
        };

        btnVer.addActionListener(e -> actualizar.run());

        if (!cafe.getInventariosJuegos().isEmpty()) actualizar.run();

        panel.add(controles, BorderLayout.NORTH);
        panel.add(contenedor, BorderLayout.CENTER);
        return panel;
    }

    private JFreeChart generarPastel(InventarioJuegos inv) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        int disponibles = inv.getCantidadDisponible();
        int prestadas = contarCopiasEnPrestamo(inv.getJuego());
        dataset.setValue("Copias disponibles", Math.max(disponibles, 0));
        dataset.setValue("Copias prestadas", Math.max(prestadas, 0));

        JFreeChart chart = ChartFactory.createPieChart(
            inv.getJuego().getNombre(), dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setSectionPaint("Copias disponibles", new Color(74, 63, 160));
        plot.setSectionPaint("Copias prestadas", new Color(159, 153, 232));
        plot.setOutlineVisible(false);
        return chart;
    }

    private int contarCopiasEnPrestamo(Juego juego) {
        int count = 0;
        for (Prestamo p : cafe.getPrestamos()) {
            if ("ACTIVO".equals(p.getEstado())) {
                for (Juego j : p.getJuegos()) {
                    if (j.getNombre().equals(juego.getNombre())) count++;
                }
            }
        }
        return count;
    }

    // ── BARRAS ───────────────────────────────────────────────────────
    private JPanel crearPanelBarras() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Ventas por período (5 días)"));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Calendar hoy = Calendar.getInstance();
        JSpinner spinDia = new JSpinner(new SpinnerNumberModel(hoy.get(Calendar.DAY_OF_MONTH), 1, 28, 1));
        JSpinner spinMes = new JSpinner(new SpinnerNumberModel(hoy.get(Calendar.MONTH)+1, 1, 12, 1));
        JSpinner spinAnio = new JSpinner(new SpinnerNumberModel(hoy.get(Calendar.YEAR), 2020, 2030, 1));
        JButton btnVer = new JButton("Ver");
        estilizarBtn(btnVer);

        controles.add(new JLabel("Desde día:"));
        controles.add(spinDia);
        controles.add(new JLabel("mes:"));
        controles.add(spinMes);
        controles.add(new JLabel("año:"));
        controles.add(spinAnio);
        controles.add(btnVer);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(new ChartPanel(generarBarras(new Date())), BorderLayout.CENTER);

        btnVer.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.set((int)spinAnio.getValue(), (int)spinMes.getValue()-1, (int)spinDia.getValue(), 0, 0, 0);
            JFreeChart chart = generarBarras(cal.getTime());
            contenedor.removeAll();
            contenedor.add(new ChartPanel(chart), BorderLayout.CENTER);
            contenedor.revalidate();
            contenedor.repaint();
        });

        panel.add(controles, BorderLayout.NORTH);
        panel.add(contenedor, BorderLayout.CENTER);
        return panel;
    }

    private JFreeChart generarBarras(Date fechaInicio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicio);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        for (int i = 0; i < 5; i++) {
            Calendar dia = (Calendar) cal.clone();
            dia.add(Calendar.DAY_OF_YEAR, i);
            String etiqueta = dia.get(Calendar.DAY_OF_MONTH) + "/"
                + (dia.get(Calendar.MONTH)+1) + "/"
                + (dia.get(Calendar.YEAR) % 100);

            double totalCafe = 0, totalJuegos = 0;
            for (Venta v : cafe.getVentas()) {
                Calendar calV = Calendar.getInstance();
                calV.setTime(v.getFecha());
                if (calV.get(Calendar.YEAR) == dia.get(Calendar.YEAR) &&
                    calV.get(Calendar.DAY_OF_YEAR) == dia.get(Calendar.DAY_OF_YEAR)) {
                    double subtotal = v.calcularSubtotal();
                    if ("CAFETERIA".equals(v.getTipoVenta())) totalCafe += subtotal;
                    else totalJuegos += subtotal;
                }
            }
            dataset.addValue(totalCafe, "Cafetería", etiqueta);
            dataset.addValue(totalJuegos, "Juegos", etiqueta);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Ventas por período", "Fecha", "Valor en Pesos ($)",
            dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(212, 83, 126));
        plot.getRenderer().setSeriesPaint(1, new Color(74, 63, 160));
        return chart;
    }

    // ── LÍNEAS ───────────────────────────────────────────────────────
    private JPanel crearPanelLineas() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Reservas por semana"));

        Calendar hoy = Calendar.getInstance();
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner spinSemana = new JSpinner(new SpinnerNumberModel(hoy.get(Calendar.WEEK_OF_YEAR), 1, 52, 1));
        JSpinner spinAnio = new JSpinner(new SpinnerNumberModel(hoy.get(Calendar.YEAR), 2020, 2030, 1));
        JButton btnVer = new JButton("Ver");
        estilizarBtn(btnVer);

        controles.add(new JLabel("Semana:"));
        controles.add(spinSemana);
        controles.add(new JLabel("Año:"));
        controles.add(spinAnio);
        controles.add(btnVer);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(new ChartPanel(
            generarLineas(hoy.get(Calendar.WEEK_OF_YEAR), hoy.get(Calendar.YEAR))),
            BorderLayout.CENTER);

        btnVer.addActionListener(e -> {
            JFreeChart chart = generarLineas((int)spinSemana.getValue(), (int)spinAnio.getValue());
            contenedor.removeAll();
            contenedor.add(new ChartPanel(chart), BorderLayout.CENTER);
            contenedor.revalidate();
            contenedor.repaint();
        });

        panel.add(controles, BorderLayout.NORTH);
        panel.add(contenedor, BorderLayout.CENTER);
        return panel;
    }

    private JFreeChart generarLineas(int semana, int anio) {
        String[] dias = {"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};
        int[] diasCal = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < 7; i++) {
            int count = contarMesasDia(semana, anio, diasCal[i]);
            dataset.addValue(count, "Reservas", dias[i]);
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Reservas semana " + semana, "Día de la semana", "Número de reservas",
            dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(74, 63, 160));
        return chart;
    }

    // Mesa no tiene getFechaCreacion(), usamos las ventas de cafetería como proxy de reservas
    private int contarMesasDia(int semana, int anio, int diaSemana) {
        int count = 0;
        for (Venta v : cafe.getVentas()) {
            if (!"CAFETERIA".equals(v.getTipoVenta())) continue;
            Calendar cal = Calendar.getInstance();
            cal.setTime(v.getFecha());
            if (cal.get(Calendar.WEEK_OF_YEAR) == semana &&
                cal.get(Calendar.YEAR) == anio &&
                cal.get(Calendar.DAY_OF_WEEK) == diaSemana) {
                count++;
            }
        }
        return count;
    }

    private JPanel crearPanelInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Instrucciones"));

        JLabel info = new JLabel("<html><p style='width:220px; font-size:12px'>"
            + "<b>Pastel:</b> selecciona un juego y presiona Ver.<br><br>"
            + "<b>Barras:</b> selecciona la fecha inicial del rango de 5 días y presiona Ver.<br><br>"
            + "<b>Líneas:</b> selecciona la semana del año y presiona Ver.<br><br>"
            + "Las gráficas se generan con los datos actuales del sistema."
            + "</p></html>");
        info.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(info);
        panel.add(Box.createVerticalStrut(12));

        JButton btnRefrescar = new JButton("Refrescar todas");
        btnRefrescar.setBackground(new Color(74, 63, 160));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRefrescar.addActionListener(e -> construirGraficas());
        panel.add(btnRefrescar);

        return panel;
    }

    private void estilizarBtn(JButton btn) {
        btn.setBackground(new Color(74, 63, 160));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }
}
