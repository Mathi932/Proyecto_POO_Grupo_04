package com.upch.rescateselva.modelo.enemigos;

import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.modelo.personajes.Jugador;
import com.upch.rescateselva.modelo.personajes.Personaje;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Enemigo abstracto. Hereda de Personaje (así reutiliza x, y, vida y dibujar()).
 * POLIMORFISMO: cada enemigo implementa atacar() de forma distinta.
 */
public abstract class Enemigo extends Personaje {

    protected int danio;
    protected int rangoDeteccion;
    protected int aturdido;           // frames que le quedan aturdido
    protected boolean activo = true;  // false = ya salió del juego
    protected Jugador objetivo;
    protected final List<Distractor> lanzados = new ArrayList<>();

    protected Enemigo(int ancho, int alto, int vida, int danio, int rangoDeteccion) {
        super(0, 0, ancho, alto, vida);
        this.danio = danio;
        this.rangoDeteccion = rangoDeteccion;
    }

    /** Qué hace el enemigo cuando alcanza al jugador. */
    public abstract void atacar(Jugador j);

    @Override
    public void update() {
        tick++;
        if (aturdido > 0) {
            aturdido--;
        }
    }

    /** Lo deja sin moverse ni atacar durante "frames" fotogramas. */
    public void aturdir(int frames) {
        aturdido = Math.max(aturdido, frames);
    }

    public boolean estaAturdido() {
        return aturdido > 0;
    }

    /** A quién persigue / vigila. */
    public void setObjetivo(Jugador j) {
        this.objetivo = j;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getDanio() {
        return danio;
    }

    public int getRangoDeteccion() {
        return rangoDeteccion;
    }

    /** Distractores que lanzó en este frame; el nivel los toma para animarlos. */
    public List<Distractor> retirarLanzados() {
        List<Distractor> copia = new ArrayList<>(lanzados);
        lanzados.clear();
        return copia;
    }

    @Override
    public void dibujar(Graphics2D g) {
        super.dibujar(g);
        if (aturdido > 0) { // estrellitas girando sobre la cabeza
            g.setColor(new Color(255, 225, 90));
            for (int i = 0; i < 3; i++) {
                double ang = tick * 0.2 + i * 2.1;
                int sx = x + ancho / 2 + (int) (14 * Math.cos(ang));
                int sy = y - 8 + (int) (4 * Math.sin(ang));
                g.fillOval(sx - 3, sy - 3, 6, 6);
            }
        }
    }
}
