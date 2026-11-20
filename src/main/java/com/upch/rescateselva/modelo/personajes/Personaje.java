package com.upch.rescateselva.modelo.personajes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;
import com.upch.rescateselva.recursos.Sprites;

public abstract class Personaje {
    protected int x;
    protected int y;
    protected int vida;
    protected Image sprite;
    protected BufferedImage[] cuadros;

    protected int ancho;
    protected int alto;
    protected boolean mirandoDerecha = true;
    protected int tick;

    protected Personaje(int x, int y, int ancho, int alto, int vida) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.vida = vida;
    }

    public abstract void update();

    public void dibujar(Graphics2D g) {
        AffineTransform previa = g.getTransform();
        g.translate(x + ancho / 2.0, y);
        if (!mirandoDerecha) {
            g.scale(-1, 1);
        }
        if (!esVisible()) {
            g.setTransform(previa);
            return;
        }
        if (cuadros != null) {
            BufferedImage c = cuadros[Math.min(cuadroActual(), cuadros.length - 1)];
            int w = c.getWidth() * Sprites.ESCALA;
            int h = c.getHeight() * Sprites.ESCALA;
            g.drawImage(c, -w / 2, alto - h, w, h, null);
        } else if (sprite != null) {
            g.drawImage(sprite, -ancho / 2, 0, ancho, alto, null);
        } else {
            dibujarPlaceholder(g);
        }
        g.setTransform(previa);
    }

    protected void dibujarPlaceholder(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(-ancho / 2, 0, ancho, alto);
    }

    protected void cargarCuadros(String nombre, int cantidad) {
        cuadros = Sprites.cargarCuadros(nombre, cantidad);
    }

    protected int cuadroActual() {
        return 0;
    }

    protected boolean esVisible() {
        return true;
    }

    protected void cargarSprite(String nombre) {
        URL url = Personaje.class.getResource("/sprites/" + nombre);
        if (url != null) {
            sprite = new ImageIcon(url).getImage();
        }
    }

    public int getVida() {
        return vida;
    }

    protected void setVida(int v) {
        this.vida = Math.max(0, v);
    }

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
