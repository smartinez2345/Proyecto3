package presentacion.swing;

import modelo.Cafe;

import javax.swing.*;

public class MainSwing {

    private static final String RUTA_DATOS = "data/cafe_inicial.dat";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

        SwingUtilities.invokeLater(() -> {
            Cafe cafe = cargarCafe();
            new LoginWindow(cafe).setVisible(true);
        });
    }

    public static Cafe cargarCafe() {
        Cafe cafe = Cafe.cargarEstado(RUTA_DATOS);
        if (cafe == null) {
            cafe = new Cafe(50);
            modelo.Administrador admin = new modelo.Administrador("admin", "admin123");
            cafe.setAdministrador(admin);
        }
        return cafe;
    }

    public static void guardarCafe(Cafe cafe) {
        cafe.guardarEstado(RUTA_DATOS);
    }
}
