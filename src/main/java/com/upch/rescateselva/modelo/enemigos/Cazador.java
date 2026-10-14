package com.upch.rescateselva.modelo.enemigos;

import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.modelo.personajes.Jugador;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Cazador de "Los Extractores". Patrulla una zona, puede lanzar piedras
 * (nivel 2) y persigue al jugador cuando empieza la huida.
 */
public class Cazador extends Enemigo {

    private static final int VEL_PATRULLA = 1;
    private static final int VEL_PERSECUCION = 3;

    private int patrullaMin;
    private int patrullaMax;
    private int dir = 1;
    private boolean modoPersecucion;
    private boolean lanzaPiedras;
    private boolean persigueAlDetectar;
    private int cooldownPiedra = 90;

    public Cazador() {
        super(30, 48, 40, 30, 380);
        cargarSprite("cazador.png");
    }

    // ---- configuración (la hace el nivel al construirlo) ----

    public void configurarPatrulla(int min, int max) {
        this.patrullaMin = min;
        this.patrullaMax = max;
    }

    public void setLanzaPiedras(boolean valor) {
        this.lanzaPiedras = valor;
    }

    public void setPersigueAlDetectar(boolean valor) {
        this.persigueAlDetectar = valor;
    }

    /** Empieza a perseguir al jugador sin soltarlo. */
    public void activarPersecucion() {
        this.modoPersecucion = true;
    }

    public boolean isPersiguiendo() {
        return modoPersecucion;
    }

    // ---- comportamiento ----

    @Override
    public void update() {
        super.update();
        if (!activo || aturdido > 0 || objetivo == null) {
            return;
        }
        int dx = (objetivo.getX() + objetivo.getAncho() / 2) - (x + ancho / 2);
        boolean cerca = Math.abs(dx) < rangoDeteccion && Math.abs(objetivo.getY() - y) < 220;

        if (modoPersecucion) {
            if (Math.abs(dx) > 8) {
                x += Integer.signum(dx) * VEL_PERSECUCION;
                mirandoDerecha = dx > 0;
            }
        } else if (cerca && persigueAlDetectar) {
            activarPersecucion();
        } else {
            patrullar();
            if (cerca) {
                mirandoDerecha = dx > 0;
            }
        }

        if (lanzaPiedras && (cerca || modoPersecucion) && Math.abs(dx) > 90) {
            cooldownPiedra--;
            if (cooldownPiedra <= 0) {
                lanzarPiedra();
                cooldownPiedra = modoPersecucion ? 100 : 140;
            }
        }
    }

    private void patrullar() {
        if (patrullaMax <= patrullaMin) {
            return;
        }
        x += dir * VEL_PATRULLA;
        if (x <= patrullaMin) {
            dir = 1;
        } else if (x >= patrullaMax) {
            dir = -1;
        }
        mirandoDerecha = dir > 0;
    }

    private void lanzarPiedra() {
        Distractor d = new Distractor(x + ancho / 2, y + 12);
        d.setLanzadoPorJugador(false);
        int frames = Math.max(35, Math.min(70, Math.abs(objetivo.getX() - x) / 8));
        d.lanzarAlObjetivo(objetivo.getX() + objetivo.getAncho() / 2,
                objetivo.getY() + objetivo.getAlto() / 2, frames);
        lanzados.add(d);
        mirandoDerecha = objetivo.getX() > x;
    }

    /**
     * Si el jugador está agotado, lo atrapa (fin del intento). Si no, le hace daño.
     * Un cazador aturdido no puede atacar.
     */
    @Override
    public void atacar(Jugador j) {
        if (!activo || aturdido > 0) {
            return;
        }
        if (j.estaAgotado()) {
            j.serAtrapado();
        } else {
            j.recibirDanio(danio);
        }
    }

    /** Cada piedra que recibe lo aturde; si se queda sin vida, huye del juego. */
    @Override
    public void recibirDanio(int cantidad) {
        super.recibirDanio(cantidad);
        aturdir(120);
        if (vida <= 0) {
            activo = false;
        }
    }

    @Override
    protected void dibujarPlaceholder(Graphics2D g) {
        int paso = (modoPersecucion || (patrullaMax > patrullaMin)) && aturdido == 0 ? ((tick / 8) % 2) * 3 : 0;
        // piernas
        g.setColor(new Color(0x5a4a34));
        g.fillRect(-9, 34, 8, 14 - paso / 2);
        g.fillRect(1, 34, 8, 14 - (3 - paso) / 2);
        g.setColor(new Color(0x2a2016));
        g.fillRect(-10, 46, 10, 2);
        g.fillRect(0, 46, 10, 2);
        // torso (chaleco caqui)
        g.setColor(new Color(0xa38d5d));
        g.fillRoundRect(-12, 17, 24, 20, 6, 6);
        g.setColor(new Color(0x6e5c36));
        g.fillRect(-12, 27, 24, 3);
        // brazo
        g.setColor(new Color(0x8c774c));
        g.fillRoundRect(6, 19, 8, 16, 5, 5);
        // pañuelo rojo
        g.setColor(new Color(0xb32d24));
        g.fillRect(-7, 16, 14, 4);
        // cabeza
        g.setColor(new Color(0xd9ab82));
        g.fillOval(-8, 3, 16, 16);
        // sombrero ancho
        g.setColor(new Color(0x5c4326));
        g.fillRoundRect(-14, 6, 28, 4, 3, 3);
        g.fillArc(-8, -3, 16, 14, 0, 180);
        // ceja y ojo (cara de pocos amigos)
        g.setColor(Color.BLACK);
        g.fillOval(3, 11, 3, 3);
        g.drawLine(1, 9, 7, 11);
        // bigote
        g.setColor(new Color(0x3b2a1a));
        g.fillRect(2, 15, 7, 2);
    }
}
