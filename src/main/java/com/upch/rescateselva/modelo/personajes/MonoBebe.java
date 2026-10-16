package com.upch.rescateselva.modelo.personajes;

import java.awt.Color;
import java.awt.Graphics2D;

/** El Mono Bebé: está en una jaula de barrotes que hay que abrir (unos 1,2 segundos). */
public class MonoBebe extends AnimalAtrapado {

    public MonoBebe(int x, int y) {
        super(x, y, 30, 34, "Mono Bebé", "abrir la jaula", 75);
        this.distanciaSeguimiento = 75;
        cargarSprite("mono.png");
    }

    /** Abrir la jaula: hay que mantener la acción hasta que ceda. */
    @Override
    public void liberar() {
        avanzar();
    }

    @Override
    protected void dibujarPlaceholder(Graphics2D g) {
        int salto = isLiberado() ? (int) (2 * Math.sin(tick * 0.3)) : 0;
        // cola
        g.setColor(new Color(0x6b4423));
        g.setStroke(new java.awt.BasicStroke(3f));
        g.drawArc(-26, 14, 20, 20, 90, 180);
        g.setStroke(new java.awt.BasicStroke(1f));
        // cuerpo
        g.setColor(new Color(0x7a4e2a));
        g.fillOval(-11, 14 + salto, 22, 20);
        g.setColor(new Color(0xd9b88f));
        g.fillOval(-6, 19 + salto, 12, 12);
        // brazos y piernas
        g.setColor(new Color(0x6b4423));
        g.fillOval(7, 18 + salto, 6, 12);
        g.fillOval(-10, 30, 8, 5);
        g.fillOval(2, 30, 8, 5);
        // cabeza
        g.setColor(new Color(0x7a4e2a));
        g.fillOval(-11, 0 + salto, 22, 20);
        g.fillOval(-14, 6 + salto, 7, 7);  // orejas
        g.fillOval(7, 6 + salto, 7, 7);
        g.setColor(new Color(0xe3c39b));
        g.fillOval(-8, 6 + salto, 16, 13);
        // ojos y boca
        g.setColor(Color.BLACK);
        g.fillOval(-4, 9 + salto, 3, 4);
        g.fillOval(3, 9 + salto, 3, 4);
        g.drawArc(-3, 13 + salto, 7, 4, 200, 140);
    }
}
