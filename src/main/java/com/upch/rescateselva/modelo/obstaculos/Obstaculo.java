package com.upch.rescateselva.modelo.obstaculos;

import com.upch.rescateselva.modelo.personajes.Jugador;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Obstaculo {
    protected int x;
    protected int y;
    protected int ancho;
    protected int alto;
    protected int tick;

    protected Obstaculo(int x, int y, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    public abstract void afectar(Jugador j);

    public abstract void dibujar(Graphics2D g);

    public void update() {
        tick++;
    }

    public boolean esFrontal() {
        return false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, ancho, alto);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
