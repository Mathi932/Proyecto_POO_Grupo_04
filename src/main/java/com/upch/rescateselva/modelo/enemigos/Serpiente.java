package com.upch.rescateselva.modelo.enemigos;

import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.modelo.personajes.Jugador;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * La Serpiente. En las Ruinas (nivel 3) patrulla como un enemigo normal; en el
 * Campamento es el JEFE FINAL: 100 de vida (usa la vida heredada de Personaje),
 * lanza objetos y no se puede matar: al llegar a 0 se asusta y huye.
 */
public class Serpiente extends Enemigo {

    private boolean jefe;
    private boolean asustada;
    private int tiempoHuida;
    private int patrullaMin;
    private int patrullaMax;
    private int dir = -1;
    private int cooldownLanzar = 120;
    private int flashDanio;

    public Serpiente() {
        super(60, 36, 30, 15, 420);
        cargarSprite("serpiente.png");
    }

    /** La convierte en el jefe del campamento: más grande y con 100 de vida. */
    public void convertirEnJefe() {
        this.jefe = true;
        this.ancho = 130;
        this.alto = 70;
        this.vida = 100;
        this.danio = 15;
    }

    public void configurarPatrulla(int min, int max) {
        this.patrullaMin = min;
        this.patrullaMax = max;
    }

    public boolean isJefe() {
        return jefe;
    }

    public boolean estaAsustada() {
        return asustada;
    }

    /** true cuando ya terminó de huir de la pantalla (cierra el combate). */
    public boolean huidaCompleta() {
        return asustada && tiempoHuida >= 110;
    }

    public int getVidaMaxima() {
        return jefe ? 100 : 30;
    }

    @Override
    public void update() {
        super.update();
        if (flashDanio > 0) {
            flashDanio--;
        }
        if (asustada) {
            x += 7;
            tiempoHuida++;
            mirandoDerecha = true;
            return;
        }
        if (!activo || aturdido > 0 || objetivo == null) {
            return;
        }
        // desplazamiento lento de lado a lado
        if (patrullaMax > patrullaMin) {
            x += dir * (jefe ? 1 : 1);
            if (x <= patrullaMin) {
                dir = 1;
            } else if (x >= patrullaMax) {
                dir = -1;
            }
        }
        mirandoDerecha = jefe ? objetivo.getX() > x : dir > 0;

        if (jefe) {
            cooldownLanzar--;
            if (cooldownLanzar <= 0) {
                lanzarObjeto();
                cooldownLanzar = 140; // más seguido que el cazador
            }
        }
    }

    private void lanzarObjeto() {
        Distractor d = new Distractor(x + (mirandoDerecha ? ancho : 0), y + 14);
        d.setLanzadoPorJugador(false);
        int frames = Math.max(40, Math.min(90, Math.abs(objetivo.getX() - x) / 9));
        d.lanzarAlObjetivo(objetivo.getX() + objetivo.getAncho() / 2,
                objetivo.getY() + objetivo.getAlto() / 2, frames);
        lanzados.add(d);
    }

    @Override
    public void atacar(Jugador j) {
        if (!activo || asustada || aturdido > 0) {
            return;
        }
        if (j.estaAgotado()) {
            j.serAtrapado();
        } else {
            j.recibirDanio(danio);
        }
    }

    /**
     * El jefe nunca muere: al llegar a 0 de vida se asusta y huye.
     * Una Serpiente normal sí desaparece si se queda sin vida.
     */
    @Override
    public void recibirDanio(int cantidad) {
        if (asustada) {
            return;
        }
        super.recibirDanio(cantidad);
        flashDanio = 8;
        if (vida <= 0) {
            if (jefe) {
                asustada = true;
                tiempoHuida = 0;
            } else {
                activo = false;
            }
        }
    }

    /** El jefe se aturde muy poco para que el combate no se vuelva trivial. */
    @Override
    public void aturdir(int frames) {
        super.aturdir(jefe ? Math.min(frames, 25) : frames);
    }

    @Override
    protected void dibujarPlaceholder(Graphics2D g) {
        boolean blanco = flashDanio > 0 && (flashDanio / 2) % 2 == 0;
        Color cuerpo = blanco ? new Color(0xf4f4f4) : (jefe ? new Color(0x3d6b35) : new Color(0x4f8f3f));
        Color vientre = blanco ? Color.WHITE : new Color(0xc9d98a);
        int w = ancho;
        int h = alto;
        int segmentos = jefe ? 16 : 10;
        double ritmo = asustada ? 0.5 : 0.14;
        for (int i = 0; i < segmentos; i++) {
            double t = i / (double) (segmentos - 1);
            int sx = (int) (-w / 2.0 + t * (w - h * 0.45));
            int sy = (int) (h * 0.62 + Math.sin(tick * ritmo + i * 0.8) * h * 0.13);
            int r = (int) (h * 0.12 + t * h * 0.13);
            g.setColor(cuerpo);
            g.fillOval(sx - r, sy - r, r * 2, r * 2);
            if (i % 2 == 0) {
                g.setColor(jefe ? new Color(0xd8b13a) : cuerpo.darker());
                g.fillOval(sx - r / 2, sy - r + 1, r, r / 2 + 1);
            }
            g.setColor(vientre);
            g.fillOval(sx - r / 2, sy + r / 3, r, r / 2);
        }
        // cabeza
        int hx = w / 2 - (int) (h * 0.38);
        int hy = (int) (h * 0.62 - h * 0.30);
        g.setColor(cuerpo);
        g.fillOval(hx, hy, (int) (h * 0.62), (int) (h * 0.48));
        if (jefe) { // cresta
            g.setColor(new Color(0xb33a2a));
            Polygon cresta = new Polygon();
            cresta.addPoint(hx + 6, hy + 2);
            cresta.addPoint(hx + 14, hy - 12);
            cresta.addPoint(hx + 22, hy + 2);
            cresta.addPoint(hx + 30, hy - 8);
            cresta.addPoint(hx + 36, hy + 3);
            g.fillPolygon(cresta);
        }
        // ojo
        g.setColor(new Color(0xffe14a));
        g.fillOval(hx + (int) (h * 0.30), hy + (int) (h * 0.08), 8, 8);
        g.setColor(Color.BLACK);
        g.fillRect(hx + (int) (h * 0.30) + 3, hy + (int) (h * 0.08), 2, 8);
        if (asustada) {
            g.setColor(new Color(120, 190, 255));
            g.fillOval(hx + 4, hy - 6, 4, 6);
            g.fillOval(hx + 14, hy - 10, 4, 6);
        }
        // lengua
        if ((tick / 10) % 2 == 0 && !asustada) {
            g.setColor(new Color(0xd0213a));
            int lx = hx + (int) (h * 0.62);
            int ly = hy + (int) (h * 0.25);
            g.drawLine(lx, ly, lx + 10, ly);
            g.drawLine(lx + 10, ly, lx + 14, ly - 3);
            g.drawLine(lx + 10, ly, lx + 14, ly + 3);
        }
    }
}
