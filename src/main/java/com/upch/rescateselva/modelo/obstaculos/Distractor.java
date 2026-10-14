package com.upch.rescateselva.modelo.obstaculos;

import com.upch.rescateselva.modelo.enemigos.Enemigo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Random;

/**
 * Piedra/objeto que se puede lanzar. Tiene dos estados:
 *  - EN VUELO: viaja en arco (gravedad) y puede golpear a alguien.
 *  - EN EL SUELO: espera a que el jugador lo toque; se recoge solo (sin tecla).
 *
 * Lo lanzan el jugador (daña/aturde enemigos) y los enemigos (dañan al jugador).
 * Lo que el jugador esquiva queda en el suelo y se lo puede devolver al enemigo.
 */
public class Distractor {

    private static final double GRAVEDAD = 0.35;
    private static final Random AZAR = new Random();

    private double px;
    private double py;
    private double prevPy;
    private double vx;
    private double vy;
    private int x;
    private int y;
    private final int ancho = 14;
    private final int alto = 12;
    private boolean enSuelo;
    private boolean recogido;
    private boolean lanzadoPorJugador;
    private final int danio;        // daño que hace a un enemigo (6 o 7)
    private final int danioAJugador = 10;
    private double giro;

    /** Crea un distractor con su centro en (x, y). Nace en vuelo, sin velocidad. */
    public Distractor(int x, int y) {
        this.px = x - ancho / 2.0;
        this.py = y - alto / 2.0;
        this.prevPy = py;
        this.x = (int) px;
        this.y = (int) py;
        this.danio = 6 + AZAR.nextInt(2); // 6 o 7
    }

    /** Lo deja "reposando" en el suelo, listo para recogerse. */
    public static Distractor enElSuelo(int x, int yPies) {
        Distractor d = new Distractor(x, yPies);
        d.enSuelo = true;
        d.py = yPies - d.alto;
        d.prevPy = d.py;
        d.y = (int) d.py;
        return d;
    }

    /**
     * Lanza en una dirección horizontal (dirX = -1 o 1). dirY positivo = hacia
     * abajo, negativo = hacia arriba. Tiro tenso, casi recto, pensado para el jugador.
     */
    public void lanzar(int dirX, int dirY) {
        lanzarHacia(dirX * 11.0, dirY * 10.0 - 2.5);
    }

    /** Lanza con una velocidad exacta (la usan los enemigos para apuntar). */
    public void lanzarHacia(double velX, double velY) {
        this.vx = velX;
        this.vy = velY;
        this.enSuelo = false;
        this.recogido = false;
    }

    /**
     * Calcula la velocidad para que el objeto llegue al punto (objetivoX, objetivoY)
     * en "frames" fotogramas, describiendo un arco.
     */
    public void lanzarAlObjetivo(int objetivoX, int objetivoY, int frames) {
        double t = Math.max(1, frames);
        double velX = (objetivoX - (px + ancho / 2.0)) / t;
        double velY = (objetivoY - (py + alto / 2.0)) / t - 0.5 * GRAVEDAD * t;
        lanzarHacia(velX, velY);
    }

    /** El jugador lo toca mientras está en el suelo: pasa a su inventario. */
    public void recoger() {
        this.recogido = true;
        this.enSuelo = false;
    }

    /** Impacto contra un enemigo: le baja vida y lo aturde un momento. */
    public void aplicarEfecto(Enemigo e) {
        e.recibirDanio(danio);
        e.aturdir(90);
    }

    /** Lo deja quieto sobre una superficie cuya parte de arriba está en yTop. */
    public void aterrizarEn(int yTop) {
        py = yTop - alto;
        y = (int) py;
        vx = 0;
        vy = 0;
        enSuelo = true;
    }

    public void update() {
        if (enSuelo || recogido) {
            return;
        }
        prevPy = py;
        vy += GRAVEDAD;
        px += vx;
        py += vy;
        giro += 0.35 * Math.signum(vx == 0 ? 1 : vx);
        x = (int) px;
        y = (int) py;
    }

    public void dibujar(Graphics2D g) {
        AffineTransform previa = g.getTransform();
        g.translate(x + ancho / 2.0, y + alto / 2.0);
        if (!enSuelo) {
            g.rotate(giro);
        }
        Color base = lanzadoPorJugador ? new Color(0x8d8d85) : new Color(0x7a5a3a);
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(-ancho / 2 + 1, -alto / 2 + 2, ancho, alto);
        g.setColor(base);
        g.fillOval(-ancho / 2, -alto / 2, ancho, alto);
        g.setColor(base.brighter());
        g.fillOval(-ancho / 2 + 2, -alto / 2 + 2, 5, 4);
        g.setColor(base.darker());
        g.drawOval(-ancho / 2, -alto / 2, ancho, alto);
        g.setTransform(previa);
        if (enSuelo) { // brillo para que se note que se puede recoger
            int alfa = 70 + (int) (50 * Math.sin(System.nanoTime() / 2.5e8));
            g.setColor(new Color(255, 240, 150, Math.max(20, alfa)));
            g.drawOval(x - 4, y - 4, ancho + 8, alto + 8);
        }
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

    public int getAlto() {
        return alto;
    }

    public double getVy() {
        return vy;
    }

    public int getYAnteriorPies() {
        return (int) prevPy + alto;
    }

    public boolean isEnSuelo() {
        return enSuelo;
    }

    public boolean isRecogido() {
        return recogido;
    }

    public boolean isLanzadoPorJugador() {
        return lanzadoPorJugador;
    }

    public void setLanzadoPorJugador(boolean valor) {
        this.lanzadoPorJugador = valor;
    }

    public int getDanio() {
        return danio;
    }

    public int getDanioAJugador() {
        return danioAJugador;
    }
}
