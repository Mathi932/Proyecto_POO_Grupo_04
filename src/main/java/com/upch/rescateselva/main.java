package com.upch.rescateselva;

import com.upch.rescateselva.core.VentanaJuego;
import javax.swing.SwingUtilities;

/** Punto de entrada: crea la ventana del juego. */
public class main {

    public static void Main(String[] args) {
        SwingUtilities.invokeLater(VentanaJuego::new);
    }
}
