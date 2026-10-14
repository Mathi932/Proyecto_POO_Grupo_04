package com.upch.rescateselva.modelo.obstaculos;

import com.upch.rescateselva.modelo.personajes.Jugador;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Clase base abstracta de todo lo que estorba o daña al jugador y no se mueve
 * por sí solo (trampas, río). Cada subclase define qué le hace al jugador.
 */
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

    /** Qué le pasa al jugador cuando está en contacto (o cerca) del obstáculo. */
    public abstract void afectar(Jugador j);

    public abstract void dibujar(Graphics2D g);

    public void update() {
        tick++;
    }

    /** true = se dibuja delante del jugador (por ejemplo el agua). */
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
