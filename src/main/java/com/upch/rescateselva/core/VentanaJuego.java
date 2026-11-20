package com.upch.rescateselva.core;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class VentanaJuego extends JFrame {
    private static final long serialVersionUID = 1L;

    private final GamePanel panel;

    public VentanaJuego() {
        this(0, 0);
    }

    public VentanaJuego(int nivelInicial, int xInicial) {
        super("Rescate en la Selva");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        panel = new GamePanel();
        if (nivelInicial >= 1) {
            panel.iniciarEnNivel(nivelInicial, xInicial);
        }
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
        panel.iniciarJuego();
    }
}
