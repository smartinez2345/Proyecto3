package presentacion.swing;

import logica.GestorUsuarios;
import modelo.Cafe;

import javax.swing.*;
import java.awt.*;

public class RegistroClienteDialog extends JDialog {

    private Cafe cafe;
    private JTextField txtLogin;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;
    private JLabel lblError;

    public RegistroClienteDialog(JFrame parent, Cafe cafe) {
        super(parent, "Registro de cliente", true);
        this.cafe = cafe;
        setSize(340, 310);
        setLocationRelativeTo(parent);
        setResizable(false);
        construirUI();
    }

    private void construirUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        panel.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Crear cuenta de cliente");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(18));

        panel.add(crearLabel("Nombre de usuario (login)"));
        panel.add(Box.createVerticalStrut(4));
        txtLogin = new JTextField();
        txtLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(txtLogin);
        panel.add(Box.createVerticalStrut(10));

        panel.add(crearLabel("Contraseña"));
        panel.add(Box.createVerticalStrut(4));
        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(10));

        panel.add(crearLabel("Confirmar contraseña"));
        panel.add(Box.createVerticalStrut(4));
        txtConfirm = new JPasswordField();
        txtConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(txtConfirm);
        panel.add(Box.createVerticalStrut(12));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblError.setForeground(new Color(180, 30, 30));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblError);
        panel.add(Box.createVerticalStrut(8));

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 8, 0));
        panelBotones.setOpaque(false);
        panelBotones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.setBackground(new Color(74, 63, 160));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(e -> registrar());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnRegistrar);
        panel.add(panelBotones);

        add(panel);
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void registrar() {
        String login = txtLogin.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (login.isEmpty() || pass.isEmpty()) {
            lblError.setText("Login y contraseña son obligatorios.");
            return;
        }
        if (!pass.equals(confirm)) {
            lblError.setText("Las contraseñas no coinciden.");
            return;
        }

        GestorUsuarios gestor = new GestorUsuarios(cafe);
        if (gestor.registrarCliente(login, pass) != null) {
            JOptionPane.showMessageDialog(this,
                "¡Cliente registrado exitosamente!\nYa puedes iniciar sesión.",
                "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            lblError.setText("Ese nombre de usuario ya existe.");
        }
    }
}
