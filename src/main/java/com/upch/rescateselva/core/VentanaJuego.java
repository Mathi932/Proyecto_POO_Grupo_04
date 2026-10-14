package com.upch.rescateselva.core;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * La ventana del juego. COMPOSICIÓN: la ventana crea y es dueña de su GamePanel.
 */
public class VentanaJuego extends JFrame {

    private static final long serialVersionUID = 1L;

    private final GamePanel panel;

    public VentanaJuego() {
        super("Rescate en la Selva");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
        panel.iniciarJuego();
    }
}
