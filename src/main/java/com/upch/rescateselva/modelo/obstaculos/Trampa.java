package com.upch.rescateselva.modelo.obstaculos;

import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.recursos.Sprites;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Trampa extends Obstaculo {
    private final int danio = 20;

    public Trampa(int x, int y) {
        super(x, y, 40, 24);
    }

    @Override
    public void afectar(Jugador j) {
        if (getBounds().intersects(j.getBounds())) {
            j.recibirDanio(danio);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 6, y + 6, ancho - 12, alto - 6);
    }

    @Override
    public void dibujar(Graphics2D g) {
        BufferedImage img = Sprites.cargar("trampa.png");
        if (img != null) {
            g.drawImage(img, x, y + alto - img.getHeight() * Sprites.ESCALA,
                    img.getWidth() * Sprites.ESCALA, img.getHeight() * Sprites.ESCALA, null);
            return;
        }
        int puntas = 4;
        int w = ancho / puntas;
        for (int i = 0; i < puntas; i++) {
            Polygon p = new Polygon();
            p.addPoint(x + i * w, y + alto);
            p.addPoint(x + i * w + w / 2, y);
            p.addPoint(x + (i + 1) * w, y + alto);
            g.setColor(new Color(0x6b4a2b));
            g.fillPolygon(p);
            g.setColor(new Color(0xcdb38b));
            g.drawLine(x + i * w + w / 2, y, x + i * w + w / 2 - 2, y + alto / 2);
            g.setColor(new Color(0x3b2814));
            g.drawPolygon(p);
        }
    }
}
