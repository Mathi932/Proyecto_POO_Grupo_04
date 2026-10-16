package com.upch.rescateselva.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * Interfaz sobre la pantalla: tiempo total de juego, barra de vida, intentos, piedras,
 * título del nivel (arriba al centro), objetivo (arriba a la derecha),
 * cronómetro y mensajes.
 *
 * Hereda de JPanel. GamePanel le pasa los datos en cada frame (tiempo, vida, intentos...)
 * y le pide que se dibuje encima del juego con dibujar().
 */
public class HUD extends JPanel {

    private static final long serialVersionUID = 1L;

    private int tiempoTotal;
    private int vida = 100;
    private int intentos = 3;
    private int piedras;
    private int segundos = -1;
    private boolean agotado;
    private String titulo = "";
    private String objetivo = "";
    private String mensaje;
    private int mensajeFrames;
    private int bannerFrames;
    private int tick;

    public HUD() {
        setOpaque(false);
    }

    /** Tiempo total de juego (en segundos) y vida actual del jugador. */
    public void setEstadoJugador(int segundos, int vida) {
        this.tiempoTotal = segundos;
        this.vida = vida;
    }

    // ---- datos extra que le pasa GamePanel cada frame ----

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public void setPiedras(int piedras) {
        this.piedras = piedras;
    }

    public void setCronometro(int segundos, boolean agotado) {
        this.segundos = segundos;
        this.agotado = agotado;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        this.bannerFrames = 170;
    }

    public void mostrarMensaje(String texto, int frames) {
        this.mensaje = texto;
        this.mensajeFrames = frames;
    }

    /** Avanza los contadores de mensajes (una vez por frame de juego). */
    public void avanzar() {
        tick++;
        if (mensajeFrames > 0) {
            mensajeFrames--;
        }
        if (bannerFrames > 0) {
            bannerFrames--;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        dibujar((Graphics2D) g, getWidth(), getHeight());
    }

    public void dibujar(Graphics2D g, int w, int h) {
        // ---- panel izquierdo: tiempo total, vida, intentos, piedras ----
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(12, 12, 230, 84, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString(String.format("Tiempo: %02d:%02d", tiempoTotal / 60, tiempoTotal % 60), 24, 34);

        int bw = 150;
        g.setColor(new Color(60, 20, 20));
        g.fillRoundRect(24, 44, bw, 14, 8, 8);
        Color colorVida = vida > 50 ? new Color(0x4ec27a) : (vida > 25 ? new Color(0xe5b93c) : new Color(0xe5513c));
        g.setColor(colorVida);
        g.fillRoundRect(24, 44, (int) (bw * Math.max(0, vida) / 100.0), 14, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(24, 44, bw, 14, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString(String.valueOf(Math.max(0, vida)), 24 + bw + 8, 56);

        for (int i = 0; i < 3; i++) {
            dibujarCorazon(g, 26 + i * 22, 66, i < intentos);
        }
        g.setColor(new Color(0xb8b8b0));
        g.fillOval(106, 70, 14, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("x" + piedras + "  [F]", 124, 81);

        // ---- título del nivel (arriba al centro) ----
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(titulo);
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(w / 2 - tw / 2 - 14, 12, tw + 28, 30, 14, 14);
        g.setColor(Color.WHITE);
        g.drawString(titulo, w / 2 - tw / 2, 33);

        // ---- objetivo (arriba a la derecha) ----
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        fm = g.getFontMetrics();
        String linea = "Objetivo: " + objetivo;
        int ow = fm.stringWidth(linea);
        // si el título es muy largo y chocaría con el objetivo, el objetivo baja una fila
        int filaY = (w / 2 + tw / 2 + 14 > w - ow - 44) ? 50 : 12;
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(w - ow - 38, filaY, ow + 26, 30, 14, 14);
        g.setColor(new Color(0xffe27a));
        g.drawString(linea, w - ow - 25, filaY + 21);

        // ---- cronómetro ----
        if (agotado) {
            if ((tick / 20) % 2 == 0) {
                textoCentrado(g, "¡AGOTADO!", w / 2, 72, 26, new Color(0xff6a5a));
            }
        } else if (segundos >= 0) {
            Color c = segundos <= 10 ? new Color(0xff6a5a) : Color.WHITE;
            textoCentrado(g, String.format("HUIDA %02d:%02d", segundos / 60, segundos % 60), w / 2, 72, 24, c);
        }

        // ---- banner de inicio de nivel ----
        if (bannerFrames > 0) {
            int alfa = Math.min(255, bannerFrames * 4);
            g.setColor(new Color(0, 0, 0, Math.min(150, alfa)));
            g.fillRect(0, h / 2 - 70, w, 90);
            textoCentrado(g, titulo, w / 2, h / 2 - 12, 34, new Color(255, 255, 255, alfa));
            textoCentrado(g, objetivo, w / 2, h / 2 + 14, 18, new Color(255, 226, 122, alfa));
        }

        // ---- mensaje temporal ----
        if (mensajeFrames > 0 && mensaje != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            fm = g.getFontMetrics();
            int mw = fm.stringWidth(mensaje);
            int alfa = Math.min(255, mensajeFrames * 6);
            g.setColor(new Color(0, 0, 0, Math.min(170, alfa)));
            g.fillRoundRect(w / 2 - mw / 2 - 16, h - 100, mw + 32, 38, 16, 16);
            g.setColor(new Color(255, 255, 255, alfa));
            g.drawString(mensaje, w / 2 - mw / 2, h - 75);
        }
    }

    private void dibujarCorazon(Graphics2D g, int x, int y, boolean lleno) {
        g.setColor(lleno ? new Color(0xe5384f) : new Color(90, 90, 90));
        g.fillOval(x, y, 10, 10);
        g.fillOval(x + 8, y, 10, 10);
        g.fillPolygon(new int[] {x, x + 18, x + 9}, new int[] {y + 6, y + 6, y + 18}, 3);
    }

    private void textoCentrado(Graphics2D g, String texto, int cx, int baseY, int tamano, Color color) {
        g.setFont(new Font("SansSerif", Font.BOLD, tamano));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(0, 0, 0, Math.min(200, color.getAlpha())));
        g.drawString(texto, cx - fm.stringWidth(texto) / 2 + 2, baseY + 2);
        g.setColor(color);
        g.drawString(texto, cx - fm.stringWidth(texto) / 2, baseY);
        g.setStroke(new BasicStroke(1f));
    }
}
