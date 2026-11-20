package com.upch.rescateselva.niveles;

import java.awt.Color;
import java.awt.Graphics2D;
import com.upch.rescateselva.recursos.Sprites;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

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
                    if (temporizador >= 75) {
                        activa = false;
                        reaparicion = 200;
                    }
                } else if (--reaparicion <= 0) {
                    activa = true;
                    temporizador = 0;
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

    public int getTop() {
        return y;
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
                dibujarFirme(g, top);
                break;
            case LECHO:
                BufferedImage lecho = Sprites.cargar("lecho.png");
                if (lecho != null) {
                    Sprites.mosaico(g, lecho, x, top, ancho, alto);
                } else {
                    g.setColor(new Color(0x7d6b45));
                    g.fillRect(x, top, ancho, alto);
                }
                break;
            case TRONCO:
                dibujarTronco(g, top);
                break;
            case FRAGIL:
                dibujarFragil(g, top);
                break;
            default:
                break;
        }
    }

    private void dibujarFirme(Graphics2D g, int top) {
        BufferedImage hierba = Sprites.cargar("hierba.png");
        BufferedImage tierra = Sprites.cargar("tierra.png");
        if (hierba == null || tierra == null) {
            g.setColor(new Color(0x5a3f24));
            g.fillRect(x, top, ancho, alto);
            g.setColor(new Color(0x3f8f4a));
            g.fillRect(x, top, ancho, 10);
            return;
        }
        int franja = hierba.getHeight() * Sprites.ESCALA;
        Sprites.mosaico(g, hierba, x, top, ancho, Math.min(franja, alto));
        if (alto > franja) {
            Sprites.mosaico(g, tierra, x, top + franja, ancho, alto - franja);
        }
    }

    private void dibujarTronco(Graphics2D g, int top) {
        BufferedImage cuerpo = Sprites.cargar("tronco.png");
        BufferedImage fin = Sprites.cargar("tronco_fin.png");
        if (cuerpo == null || fin == null) {
            g.setColor(new Color(0x6b4524));
            g.fillRoundRect(x, top, ancho, alto, 16, 16);
            return;
        }
        int px = x + (temporizador > 45 ? (temporizador % 4 < 2 ? -2 : 2) : 0);
        Sprites.mosaico(g, cuerpo, px, top, ancho, alto);
        Sprites.dibujar(g, fin, px - 4, top - 2);
        g.drawImage(fin, px + ancho + 4, top - 2, px + ancho + 4 - fin.getWidth() * Sprites.ESCALA,
                top - 2 + fin.getHeight() * Sprites.ESCALA, 0, 0, fin.getWidth(), fin.getHeight(), null);
    }

    private void dibujarFragil(Graphics2D g, int top) {
        int temblor = temporizador > 0 ? (temporizador % 4 < 2 ? -2 : 2) : 0;
        BufferedImage img = Sprites.cargar(temporizador > 8 ? "fragil_grieta.png" : "fragil.png");
        if (img == null) {
            g.setColor(new Color(0x7b7b73));
            g.fillRect(x + temblor, top, ancho, alto);
            return;
        }
        Sprites.mosaico(g, img, x + temblor, top, ancho, alto);
    }
}
