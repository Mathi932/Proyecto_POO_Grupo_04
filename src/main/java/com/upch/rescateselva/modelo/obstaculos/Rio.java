package com.upch.rescateselva.modelo.obstaculos;

import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.recursos.Sprites;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Rio extends Obstaculo {
    private final int velocidadCorriente;
    private int cooldownDanio;

    public Rio(int x, int y, int ancho, int alto, int velocidadCorriente) {
        super(x, y, ancho, alto);
        this.velocidadCorriente = velocidadCorriente;
    }

    @Override
    public void update() {
        super.update();
        if (cooldownDanio > 0) {
            cooldownDanio--;
        }
    }

    @Override
    public void afectar(Jugador j) {
        int pies = j.getY() + j.getAlto();
        boolean dentroX = j.getX() + j.getAncho() > x && j.getX() < x + ancho;
        if (dentroX && pies > y + 8) {
            j.entrarAlAgua(velocidadCorriente);
            if (cooldownDanio == 0) {
                j.recibirDanio(5);
                cooldownDanio = 40;
            }
        }
    }

    @Override
    public boolean esFrontal() {
        return true;
    }

    @Override
    public void dibujar(Graphics2D g) {
        BufferedImage superficie = Sprites.cargar((tick / 20) % 2 == 0 ? "agua0.png" : "agua1.png");
        if (superficie != null) {
            java.awt.Composite previo = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.72f));
            Sprites.mosaico(g, superficie, x, y, ancho, alto);
            g.setComposite(previo);
            return;
        }
        g.setColor(new Color(110, 96, 50, 170));
        g.fillRect(x, y, ancho, alto);

        g.setColor(new Color(170, 150, 90, 150));
        for (int i = x; i < x + ancho; i += 36) {
            int ola = (int) (3 * Math.sin((i * 0.07) + tick * 0.08));
            g.fillRect(i, y + ola, 22, 3);
        }
        g.setColor(new Color(200, 185, 120, 120));
        g.fillRect(x, y, ancho, 2);
    }
}
