package com.upch.rescateselva.modelo.personajes;

import java.awt.Color;
import java.awt.Graphics2D;

/** El Jaguar Cachorro: su jaula tiene un candado que hay que romper (unos 1,8 segundos). */
public class JaguarCachorro extends AnimalAtrapado {

    public JaguarCachorro(int x, int y) {
        super(x, y, 46, 30, "Jaguar Cachorro", "romper el candado", 110);
        this.distanciaSeguimiento = 105;
        cargarSprite("jaguar.png");
    }

    /** Romper el candado: hay que mantener la acción hasta que ceda. */
    @Override
    public void liberar() {
        avanzar();
    }

    @Override
    protected void dibujarJaula(Graphics2D g) {
        super.dibujarJaula(g);
        int cx = x + ancho / 2;
        int base = y + alto;
        // candado al frente; tiembla y se agrieta
        double avance = progreso / (double) requerido;
        int temblor = progreso > 0 ? (int) (2 * Math.sin(tick * 1.5)) : 0;
        int lx = cx - 9 + temblor;
        int ly = base - 36;
        g.setColor(new Color(0xd9a21b));
        g.fillRoundRect(lx, ly, 18, 15, 4, 4);
        g.setColor(new Color(0x8a6410));
        g.drawArc(lx + 3, ly - 9, 12, 14, 0, 180);
        g.fillOval(lx + 7, ly + 4, 4, 5);
        if (avance > 0.35) {
            g.setColor(new Color(0x3a2a08));
            g.drawLine(lx + 4, ly, lx + 9, ly + 8);
            g.drawLine(lx + 9, ly + 8, lx + 14, ly + 15);
        }
    }

    @Override
    protected void dibujarPlaceholder(Graphics2D g) {
        int paso = isLiberado() ? (tick / 7) % 2 : 0;
        // cola
        g.setColor(new Color(0xe2a93b));
        g.setStroke(new java.awt.BasicStroke(4f));
        g.drawArc(-30, 4, 22, 20, 180, 150);
        g.setStroke(new java.awt.BasicStroke(1f));
        // patas
        g.setColor(new Color(0xd6992b));
        g.fillRect(-16, 20, 6, 10 - paso);
        g.fillRect(-7, 20, 6, 10 - (1 - paso));
        g.fillRect(6, 20, 6, 10 - (1 - paso));
        g.fillRect(14, 20, 6, 10 - paso);
        // cuerpo
        g.setColor(new Color(0xe2a93b));
        g.fillRoundRect(-20, 6, 40, 20, 14, 14);
        // manchas
        g.setColor(new Color(0x4a2f0a));
        g.fillOval(-14, 10, 6, 5);
        g.fillOval(-4, 14, 6, 5);
        g.fillOval(6, 9, 6, 5);
        g.fillOval(-9, 19, 5, 4);
        // cabeza
        g.setColor(new Color(0xe9b44a));
        g.fillOval(10, 0, 22, 20);
        g.fillOval(12, -3, 7, 7);  // orejas
        g.fillOval(24, -3, 7, 7);
        g.setColor(new Color(0xf3dca0));
        g.fillOval(18, 9, 13, 10);
        // ojos y nariz
        g.setColor(Color.BLACK);
        g.fillOval(17, 6, 3, 4);
        g.fillOval(25, 6, 3, 4);
        g.setColor(new Color(0x7a3b2a));
        g.fillOval(23, 11, 5, 3);
    }
}
