package com.upch.rescateselva.modelo.personajes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/** El Tucán: está atrapado en una red que hay que cortar (unos 0,7 segundos de acción). */
public class Tucan extends AnimalAtrapado {

    public Tucan(int x, int y) {
        super(x, y, 34, 30, "Tucán", "cortar la red", 45);
        this.vuela = true;
        this.distanciaSeguimiento = 45;
        cargarSprite("tucan.png");
    }

    /** Cortar la red: hay que mantener la acción hasta completar el corte. */
    @Override
    public void liberar() {
        avanzar();
    }

    @Override
    protected void dibujarJaula(Graphics2D g) {
        // Una red de cuerdas colgando de un palo
        int cx = x + ancho / 2;
        int base = y + alto;
        int izq = cx - 36;
        int der = cx + 36;
        int arriba = base - 72;
        g.setColor(new Color(0x5b4630));
        g.fillRect(izq - 4, arriba - 5, 80, 6);
        double corte = progreso / (double) requerido;
        g.setColor(new Color(0xd8c9a0));
        for (int gx = izq; gx <= der; gx += 12) {
            g.drawLine(gx, arriba, gx, base);
        }
        for (int gy = arriba; gy <= base; gy += 12) {
            g.drawLine(izq, gy, der, gy);
        }
        if (corte > 0) { // cuerdas cortadas (se abren)
            g.setColor(new Color(0xff6a5a));
            int tajo = (int) (66 * corte);
            g.drawLine(izq, base - 6, izq + tajo, base - 6 - tajo);
        }
    }

    @Override
    protected void dibujarPlaceholder(Graphics2D g) {
        int aleteo = isLiberado() ? (int) (4 * Math.sin(tick * 0.5)) : 0;
        // cola
        g.setColor(new Color(0x1d1d22));
        g.fillOval(-18, 10, 14, 8);
        // ala
        g.setColor(new Color(0x2b2b33));
        g.fillOval(-9, 8 + aleteo / 2, 16, 10);
        // cuerpo
        g.setColor(new Color(0x24242a));
        g.fillOval(-12, 6, 24, 20);
        g.setColor(new Color(0xffe27a)); // pechito amarillo
        g.fillOval(-3, 12, 12, 11);
        // cabeza
        g.setColor(new Color(0x24242a));
        g.fillOval(2, 0, 14, 14);
        // pico
        Polygon pico = new Polygon();
        pico.addPoint(11, 3);
        pico.addPoint(26, 6);
        pico.addPoint(11, 11);
        g.setColor(new Color(0xff8a1f));
        g.fillPolygon(pico);
        g.setColor(new Color(0xe8352a));
        g.fillRect(11, 6, 12, 2);
        // ojo
        g.setColor(Color.WHITE);
        g.fillOval(8, 3, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(10, 4, 2, 3);
        // patitas
        g.setColor(new Color(0xff8a1f));
        g.fillRect(-3, 25, 2, 4);
        g.fillRect(3, 25, 2, 4);
    }
}
