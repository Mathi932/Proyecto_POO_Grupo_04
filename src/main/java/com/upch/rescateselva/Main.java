package com.upch.rescateselva;

import com.upch.rescateselva.core.VentanaJuego;
import javax.swing.SwingUtilities;

/** Punto de entrada: crea la ventana del juego. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaJuego::new);
    }
}
