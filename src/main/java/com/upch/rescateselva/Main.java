package com.upch.rescateselva;

import com.upch.rescateselva.core.VentanaJuego;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        int nivel = 0;
        int x = 100;
        for (int i = 0; i + 1 < args.length; i++) {
            if (args[i].equals("--nivel")) {
                nivel = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("--x")) {
                x = Integer.parseInt(args[i + 1]);
            }
        }
        final int nivelInicial = nivel;
        final int xInicial = x;
        SwingUtilities.invokeLater(() -> new VentanaJuego(nivelInicial, xInicial));
    }
}
