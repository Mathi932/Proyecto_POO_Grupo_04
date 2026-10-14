package com.upch.rescateselva.modelo.personajes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Clase base abstracta de todo lo que "vive" en el juego:
 * el jugador, los animales atrapados y los enemigos.
 *
 * POLIMORFISMO: cada subclase implementa update() a su manera y dibuja su
 * propio "placeholder" (figura hecha con Java2D) mientras no haya sprite.
 *
 * ENCAPSULAMIENTO: x, y y vida son protected; desde afuera solo se tocan
 * mediante getters y recibirDanio().
 */
public abstract class Personaje {

    protected int x;
    protected int y;
    protected int vida;
    protected Image sprite;

    protected int ancho;
    protected int alto;
    protected boolean mirandoDerecha = true;
    protected int tick; // contador de frames, sirve para animaciones

    protected Personaje(int x, int y, int ancho, int alto, int vida) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.vida = vida;
    }

    /** Lógica de cada frame. Cada subclase decide qué hace. */
    public abstract void update();

    /**
     * Dibuja al personaje. Si hay sprite cargado lo usa; si no, llama a
     * dibujarPlaceholder(), que cada subclase redefine con su propio dibujo.
     * El sistema de coordenadas local tiene el origen en el centro-arriba del
     * personaje y ya viene espejado cuando mira a la izquierda.
     */
    public void dibujar(Graphics2D g) {
        AffineTransform previa = g.getTransform();
        g.translate(x + ancho / 2.0, y);
        if (!mirandoDerecha) {
            g.scale(-1, 1);
        }
        if (sprite != null) {
            g.drawImage(sprite, -ancho / 2, 0, ancho, alto, null);
        } else {
            dibujarPlaceholder(g);
        }
        g.setTransform(previa);
    }

    /** Dibujo por defecto (rectángulo magenta = "falta el dibujo"). */
    protected void dibujarPlaceholder(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(-ancho / 2, 0, ancho, alto);
    }

    /**
     * Intenta cargar /sprites/<nombre> desde resources. Si el archivo no existe
     * se queda con el placeholder, así el juego nunca se rompe por falta de arte.
     */
    protected void cargarSprite(String nombre) {
        URL url = Personaje.class.getResource("/sprites/" + nombre);
        if (url != null) {
            sprite = new ImageIcon(url).getImage();
        }
    }

    public int getVida() {
        return vida;
    }

    /** La vida nunca baja de 0. Solo las subclases pueden fijarla directamente. */
    protected void setVida(int v) {
        this.vida = Math.max(0, v);
    }

    /** Así nadie externo toca "vida" directamente. */
    public void recibirDanio(int cantidad) {
        setVida(vida - Math.max(0, cantidad));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }

    public void setPosicion(int nuevoX, int nuevoY) {
        this.x = nuevoX;
        this.y = nuevoY;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, ancho, alto);
    }
}
