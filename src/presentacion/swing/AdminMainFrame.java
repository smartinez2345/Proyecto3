package presentacion.swing;

import logica.*;
import modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminMainFrame extends JFrame {

    private Cafe cafe;
    private GestorUsuarios gestorUsuarios;
    private GestorInventario gestorInventario;
    private GestorTorneos gestorTorneos;
    private GestorTurnos gestorTurnos;

    public AdminMainFrame(Cafe cafe) {
        this.cafe = cafe;
        this.gestorUsuarios = new GestorUsuarios(cafe);
        this.gestorInventario = new GestorInventario(cafe);
        this.gestorTorneos = new GestorTorneos(cafe);
        this.gestorTurnos = new GestorTurnos(cafe);

        setTitle("Dulces & Dados — Administrador");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                guardarYSalir();
            }
        });

        construirUI();
    }

    private void construirUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("Empleados", crearPanelEmpleados());
        tabs.addTab("Inventario", crearPanelInventario());
        tabs.addTab("Torneos", crearPanelTorneos());
        tabs.addTab("Turnos", crearPanelTurnos());
        tabs.addTab("Gráficas", new GraficasPanel(cafe));
        add(tabs, BorderLayout.CENTER);

        JPanel barraInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalir = new JButton("Guardar y salir");
        btnSalir.setBackground(new Color(74, 63, 160));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.addActionListener(e -> guardarYSalir());
        barraInferior.add(btnSalir);
        add(barraInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelEmpleados() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Registrar nuevo empleado"));

        JTextField txtLogin = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"MESERO", "COCINERO"});
        JComboBox<String> cmbTurno = new JComboBox<>(new String[]{
            "LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","SABADO","DOMINGO"});

        formPanel.add(crearFilaForm("Login:", txtLogin));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(crearFilaForm("Contraseña:", txtPass));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(crearFilaForm("Tipo:", cmbTipo));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(crearFilaForm("Turno:", cmbTurno));
        formPanel.add(Box.createVerticalStrut(16));

        JButton btnRegistrar = new JButton("Registrar empleado");
        btnRegistrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegistrar.setBackground(new Color(74, 63, 160));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        formPanel.add(btnRegistrar);

        String[] columnas = {"Login", "Tipo", "Turno"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Empleados registrados"));
        cargarTablaEmpleados(modelo);

        btnRegistrar.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String tipo = (String) cmbTipo.getSelectedItem();
            String turno = (String) cmbTurno.getSelectedItem();
            if (login.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Login y contraseña son obligatorios.");
                return;
            }
            Empleado emp = gestorUsuarios.registrarEmpleado(login, pass, turno, tipo);
            if (emp != null) {
                JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.");
                txtLogin.setText(""); txtPass.setText("");
                cargarTablaEmpleados(modelo);
            } else {
                JOptionPane.showMessageDialog(this, "Ese login ya existe.");
            }
        });

        panel.add(formPanel);
        panel.add(scroll);
        return panel;
    }

    private void cargarTablaEmpleados(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Empleado e : cafe.getEmpleados()) {
            String tipo = e instanceof Cocinero ? "Cocinero" : "Mesero";
            modelo.addRow(new Object[]{e.getLogin(), tipo, e.getTurnoSemana()});
        }
    }

    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel juegoPanel = new JPanel();
        juegoPanel.setLayout(new BoxLayout(juegoPanel, BoxLayout.Y_AXIS));
        juegoPanel.setBorder(BorderFactory.createTitledBorder("Agregar juego (préstamo)"));

        JTextField txtNombre = new JTextField();
        JTextField txtCategoria = new JTextField();
        JTextField txtMinJ = new JTextField("2");
        JTextField txtMaxJ = new JTextField("4");
        JTextField txtEdad = new JTextField("8");
        JTextField txtCantidad = new JTextField("1");
        JCheckBox chkDificil = new JCheckBox("¿Es difícil?");

        juegoPanel.add(crearFilaForm("Nombre:", txtNombre));
        juegoPanel.add(Box.createVerticalStrut(6));
        juegoPanel.add(crearFilaForm("Categoría:", txtCategoria));
        juegoPanel.add(Box.createVerticalStrut(6));
        juegoPanel.add(crearFilaForm("Min jugadores:", txtMinJ));
        juegoPanel.add(Box.createVerticalStrut(6));
        juegoPanel.add(crearFilaForm("Max jugadores:", txtMaxJ));
        juegoPanel.add(Box.createVerticalStrut(6));
        juegoPanel.add(crearFilaForm("Edad mínima:", txtEdad));
        juegoPanel.add(Box.createVerticalStrut(6));
        juegoPanel.add(crearFilaForm("Cantidad:", txtCantidad));
        juegoPanel.add(Box.createVerticalStrut(6));
        juegoPanel.add(chkDificil);
        juegoPanel.add(Box.createVerticalStrut(12));

        JButton btnAgrJuego = new JButton("Agregar juego");
        btnAgrJuego.setBackground(new Color(74, 63, 160));
        btnAgrJuego.setForeground(Color.WHITE);
        btnAgrJuego.setFocusPainted(false);
        btnAgrJuego.setAlignmentX(Component.LEFT_ALIGNMENT);
        juegoPanel.add(btnAgrJuego);

        String[] cols = {"Juego", "Disponibles", "Estado"};
        DefaultTableModel modeloJuegos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaJuegos = new JTable(modeloJuegos);
        tablaJuegos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scrollJ = new JScrollPane(tablaJuegos);
        scrollJ.setBorder(BorderFactory.createTitledBorder("Inventario de juegos"));
        cargarTablaJuegos(modeloJuegos);

        btnAgrJuego.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String cat = txtCategoria.getText().trim();
                int minJ = Integer.parseInt(txtMinJ.getText().trim());
                int maxJ = Integer.parseInt(txtMaxJ.getText().trim());
                int edad = Integer.parseInt(txtEdad.getText().trim());
                int cant = Integer.parseInt(txtCantidad.getText().trim());
                boolean dificil = chkDificil.isSelected();
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
                    return;
                }
                // Constructor correcto: nombre, publicacion, empresaMatriz, categoria, min, max, edad, dificil
                Juego juego = new Juego(nombre, 0, "", cat, minJ, maxJ, edad, dificil);
                gestorInventario.agregarJuegoAPrestamo(juego, cant);
                JOptionPane.showMessageDialog(this, "Juego agregado correctamente.");
                txtNombre.setText("");
                cargarTablaJuegos(modeloJuegos);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Verifica que los campos numéricos sean válidos.");
            }
        });

        panel.add(juegoPanel);
        panel.add(scrollJ);
        return panel;
    }

    private void cargarTablaJuegos(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (InventarioJuegos inv : cafe.getInventariosJuegos()) {
            modelo.addRow(new Object[]{
                inv.getJuego().getNombre(),
                inv.getCantidadDisponible(),
                inv.getJuego().getEstado()
            });
        }
    }

    private JPanel crearPanelTorneos() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Crear torneo"));

        JTextField txtNombre = new JTextField();
        JComboBox<String> cmbDia = new JComboBox<>(new String[]{
            "LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","SABADO","DOMINGO"});
        JComboBox<String> cmbJuego = new JComboBox<>();
        actualizarComboJuegos(cmbJuego);
        JTextField txtParticipantes = new JTextField("10");
        JComboBox<String> cmbTipoTorneo = new JComboBox<>(new String[]{"AMISTOSO","COMPETITIVO"});
        JTextField txtPremio = new JTextField("0.15");

        formPanel.add(crearFilaForm2("Nombre:", txtNombre));
        formPanel.add(crearFilaForm2("Día:", cmbDia));
        formPanel.add(crearFilaForm2("Juego:", cmbJuego));
        formPanel.add(crearFilaForm2("Participantes:", txtParticipantes));
        formPanel.add(crearFilaForm2("Tipo:", cmbTipoTorneo));
        formPanel.add(crearFilaForm2("Premio (% o $):", txtPremio));

        JButton btnCrear = new JButton("Crear torneo");
        btnCrear.setBackground(new Color(74, 63, 160));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFocusPainted(false);
        formPanel.add(btnCrear);

        String[] cols = {"Nombre","Tipo","Día","Juego","Cupos disp.","Premio"};
        DefaultTableModel modeloTorneos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaTorneos = new JTable(modeloTorneos);
        tablaTorneos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(tablaTorneos);
        scroll.setBorder(BorderFactory.createTitledBorder("Torneos registrados"));
        cargarTablaTorneos(modeloTorneos);

        btnCrear.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String dia = (String) cmbDia.getSelectedItem();
                int idx = cmbJuego.getSelectedIndex();
                if (idx < 0 || cafe.getInventariosJuegos().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Agrega juegos al inventario primero.");
                    return;
                }
                Juego juego = cafe.getInventariosJuegos().get(idx).getJuego();
                int participantes = Integer.parseInt(txtParticipantes.getText().trim());
                double premio = Double.parseDouble(txtPremio.getText().trim());
                String tipo = (String) cmbTipoTorneo.getSelectedItem();
                boolean exito;
                if ("AMISTOSO".equals(tipo)) {
                    exito = gestorTorneos.crearTorneoAmistoso(nombre, dia, juego, participantes, premio) != null;
                } else {
                    exito = gestorTorneos.crearTorneoCompetitivo(nombre, dia, juego, participantes, premio) != null;
                }
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Torneo creado correctamente.");
                    txtNombre.setText("");
                    cargarTablaTorneos(modeloTorneos);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo crear el torneo. Verifica los datos.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Verifica los campos numéricos.");
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarComboJuegos(JComboBox<String> combo) {
        combo.removeAllItems();
        for (InventarioJuegos inv : cafe.getInventariosJuegos()) {
            combo.addItem(inv.getJuego().getNombre());
        }
    }

    private void cargarTablaTorneos(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Torneo t : cafe.getTorneos()) {
            String tipo = t.esAmistoso() ? "Amistoso" : "Competitivo";
            modelo.addRow(new Object[]{
                t.getNombre(), tipo, t.getDiaSemana(),
                t.getJuego().getNombre(),
                t.cuposTotalesDisponibles(),
                t.descripcionPremio()
            });
        }
    }

    private JPanel crearPanelTurnos() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Empleado","Tipo","Turno"};
        DefaultTableModel modeloTurnos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaTurnos = new JTable(modeloTurnos);
        tablaTurnos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(tablaTurnos);
        scroll.setBorder(BorderFactory.createTitledBorder("Turnos actuales"));
        cargarTablaTurnos(modeloTurnos);

        String[] colsSol = {"Solicitante","Reemplazo","Estado"};
        DefaultTableModel modeloSol = new DefaultTableModel(colsSol, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaSol = new JTable(modeloSol);
        tablaSol.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scrollSol = new JScrollPane(tablaSol);
        scrollSol.setBorder(BorderFactory.createTitledBorder("Solicitudes pendientes"));
        cargarTablaSolicitudes(modeloSol);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAprobar = new JButton("Aprobar seleccionada");
        JButton btnRechazar = new JButton("Rechazar seleccionada");
        btnAprobar.setBackground(new Color(15, 110, 86));
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.setFocusPainted(false);
        btnRechazar.setBackground(new Color(153, 60, 29));
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFocusPainted(false);

        btnAprobar.addActionListener(e -> {
            int fila = tablaSol.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona una solicitud."); return; }
            gestorTurnos.aprobarCambio(cafe.getSolicitudes().get(fila));
            cargarTablaTurnos(modeloTurnos);
            cargarTablaSolicitudes(modeloSol);
        });

        btnRechazar.addActionListener(e -> {
            int fila = tablaSol.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona una solicitud."); return; }
            gestorTurnos.rechazarCambio(cafe.getSolicitudes().get(fila));
            cargarTablaSolicitudes(modeloSol);
        });

        botones.add(btnAprobar);
        botones.add(btnRechazar);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, scrollSol);
        split.setDividerLocation(200);
        panel.add(split, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarTablaTurnos(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Empleado e : cafe.getEmpleados()) {
            String tipo = e instanceof Cocinero ? "Cocinero" : "Mesero";
            modelo.addRow(new Object[]{e.getLogin(), tipo, e.getTurnoSemana()});
        }
    }

    private void cargarTablaSolicitudes(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (SolicitudCambioTurno s : cafe.getSolicitudes()) {
            modelo.addRow(new Object[]{
                s.getSolicitante().getLogin(),
                s.getReemplazo().getLogin(),
                s.getEstado()
            });
        }
    }

    private JPanel crearFilaForm(String label, JComponent campo) {
        JPanel fila = new JPanel(new BorderLayout(6, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(120, 28));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        fila.add(lbl, BorderLayout.WEST);
        fila.add(campo, BorderLayout.CENTER);
        return fila;
    }

    private JPanel crearFilaForm2(String label, JComponent campo) {
        JPanel fila = new JPanel(new BorderLayout(4, 2));
        fila.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        fila.add(lbl, BorderLayout.NORTH);
        fila.add(campo, BorderLayout.CENTER);
        return fila;
    }

    private void guardarYSalir() {
        MainSwing.guardarCafe(cafe);
        dispose();
        new LoginWindow(cafe).setVisible(true);
    }
}
