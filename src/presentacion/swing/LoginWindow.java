package presentacion.swing;

import logica.*;
import modelo.*;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {

    private Cafe cafe;
    private int intentos = 0;

    private JTextField txtLogin;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginWindow(Cafe cafe) {
        this.cafe = cafe;
        setTitle("Dulces & Dados");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        construirUI();
    }

    private void construirUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Dulces & Dados");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setForeground(new Color(74, 63, 160));

        JLabel lblSubtitulo = new JLabel("Board Game Café");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitulo.setForeground(Color.GRAY);

        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblSubtitulo);
        panel.add(Box.createVerticalStrut(28));

        panel.add(crearLabel("Usuario"));
        panel.add(Box.createVerticalStrut(4));
        txtLogin = new JTextField();
        txtLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(txtLogin);
        panel.add(Box.createVerticalStrut(12));

        panel.add(crearLabel("Contraseña"));
        panel.add(Box.createVerticalStrut(4));
        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(20));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblError.setForeground(new Color(180, 30, 30));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblError);
        panel.add(Box.createVerticalStrut(8));

        JButton btnLogin = new JButton("Iniciar sesión");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.setBackground(new Color(74, 63, 160));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> iniciarSesion());
        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(10));

        JButton btnRegistrar = new JButton("Registrarme como cliente");
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRegistrar.setBackground(Color.WHITE);
        btnRegistrar.setForeground(new Color(74, 63, 160));
        btnRegistrar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegistrar.addActionListener(e -> abrirRegistro());
        panel.add(btnRegistrar);

        txtPassword.addActionListener(e -> iniciarSesion());
        add(panel);
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void iniciarSesion() {
        String login = txtLogin.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (login.isEmpty() || password.isEmpty()) {
            lblError.setText("Ingresa usuario y contraseña.");
            return;
        }

        GestorUsuarios gestorUsuarios = new GestorUsuarios(cafe);
        Usuario usuario = gestorUsuarios.login(login, password);

        if (usuario != null) {
            lblError.setText(" ");
            abrirVentanaSegunRol(usuario);
        } else {
            intentos++;
            if (intentos >= 3) {
                JOptionPane.showMessageDialog(this,
                    "Demasiados intentos fallidos. La aplicación se cerrará.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                MainSwing.guardarCafe(cafe);
                System.exit(0);
            }
            lblError.setText("Credenciales incorrectas. Intento " + intentos + " de 3.");
            txtPassword.setText("");
        }
    }

    private void abrirVentanaSegunRol(Usuario usuario) {
        dispose();
        if (usuario instanceof Administrador) {
            new AdminMainFrame(cafe).setVisible(true);
        } else if (usuario instanceof Empleado) {
            new EmpleadoMainFrame(cafe, (Empleado) usuario).setVisible(true);
        } else if (usuario instanceof Cliente) {
            new ClienteMainFrame(cafe, (Cliente) usuario).setVisible(true);
        }
    }

    private void abrirRegistro() {
        RegistroClienteDialog dialog = new RegistroClienteDialog(this, cafe);
        dialog.setVisible(true);
    }
}
