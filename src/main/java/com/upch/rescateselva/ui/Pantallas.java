package com.upch.rescateselva.ui;

import com.upch.rescateselva.recursos.Fuente;
import com.upch.rescateselva.recursos.Sprites;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public final class Pantallas {
    private static final int ANCHO = 960;
    private static final int ALTO = 540;
    private static final int SUELO = 470;

    private static final Color FONDO_CAJA = new Color(0x16231c);
    private static final Color BORDE = new Color(0x5f8a6c);
    private static final Color CLARO = new Color(0xf4ead0);
    private static final Color AMARILLO = new Color(0xffd96b);
    private static final Color VERDE = new Color(0x9fe0b4);

    private Pantallas() {
    }

    public static void caja(Graphics2D g, int x, int y, int w, int h, boolean resaltada) {
        g.setColor(resaltada ? AMARILLO : BORDE);
        g.fillRect(x, y, w, h);
        g.setColor(FONDO_CAJA);
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
    }

    private static void suelo(Graphics2D g) {
        Sprites.mosaico(g, Sprites.cargar("hierba.png"), 0, SUELO, ANCHO, 32);
        Sprites.mosaico(g, Sprites.cargar("tierra.png"), 0, SUELO + 32, ANCHO, ALTO - SUELO - 32);
    }

    private static void personajes(Graphics2D g) {
        String[] archivos = {"amaru.png", "jaguar.png", "mono.png", "tucan.png"};
        int[] centros = {150, 735, 830, 915};
        for (int i = 0; i < archivos.length; i++) {
            BufferedImage img = Sprites.cargarCuadros(archivos[i], archivos[i].equals("amaru.png") ? 5 : 2)[0];
            Sprites.dibujar(g, img, centros[i] - img.getWidth() * 2, SUELO - img.getHeight() * 4, 4);
        }
    }

    public static void menu(Graphics2D g, int opcion, String[] opciones) {
        suelo(g);
        personajes(g);
        Fuente.centrado(g, "KAWSAY", ANCHO / 2, 78, 24, AMARILLO);
        Fuente.centrado(g, "RESCATE EN LA SELVA", ANCHO / 2, 140, 40, CLARO);
        for (int i = 0; i < opciones.length; i++) {
            int y = 214 + i * 72;
            boolean elegida = i == opcion;
            caja(g, 300, y, 360, 56, elegida);
            Fuente.centrado(g, opciones[i], ANCHO / 2, y + 40, 24, elegida ? Color.WHITE : VERDE);
            if (elegida) {
                Fuente.texto(g, ">", 322, y + 40, 24, AMARILLO);
            }
        }
        Fuente.centrado(g, "Arriba y abajo para elegir - Enter para aceptar", ANCHO / 2, ALTO - 20, 16, CLARO);
    }

    public static void controles(Graphics2D g) {
        caja(g, 50, 30, ANCHO - 100, 420, false);
        Fuente.centrado(g, "CONTROLES", ANCHO / 2, 96, 32, AMARILLO);
        String[][] filas = {
            {"Flechas / A D", "Moverse"},
            {"Arriba / W / Espacio", "Saltar"},
            {"E (mantener)", "Liberar al animal"},
            {"F", "Lanzar piedra"},
            {"P / Esc", "Pausa"},
            {"N", "Silenciar la música"},
            {"- / +", "Volumen de la música"}
        };
        for (int i = 0; i < filas.length; i++) {
            int y = 156 + i * 36;
            Fuente.texto(g, filas[i][0], 90, y, 16, AMARILLO);
            Fuente.texto(g, filas[i][1], 500, y, 16, CLARO);
        }
        Fuente.centrado(g, "Las piedras se recogen al pasar encima", ANCHO / 2, 430, 16, VERDE);
        Fuente.centrado(g, "Enter para volver", ANCHO / 2, ALTO - 30, 16, CLARO);
    }

    public static void historia(Graphics2D g, String texto, int pagina, int total) {
        caja(g, 90, 70, ANCHO - 180, 350, false);
        Fuente.parrafo(g, texto, 130, 150, ANCHO - 260, 44, 24, CLARO);
        Fuente.centrado(g, "Página " + (pagina + 1) + " de " + total, ANCHO / 2, 395, 16, VERDE);
        Fuente.centrado(g, "Enter para continuar", ANCHO / 2, ALTO - 50, 16, CLARO);
    }

    public static void nivelCompleto(Graphics2D g, boolean ultimo, List<String> lineas, String tiempo) {
        int h = 190 + lineas.size() * 34;
        int y = (ALTO - h) / 2;
        caja(g, 100, y, ANCHO - 200, h, true);
        Fuente.centrado(g, ultimo ? "¡LA SERPIENTE HUYE!" : "¡NIVEL COMPLETADO!", ANCHO / 2, y + 76, 32, AMARILLO);
        int ly = y + 124;
        for (String linea : lineas) {
            Fuente.centrado(g, linea, ANCHO / 2, ly, 16, CLARO);
            ly += 34;
        }
        Fuente.centrado(g, "Tiempo: " + tiempo, ANCHO / 2, ly + 20, 24, VERDE);
    }

    public static void finPartida(Graphics2D g, boolean victoria, String tiempo) {
        g.setColor(victoria ? new Color(0x2a6b4a) : new Color(0x3a1f1f));
        g.fillRect(0, 0, ANCHO, ALTO);
        if (victoria) {
            Fuente.centrado(g, "¡VICTORIA!", ANCHO / 2, 110, 48, Color.WHITE);
            caja(g, 160, 150, ANCHO - 320, 170, false);
            Fuente.parrafo(g, "La Serpiente huye asustada y Los Extractores pierden su guarida. "
                    + "Tucán, Mono Bebé y Jaguar Cachorro vuelven a casa gracias a Amaru.", 196, 206, ANCHO - 392, 32,
                    16, CLARO);
        } else {
            Fuente.centrado(g, "GAME OVER", ANCHO / 2, 170, 56, new Color(0xffd0c0));
            Fuente.centrado(g, "Los animales siguen necesitando ayuda...", ANCHO / 2, 230, 16, Color.WHITE);
        }
        Fuente.centrado(g, "Tiempo total: " + tiempo, ANCHO / 2, victoria ? 380 : 320, 24, AMARILLO);
        Fuente.centrado(g, "Enter para volver al menú", ANCHO / 2, ALTO - 20, 16, CLARO);
    }

    public static void pausa(Graphics2D g, String musica) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, ANCHO, ALTO);
        caja(g, 180, 150, 600, 240, false);
        Fuente.centrado(g, "PAUSA", ANCHO / 2, 240, 48, CLARO);
        Fuente.centrado(g, "P continuar  -  M menú principal", ANCHO / 2, 300, 16, VERDE);
        Fuente.centrado(g, musica, ANCHO / 2, 350, 16, CLARO);
    }
}