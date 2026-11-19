package com.upch.rescateselva.modelo.personajes;

import com.upch.rescateselva.modelo.obstaculos.Distractor;
import com.upch.rescateselva.patrones.observer.EstadisticasSubject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Jugador extends Personaje {
    public static final int VEL = 4;
    public static final int VEL_AGOTADO = 1;
    public static final int VEL_AGUA = 2;
    public static final int SALTO = -15;
    public static final int SALTO_AGOTADO = -8;
    public static final int GRAVEDAD = 1;
    public static final int VEL_CAIDA_MAX = 16;
    public static final int MAX_DISTRACTORES = 5;
    public static final int FPS = 60;

    private int framesJugados;
    private final EstadisticasSubject stats = new EstadisticasSubject();
    private boolean agotado;
    private boolean atrapado;
    private final List<Distractor> inventario = new ArrayList<>();
    private final List<Distractor> lanzados = new ArrayList<>();

    private int intentoX;
    private boolean saltoPedido;
    private int vy;
    private boolean enSuelo;
    private int prevX;
    private int prevY;
    private boolean caminando;
    private boolean enAgua;
    private int corriente;
    private int invulnerable;
    private int cooldownLanzar;
    private int seguroX;
    private int seguroY;

    public Jugador(int x, int y) {
        super(x, y, 28, 44, 100);
        this.prevX = x;
        this.prevY = y;
        this.seguroX = x;
        this.seguroY = y;
        cargarSprite("amaru.png");
        actualizarStats();
    }



    public void mover(int dx, int dy) {
        this.intentoX = Integer.signum(dx);
        if (dy < 0) {
            this.saltoPedido = true;
        }
    }

    public void rescatar(AnimalAtrapado a) {
        if (a == null || a.isLiberado()) {
            return;
        }
        int dx = Math.abs((x + ancho / 2) - (a.getX() + a.getAncho() / 2));
        int dy = Math.abs((y + alto / 2) - (a.getY() + a.getAlto() / 2));
        if (dx > 70 || dy > 80) {
            return;
        }
        a.liberar();
        if (a.isLiberado()) {
        }
    }

    public void lanzarDistractor() {
        if (inventario.isEmpty() || cooldownLanzar > 0 || atrapado) {
            return;
        }
        inventario.remove(inventario.size() - 1);
        Distractor nuevo = new Distractor(x + ancho / 2, y + 14);
        nuevo.setLanzadoPorJugador(true);
        nuevo.lanzar(mirandoDerecha ? 1 : -1, 0);
        lanzados.add(nuevo);
        cooldownLanzar = 18;
    }

    public boolean agregarDistractor(Distractor d) {
        if (inventario.size() >= MAX_DISTRACTORES) {
            return false;
        }
        inventario.add(d);
        return true;
    }

    public List<Distractor> retirarLanzados() {
        List<Distractor> copia = new ArrayList<>(lanzados);
        lanzados.clear();
        return copia;
    }

    public void activarCansancio() {
        this.agotado = true;
    }

    public boolean estaAgotado() {
        return agotado;
    }

    @Override
    public void update() {
        tick++;
        framesJugados++;
        if (framesJugados % FPS == 0) {
            actualizarStats();
        }
        prevX = x;
        prevY = y;

        int vel = agotado ? VEL_AGOTADO : (enAgua ? VEL_AGUA : VEL);
        x += intentoX * vel;
        caminando = intentoX != 0;
        if (intentoX != 0) {
            mirandoDerecha = intentoX > 0;
        }
        if (enAgua) {
            x -= corriente;
        }
        if (saltoPedido && enSuelo && !atrapado) {
            vy = agotado ? SALTO_AGOTADO : SALTO;
            enSuelo = false;
        }
        vy = Math.min(vy + GRAVEDAD, VEL_CAIDA_MAX);
        y += vy;
        enSuelo = false;

        intentoX = 0;
        saltoPedido = false;
        enAgua = false;
        if (invulnerable > 0) {
            invulnerable--;
        }
        if (cooldownLanzar > 0) {
            cooldownLanzar--;
        }
    }



    @Override
    public void recibirDanio(int cantidad) {
        if (invulnerable > 0 || vida <= 0) {
            return;
        }
        super.recibirDanio(cantidad);
        invulnerable = 70;
        actualizarStats();
    }

    public void serAtrapado() {
        atrapado = true;
        setVida(0);
        actualizarStats();
    }

    public void caer() {
        super.recibirDanio(20);
        invulnerable = 70;
        x = seguroX;
        y = seguroY;
        prevX = x;
        prevY = y;
        vy = 0;
        actualizarStats();
    }

    public void teletransportar(int nuevoX, int nuevoY) {
        x = nuevoX;
        y = nuevoY;
        prevX = x;
        prevY = y;
        vy = 0;
        marcarSeguro();
    }

    public void entrarAlAgua(int velocidadCorriente) {
        this.enAgua = true;
        this.corriente = velocidadCorriente;
    }

    public void reiniciarParaNivel(int inicioX, int inicioY) {
        x = inicioX;
        y = inicioY;
        prevX = x;
        prevY = y;
        seguroX = x;
        seguroY = y;
        vida = 100;
        vy = 0;
        agotado = false;
        atrapado = false;
        invulnerable = 0;
        cooldownLanzar = 0;
        enAgua = false;
        mirandoDerecha = true;
        inventario.clear();
        lanzados.clear();
        actualizarStats();
    }

    private void actualizarStats() {
        stats.setEstado(getSegundosJugados(), vida); // OBSERVER: publica el cambio
    }



    public int getPrevX() {
        return prevX;
    }

    public int getPrevY() {
        return prevY;
    }

    public int getVy() {
        return vy;
    }

    public boolean isEnSuelo() {
        return enSuelo;
    }

    public void aterrizar(int yTop) {
        y = yTop - alto;
        vy = 0;
        enSuelo = true;
    }

    public void golpearTecho(int yAbajo) {
        y = yAbajo;
        if (vy < 0) {
            vy = 0;
        }
    }

    public void empujarX(int nuevoX) {
        x = nuevoX;
    }

    public void marcarSeguro() {
        seguroX = x;
        seguroY = y;
    }



    public int getSegundosJugados() {
        return framesJugados / FPS;
    }

    public EstadisticasSubject getStats() {
        return stats;
    }

    public int getCantidadDistractores() {
        return inventario.size();
    }

    public boolean isAtrapado() {
        return atrapado;
    }



    @Override
    protected void dibujarPlaceholder(Graphics2D g) {
        if (invulnerable > 0 && (invulnerable / 4) % 2 == 0) {
            return;
        }
        int paso = caminando && enSuelo ? ((tick / 6) % 2) * 3 : 0;

        g.setColor(new Color(0x7b5a3a));
        g.fillRoundRect(-15, 18, 8, 15, 4, 4);

        g.setColor(new Color(0x4a3b2a));
        g.fillRect(-9, 33, 8, 11 - paso / 2);
        g.fillRect(1, 33, 8, 11 - (3 - paso) / 2);
        g.setColor(new Color(0x2b2118));
        g.fillRect(-10, 42, 10, 2);
        g.fillRect(0, 42, 10, 2);

        g.setColor(agotado ? new Color(0x5f8f78) : new Color(0x2f6b4f));
        g.fillRoundRect(-11, 16, 22, 19, 6, 6);
        g.setColor(new Color(0xd9b45b));
        g.fillRect(-11, 28, 22, 3);

        g.setColor(new Color(0xf0c8a0));
        g.fillOval(-8, 3, 16, 16);

        g.setColor(new Color(0xb98b4a));
        g.fillRoundRect(-12, 5, 24, 4, 3, 3);
        g.fillArc(-8, -4, 16, 14, 0, 180);
        g.setColor(new Color(0x7a5a2c));
        g.fillRect(-8, 3, 16, 2);

        g.setColor(new Color(0x2a1d12));
        g.fillOval(3, 10, 3, 3);
        if (agotado) {
            g.setColor(new Color(120, 190, 255, 220));
            int d = (tick / 5) % 8;
            g.fillOval(-11, 8 + d, 3, 4);
            g.fillOval(10, 4 + d, 3, 4);
        }
    }
}
