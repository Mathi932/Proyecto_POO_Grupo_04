package com.upch.rescateselva.niveles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Superficie sobre la que se camina. Hay cuatro tipos:
 *  FIRME: suelo normal. LECHO: fondo del río (también sólido).
 *  TRONCO: se hunde si te quedas encima y vuelve a flotar después (nivel 2).
 *  FRAGIL: se desmorona poco después de pisarla y reaparece luego (nivel 3).
 */
public class Plataforma {

    public enum Tipo { FIRME, LECHO, TRONCO, FRAGIL }

    private final int x;
    private final int y;
    private final int ancho;
    private final int alto;
    private final Tipo tipo;

    private boolean activa = true;
    private boolean pisada;
    private int temporizador;
    private int hundimiento;
    private int reaparicion;

    public Plataforma(int x, int y, int ancho, int alto, Tipo tipo) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.tipo = tipo;
    }

    public void update() {
        switch (tipo) {
            case TRONCO:
                if (activa) {
                    if (pisada) {
                        temporizador++;
                    } else if (temporizador > 0) {
                        temporizador--;
                    }
                    hundimiento = Math.min(18, temporizador / 4);
                    if (temporizador >= 75) {
                        activa = false;
                        reaparicion = 200;
                    }
                } else if (--reaparicion <= 0) {
                    activa = true;
                    temporizador = 0;
                    hundimiento = 0;
                }
                break;
            case FRAGIL:
                if (activa) {
                    if (pisada || temporizador > 0) {
                        temporizador++;
                    }
                    if (temporizador >= 40) {
                        activa = false;
                        reaparicion = 260;
                    }
                } else if (--reaparicion <= 0) {
                    activa = true;
                    temporizador = 0;
                }
                break;
            default:
                break;
        }
        pisada = false;
    }

    /** El nivel avisa que el jugador está parado encima este frame. */
    public void pisar() {
        pisada = true;
    }

    public boolean isActiva() {
        return activa;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getX() {
        return x;
    }

    public int getAncho() {
        return ancho;
    }

    /** Borde superior real (baja un poco si es un tronco que se hunde). */
    public int getTop() {
        return y + hundimiento;
    }

    public int getAlto() {
        return alto;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, getTop(), ancho, alto);
    }

    public void dibujar(Graphics2D g) {
        if (!activa) {
            return;
        }
        int top = getTop();
        switch (tipo) {
            case FIRME:
                g.setColor(new Color(0x5a3f24));
                g.fillRect(x, top, ancho, alto);
                g.setColor(new Color(0x47301a));
                for (int i = x + 20; i < x + ancho; i += 70) {
                    g.fillOval(i, top + 28 + (i % 3) * 14, 14, 8);
                }
                g.setColor(new Color(0x3f8f4a));
                g.fillRect(x, top, ancho, 10);
                g.setColor(new Color(0x62b564));
                for (int i = x; i < x + ancho; i += 12) {
                    g.fillRect(i, top - 3 + (i % 5 == 0 ? -2 : 0), 5, 4);
                }
                break;
            case LECHO:
                g.setColor(new Color(0x7d6b45));
                g.fillRect(x, top, ancho, alto);
                break;
            case TRONCO:
                g.setColor(new Color(0x6b4524));
                g.fillRoundRect(x, top, ancho, alto, 16, 16);
                g.setColor(new Color(0x8a5a30));
                g.fillRoundRect(x + 4, top + 3, ancho - 8, 7, 8, 8);
                g.setColor(new Color(0x3d2812));
                g.drawRoundRect(x, top, ancho, alto, 16, 16);
                g.setColor(new Color(0xc9a06a)); // anillos en los extremos
                g.fillOval(x - 2, top + 2, 14, alto - 4);
                g.fillOval(x + ancho - 12, top + 2, 14, alto - 4);
                break;
            case FRAGIL:
                int temblor = temporizador > 0 ? (temporizador % 4 < 2 ? -1 : 1) : 0;
                int px = x + temblor;
                g.setColor(new Color(0x7b7b73));
                g.fillRect(px, top, ancho, alto);
                g.setColor(new Color(0x9a9a90));
                g.fillRect(px, top, ancho, 6);
                g.setColor(new Color(0x4d4d47));
                g.drawRect(px, top, ancho, alto);
                for (int i = px + 20; i < px + ancho; i += 22) {
                    g.drawLine(i, top, i, top + alto);
                }
                if (temporizador > 8) { // grietas
                    g.setColor(new Color(0x2c2c28));
                    g.drawLine(px + ancho / 2 - 8, top, px + ancho / 2, top + alto);
                    g.drawLine(px + ancho / 2, top + alto, px + ancho / 2 + 10, top + 4);
                }
                break;
            default:
                break;
        }
    }
}
