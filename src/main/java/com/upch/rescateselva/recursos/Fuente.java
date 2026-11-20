package com.upch.rescateselva.recursos;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

public final class Fuente {
    private static Font base;
    private static boolean intentada;

    private Fuente() {
    }

    public static synchronized Font de(float tamano) {
        if (!intentada) {
            intentada = true;
            try (InputStream in = Fuente.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf")) {
                if (in != null) {
                    base = Font.createFont(Font.TRUETYPE_FONT, in);
                }
            } catch (FontFormatException | IOException e) {
                base = null;
            }
        }
        float ajustado = Math.max(8f, Math.round(tamano / 8f) * 8f);
        return base != null ? base.deriveFont(ajustado) : new Font(Font.MONOSPACED, Font.BOLD, Math.round(tamano));
    }

    public static void nitido(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    public static int ancho(Graphics2D g, String texto, float tamano) {
        FontMetrics fm = g.getFontMetrics(de(tamano));
        return fm.stringWidth(texto);
    }

    public static void texto(Graphics2D g, String texto, int x, int baseY, float tamano, Color color) {
        g.setFont(de(tamano));
        g.setColor(new Color(0, 0, 0, Math.min(190, color.getAlpha())));
        g.drawString(texto, x + 2, baseY + 2);
        g.setColor(color);
        g.drawString(texto, x, baseY);
    }

    public static void centrado(Graphics2D g, String texto, int centroX, int baseY, float tamano, Color color) {
        texto(g, texto, centroX - ancho(g, texto, tamano) / 2, baseY, tamano, color);
    }

    public static void derecha(Graphics2D g, String texto, int bordeX, int baseY, float tamano, Color color) {
        texto(g, texto, bordeX - ancho(g, texto, tamano), baseY, tamano, color);
    }

    public static java.util.List<String> lineas(Graphics2D g, String texto, int anchoMax, float tamano) {
        java.util.List<String> res = new java.util.ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
            if (ancho(g, prueba, tamano) > anchoMax && linea.length() > 0) {
                res.add(linea.toString());
                linea = new StringBuilder(palabra);
            } else {
                linea = new StringBuilder(prueba);
            }
        }
        res.add(linea.toString());
        return res;
    }

    public static int parrafo(Graphics2D g, String texto, int x, int y, int anchoMax, int interlineado,
            float tamano, Color color) {
        StringBuilder linea = new StringBuilder();
        int yy = y;
        for (String palabra : texto.split(" ")) {
            String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
            if (ancho(g, prueba, tamano) > anchoMax && linea.length() > 0) {
                texto(g, linea.toString(), x, yy, tamano, color);
                yy += interlineado;
                linea = new StringBuilder(palabra);
            } else {
                linea = new StringBuilder(prueba);
            }
        }
        texto(g, linea.toString(), x, yy, tamano, color);
        return yy;
    }
}
