package com.upch.rescateselva.modelo.personajes;

import com.upch.rescateselva.modelo.enemigos.Enemigo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Animal prisionero. Es abstracta: cada especie (Tucán, Mono, Jaguar) tiene su
 * propia forma de ser liberada (método liberar()) y su propio dibujo.
 *
 * Una vez liberado sigue al jugador y, en el campamento, ayuda a atacar a la
 * Serpiente (DEPENDENCIA con Enemigo): 2 de daño cada 5 segundos.
 */
public abstract class AnimalAtrapado extends Personaje {

    private static final int FRAMES_ENTRE_ATAQUES = 300; // 5 s a 60 FPS
    private static final int DANIO_AYUDA = 2;

    private boolean liberado;
    private final String tipoTrampa;
    private final String nombre;
    protected int progreso;
    protected final int requerido;
    private boolean trabajando;

    protected Jugador seguirA;
    protected Enemigo objetivoCombate;
    private int cooldownAtaque;
    protected int flashAtaque;

    protected int distanciaSeguimiento = 60;
    protected boolean vuela;

    protected AnimalAtrapado(int x, int y, int ancho, int alto, String nombre, String tipoTrampa, int requerido) {
        super(x, y, ancho, alto, 50);
        this.nombre = nombre;
        this.tipoTrampa = tipoTrampa;
        this.requerido = requerido;
    }

    /**
     * Cada especie define su método de liberación. Se llama en cada frame mientras
     * el jugador mantiene la tecla de acción; internamente suele llamar a avanzar().
     */
    public abstract void liberar();

    /** Suma un "paso" de progreso; al llegar a "requerido" el animal queda libre. */
    protected void avanzar() {
        if (liberado) {
            return;
        }
        trabajando = true;
        progreso++;
        if (progreso >= requerido) {
            liberado = true;
        }
    }

    /** Lo deja libre de inmediato (para los aliados que llegan al campamento). */
    public void marcarLiberado() {
        liberado = true;
        progreso = requerido;
    }

    /** Ataca al objetivo asignado cada 5 segundos. */
    public void ayudarEnCombate() {
        if (!liberado || objetivoCombate == null || objetivoCombate.getVida() <= 0) {
            return;
        }
        cooldownAtaque++;
        if (cooldownAtaque >= FRAMES_ENTRE_ATAQUES) {
            cooldownAtaque = 0;
            objetivoCombate.recibirDanio(DANIO_AYUDA);
            flashAtaque = 22;
        }
    }

    @Override
    public void update() {
        tick++;
        if (flashAtaque > 0) {
            flashAtaque--;
        }
        if (!liberado) {
            if (!trabajando && progreso > 0) {
                progreso = Math.max(0, progreso - 2); // si sueltas la tecla, se pierde avance
            }
            trabajando = false;
            return;
        }
        seguir();
        if (objetivoCombate != null) {
            ayudarEnCombate();
        }
    }

    private void seguir() {
        if (seguirA == null) {
            return;
        }
        int objetivoX;
        if (seguirA.isMirandoDerecha()) {
            objetivoX = seguirA.getX() - distanciaSeguimiento;
        } else {
            objetivoX = seguirA.getX() + seguirA.getAncho() + distanciaSeguimiento - ancho;
        }
        int objetivoY;
        if (vuela) {
            objetivoY = seguirA.getY() - 18 + (int) (6 * Math.sin(tick * 0.12));
        } else {
            objetivoY = seguirA.getY() + seguirA.getAlto() - alto;
        }
        x += (objetivoX - x) / 8;
        y += (objetivoY - y) / 5;
        if (objetivoCombate != null) {
            mirandoDerecha = objetivoCombate.getX() > x;
        } else {
            mirandoDerecha = seguirA.isMirandoDerecha();
        }
    }

    // ------------------------------------------------------------------
    //  Dibujo: animal + jaula (si sigue prisionero) o rayo de ayuda
    // ------------------------------------------------------------------

    @Override
    public void dibujar(Graphics2D g) {
        super.dibujar(g);
        if (!liberado) {
            dibujarJaula(g);
            dibujarProgreso(g);
        } else if (flashAtaque > 0 && objetivoCombate != null) {
            g.setColor(new Color(255, 230, 120, 40 + flashAtaque * 8));
            g.setStroke(new BasicStroke(3f));
            g.drawLine(x + ancho / 2, y + alto / 2,
                    objetivoCombate.getX() + objetivoCombate.getAncho() / 2,
                    objetivoCombate.getY() + objetivoCombate.getAlto() / 2);
            g.setStroke(new BasicStroke(1f));
        }
    }

    /** Jaula por defecto: barrotes verticales. Las subclases la personalizan. */
    protected void dibujarJaula(Graphics2D g) {
        int cx = x + ancho / 2;
        int base = y + alto;
        int izq = cx - 38;
        int der = cx + 38;
        int arriba = base - 74;
        g.setColor(new Color(0x4b3b2b));
        g.fillRect(izq - 3, arriba - 4, 82, 6);
        g.fillRect(izq - 3, base - 4, 82, 4);
        g.setColor(new Color(0x7d6a52));
        for (int bx = izq; bx <= der; bx += 11) {
            g.fillRect(bx, arriba, 4, base - arriba);
        }
    }

    protected void dibujarProgreso(Graphics2D g) {
        int cx = x + ancho / 2;
        int base = y + alto;
        if (progreso > 0) {
            int w = 64;
            int bx = cx - w / 2;
            int by = base - 92;
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(bx - 2, by - 2, w + 4, 12, 6, 6);
            g.setColor(new Color(0x74d1a0));
            g.fillRoundRect(bx, by, (int) (w * (progreso / (double) requerido)), 8, 4, 4);
        } else if (seguirA != null && cercaDe(seguirA)) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String txt = "Mantén [E] para " + tipoTrampa;
            int tw = g.getFontMetrics().stringWidth(txt);
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(cx - tw / 2 - 6, base - 108, tw + 12, 20, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString(txt, cx - tw / 2, base - 93);
        }
    }

    private boolean cercaDe(Jugador j) {
        int dx = Math.abs((j.getX() + j.getAncho() / 2) - (x + ancho / 2));
        int dy = Math.abs((j.getY() + j.getAlto() / 2) - (y + alto / 2));
        return dx <= 70 && dy <= 80;
    }

    // ------------------------------------------------------------------
    //  Getters / setters
    // ------------------------------------------------------------------

    public boolean isLiberado() {
        return liberado;
    }

    public String getTipoTrampa() {
        return tipoTrampa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setSeguirA(Jugador j) {
        this.seguirA = j;
    }

    public void setObjetivoCombate(Enemigo e) {
        this.objetivoCombate = e;
    }
}
