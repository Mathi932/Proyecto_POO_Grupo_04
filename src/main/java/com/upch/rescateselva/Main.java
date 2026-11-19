package com.upch.rescateselva;

import com.upch.rescateselva.core.VentanaJuego;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaJuego::new);
    }
}
