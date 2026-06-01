package presentacion.swing;

import logica.*;
import modelo.*;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClienteMainFrame extends JFrame {

    private Cafe cafe;
    private Cliente cliente;
    private GestorTorneos gestorTorneos;
    private GestorInventario gestorInventario;

    public ClienteMainFrame(Cafe cafe, Cliente cliente) {
        this.cafe = cafe;
        this.cliente = cliente;
        this.gestorTorneos = new GestorTorneos(cafe);
        this.gestorInventario = new GestorInventario(cafe);

        setTitle("Dulces & Dados — Cliente: " + cliente.getLogin());
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
        JPanel barraInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraInfo.setBackground(new Color(153, 60, 29));
        JLabel lblInfo = new JLabel("  Cliente: " + cliente.getLogin()
            + "   |   Puntos: " + cliente.getPuntosFidelidad());
        lblInfo.setForeground(Color.WHITE);
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 13));
        barraInfo.add(lblInfo);
        add(barraInfo, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("Torneos", crearPanelTorneos());
        tabs.addTab("Mis bonos", crearPanelBonos());
        tabs.addTab("Mis favoritos", crearPanelFavoritos());
        add(tabs, BorderLayout.CENTER);

        JPanel barraInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalir = new JButton("Guardar y salir");
        btnSalir.setBackground(new Color(153, 60, 29));
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

        String[] cols = {"Nombre", "Tipo", "Día", "Juego", "Cupos disp.", "Premio"};
        DefaultTableModel modeloTorneos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaTorneos = new JTable(modeloTorneos);
        tablaTorneos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaTorneos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tablaTorneos);
        scroll.setBorder(BorderFactory.createTitledBorder("Torneos disponibles"));
        cargarTablaTorneos(modeloTorneos);

        JPanel inscPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inscPanel.add(new JLabel("Participantes (1-3):"));
        JSpinner spinnerPart = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
        inscPanel.add(spinnerPart);

        JButton btnInscribir = new JButton("Inscribirme");
        btnInscribir.setBackground(new Color(153, 60, 29));
        btnInscribir.setForeground(Color.WHITE);
        btnInscribir.setFocusPainted(false);

        JButton btnDesinscribir = new JButton("Desinscribirme");
        btnDesinscribir.setFocusPainted(false);

        inscPanel.add(btnInscribir);
        inscPanel.add(btnDesinscribir);

        btnInscribir.addActionListener(e -> {
            int fila = tablaTorneos.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un torneo."); return; }
            Torneo torneo = cafe.getTorneos().get(fila);
            int cantidad = (int) spinnerPart.getValue();
            boolean ok = gestorTorneos.inscribirCliente(torneo, cliente, cantidad);
            if (ok) JOptionPane.showMessageDialog(this, "¡Inscripción exitosa!");
            else JOptionPane.showMessageDialog(this, "No se pudo inscribir. Verifica cupos o si ya estás inscrito.");
            cargarTablaTorneos(modeloTorneos);
        });

        btnDesinscribir.addActionListener(e -> {
            int fila = tablaTorneos.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un torneo."); return; }
            Torneo torneo = cafe.getTorneos().get(fila);
            boolean ok = gestorTorneos.desinscribirCliente(torneo, cliente);
            if (ok) JOptionPane.showMessageDialog(this, "Desinscripción exitosa.");
            else JOptionPane.showMessageDialog(this, "No estás inscrito en ese torneo.");
            cargarTablaTorneos(modeloTorneos);
        });

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(inscPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarTablaTorneos(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Torneo t : cafe.getTorneos()) {
            boolean esFan = cliente.esFanaticoDeJuego(t.getJuego());
            modelo.addRow(new Object[]{
                t.getNombre() + (esFan ? " ★" : ""),
                t.esAmistoso() ? "Amistoso" : "Competitivo",
                t.getDiaSemana(),
                t.getJuego().getNombre(),
                t.cuposTotalesDisponibles(),
                t.descripcionPremio()
            });
        }
    }

    // ── PANEL BONOS ──────────────────────────────────────────────────
    private JPanel crearPanelBonos() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titulo = new JLabel("Mis bonos de descuento acumulados:");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(titulo, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> listaBonos = new JList<>(listModel);
        listaBonos.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(listaBonos);

        cargarBonos(listModel);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarBonos(listModel));

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnRefrescar, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarBonos(DefaultListModel<String> modelo) {
        modelo.clear();
        List<Double> bonos = cliente.getBonosDescuento();
        if (bonos.isEmpty()) {
            modelo.addElement("No tienes bonos de descuento activos.");
        } else {
            for (int i = 0; i < bonos.size(); i++) {
                modelo.addElement("Bono #" + (i + 1) + " — " + (int)(bonos.get(i) * 100) + "% de descuento");
            }
        }
    }

    // ── PANEL FAVORITOS ──────────────────────────────────────────────
    private JPanel crearPanelFavoritos() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Lista de favoritos actuales
        DefaultListModel<String> modeloFav = new DefaultListModel<>();
        JList<String> listaFav = new JList<>(modeloFav);
        listaFav.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scrollFav = new JScrollPane(listaFav);
        scrollFav.setBorder(BorderFactory.createTitledBorder("Mis juegos favoritos"));
        cargarFavoritos(modeloFav);

        // Lista para agregar favoritos
        String[] cols = {"Juego", "Categoría"};
        DefaultTableModel modeloJuegos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaJuegos = new JTable(modeloJuegos);
        tablaJuegos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaJuegos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollJ = new JScrollPane(tablaJuegos);
        scrollJ.setBorder(BorderFactory.createTitledBorder("Agregar a favoritos"));

        for (InventarioJuegos inv : cafe.getInventariosJuegos()) {
            modeloJuegos.addRow(new Object[]{
                inv.getJuego().getNombre(),
                inv.getJuego().getCategoria()
            });
        }

        JButton btnAgregar = new JButton("★ Agregar a favoritos");
        btnAgregar.setBackground(new Color(153, 60, 29));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);

        btnAgregar.addActionListener(e -> {
            int fila = tablaJuegos.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un juego."); return; }
            Juego juego = cafe.getInventariosJuegos().get(fila).getJuego();
            cliente.agregarJuegoFavorito(juego);
            JOptionPane.showMessageDialog(this, juego.getNombre() + " agregado a favoritos.");
            cargarFavoritos(modeloFav);
        });

        JPanel derechaPanel = new JPanel(new BorderLayout(0, 8));
        derechaPanel.add(scrollJ, BorderLayout.CENTER);
        derechaPanel.add(btnAgregar, BorderLayout.SOUTH);

        panel.add(scrollFav);
        panel.add(derechaPanel);
        return panel;
    }

    private void cargarFavoritos(DefaultListModel<String> modelo) {
        modelo.clear();
        if (cliente.getJuegosFavoritos().isEmpty()) {
            modelo.addElement("No tienes juegos favoritos aún.");
        } else {
            for (Juego j : cliente.getJuegosFavoritos()) {
                modelo.addElement("★ " + j.getNombre());
            }
        }
    }

    private void guardarYSalir() {
        MainSwing.guardarCafe(cafe);
        dispose();
        new LoginWindow(cafe).setVisible(true);
    }
}
