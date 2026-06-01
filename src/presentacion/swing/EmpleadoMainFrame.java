package presentacion.swing;

import logica.*;
import modelo.*;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EmpleadoMainFrame extends JFrame {

    private Cafe cafe;
    private Empleado empleado;
    private GestorTorneos gestorTorneos;
    private GestorTurnos gestorTurnos;
    private GestorPrestamos gestorPrestamos;

    public EmpleadoMainFrame(Cafe cafe, Empleado empleado) {
        this.cafe = cafe;
        this.empleado = empleado;
        this.gestorTorneos = new GestorTorneos(cafe);
        this.gestorTurnos = new GestorTurnos(cafe);
        this.gestorPrestamos = new GestorPrestamos(cafe);

        setTitle("Dulces & Dados — Empleado: " + empleado.getLogin());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(750, 550);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                guardarYSalir();
            }
        });

        construirUI();
    }

    private void construirUI() {
        // Barra superior con info del turno
        JPanel barraInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraInfo.setBackground(new Color(15, 110, 86));
        JLabel lblInfo = new JLabel("  Empleado: " + empleado.getLogin()
            + "   |   Turno: " + empleado.getTurnoSemana());
        lblInfo.setForeground(Color.WHITE);
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 13));
        barraInfo.add(lblInfo);
        add(barraInfo, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("Torneos", crearPanelTorneos());
        tabs.addTab("Cambio de turno", crearPanelTurnos());
        tabs.addTab("Préstamos", crearPanelPrestamos());
        add(tabs, BorderLayout.CENTER);

        JPanel barraInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalir = new JButton("Guardar y salir");
        btnSalir.setBackground(new Color(15, 110, 86));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.addActionListener(e -> guardarYSalir());
        barraInferior.add(btnSalir);
        add(barraInferior, BorderLayout.SOUTH);
    }

    // ── PANEL TORNEOS ────────────────────────────────────────────────
    private JPanel crearPanelTorneos() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Tabla de torneos disponibles
        String[] cols = {"Nombre", "Tipo", "Día", "Juego", "Cupos disp.", "Premio"};
        DefaultTableModel modeloDisp = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaDisp = new JTable(modeloDisp);
        tablaDisp.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaDisp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollDisp = new JScrollPane(tablaDisp);
        scrollDisp.setBorder(BorderFactory.createTitledBorder("Torneos disponibles"));
        cargarTablaTorneos(modeloDisp);

        // Panel inscripción
        JPanel inscPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inscPanel.add(new JLabel("Participantes (1-3):"));
        JSpinner spinnerPart = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
        inscPanel.add(spinnerPart);

        JButton btnInscribir = new JButton("Inscribirme");
        btnInscribir.setBackground(new Color(15, 110, 86));
        btnInscribir.setForeground(Color.WHITE);
        btnInscribir.setFocusPainted(false);

        JButton btnDesinscribir = new JButton("Desinscribirme");
        btnDesinscribir.setBackground(new Color(153, 60, 29));
        btnDesinscribir.setForeground(Color.WHITE);
        btnDesinscribir.setFocusPainted(false);

        inscPanel.add(btnInscribir);
        inscPanel.add(btnDesinscribir);

        btnInscribir.addActionListener(e -> {
            int fila = tablaDisp.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un torneo."); return; }
            Torneo torneo = cafe.getTorneos().get(fila);
            int cantidad = (int) spinnerPart.getValue();
            boolean ok = gestorTorneos.inscribirEmpleado(torneo, empleado, cantidad);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Inscripción exitosa.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo inscribir. Verifica que no tengas turno ese día.");
            }
            cargarTablaTorneos(modeloDisp);
        });

        btnDesinscribir.addActionListener(e -> {
            int fila = tablaDisp.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un torneo."); return; }
            Torneo torneo = cafe.getTorneos().get(fila);
            boolean ok = gestorTorneos.desinscribirEmpleado(torneo, empleado);
            if (ok) JOptionPane.showMessageDialog(this, "Desinscripción exitosa.");
            else JOptionPane.showMessageDialog(this, "No estás inscrito en ese torneo.");
            cargarTablaTorneos(modeloDisp);
        });

        panel.add(scrollDisp, BorderLayout.CENTER);
        panel.add(inscPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarTablaTorneos(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Torneo t : cafe.getTorneos()) {
            modelo.addRow(new Object[]{
                t.getNombre(),
                t.esAmistoso() ? "Amistoso" : "Competitivo",
                t.getDiaSemana(),
                t.getJuego().getNombre(),
                t.cuposTotalesDisponibles(),
                t.descripcionPremio()
            });
        }
    }

    // ── PANEL TURNOS ─────────────────────────────────────────────────
    private JPanel crearPanelTurnos() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Login", "Tipo", "Turno"};
        DefaultTableModel modeloEmp = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaEmp = new JTable(modeloEmp);
        tablaEmp.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaEmp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tablaEmp);
        scroll.setBorder(BorderFactory.createTitledBorder("Selecciona un empleado para intercambiar turno"));

        for (Empleado e : cafe.getEmpleados()) {
            if (!e.getLogin().equals(empleado.getLogin())) {
                String tipo = e instanceof Cocinero ? "Cocinero" : "Mesero";
                modeloEmp.addRow(new Object[]{e.getLogin(), tipo, e.getTurnoSemana()});
            }
        }

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSolicitar = new JButton("Solicitar cambio de turno");
        btnSolicitar.setBackground(new Color(15, 110, 86));
        btnSolicitar.setForeground(Color.WHITE);
        btnSolicitar.setFocusPainted(false);
        botones.add(btnSolicitar);

        btnSolicitar.addActionListener(e -> {
            int fila = tablaEmp.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un empleado."); return; }
            // Buscar empleado seleccionado (saltamos al propio empleado en la lista)
            java.util.List<Empleado> otros = new java.util.ArrayList<>();
            for (Empleado emp : cafe.getEmpleados()) {
                if (!emp.getLogin().equals(empleado.getLogin())) otros.add(emp);
            }
            Empleado reemplazo = otros.get(fila);
            SolicitudCambioTurno sol = gestorTurnos.solicitarCambio(empleado, reemplazo);
            if (sol != null) JOptionPane.showMessageDialog(this, "Solicitud enviada al administrador.");
            else JOptionPane.showMessageDialog(this, "No se pudo enviar la solicitud.");
        });

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ── PANEL PRÉSTAMOS ──────────────────────────────────────────────
    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel info = new JLabel("<html><b>Préstamo de juego para empleado</b><br>"
            + "Solo puedes pedir prestado si no estás en turno activo.</html>");
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(info, BorderLayout.NORTH);

        String[] cols = {"Juego", "Disponibles"};
        DefaultTableModel modeloJuegos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaJuegos = new JTable(modeloJuegos);
        tablaJuegos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaJuegos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        for (InventarioJuegos inv : cafe.getInventariosJuegos()) {
            if (inv.estaDisponible()) {
                modeloJuegos.addRow(new Object[]{inv.getJuego().getNombre(), inv.getCantidadDisponible()});
            }
        }

        JScrollPane scroll = new JScrollPane(tablaJuegos);
        scroll.setBorder(BorderFactory.createTitledBorder("Juegos disponibles para préstamo"));

        JButton btnPedir = new JButton("Pedir préstamo");
        btnPedir.setBackground(new Color(15, 110, 86));
        btnPedir.setForeground(Color.WHITE);
        btnPedir.setFocusPainted(false);

        btnPedir.addActionListener(e -> {
            int fila = tablaJuegos.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un juego."); return; }
            if (empleado.estaEnTurno()) {
                JOptionPane.showMessageDialog(this, "No puedes pedir prestado mientras estás en turno.");
                return;
            }
            Juego juego = cafe.getInventariosJuegos().get(fila).getJuego();
            Prestamo p = gestorPrestamos.iniciarPrestamoEmpleado(empleado);
            if (p != null) {
                boolean ok = gestorPrestamos.agregarJuegoAPrestamoEmpleado(p, juego, empleado);
                if (ok) JOptionPane.showMessageDialog(this, "Préstamo iniciado para: " + juego.getNombre());
                else JOptionPane.showMessageDialog(this, "No se pudo agregar el juego.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo iniciar el préstamo.");
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnPedir);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void guardarYSalir() {
        MainSwing.guardarCafe(cafe);
        dispose();
        new LoginWindow(cafe).setVisible(true);
    }
}
