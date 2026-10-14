package com.upch.rescateselva.core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lee el teclado. Distingue entre "tecla mantenida" (para moverse) y
 * "tecla recién pulsada" (para menús, pausa y lanzar).
 *
 * Controles: flechas o A/D = mover · flecha arriba/W/Espacio = saltar ·
 * E = liberar animal (mantener) · F = lanzar piedra · P o Esc = pausa.
 */
public class Entrada extends KeyAdapter {

    private final Set<Integer> mantenidas = ConcurrentHashMap.newKeySet();
    private final Set<Integer> nuevas = ConcurrentHashMap.newKeySet();

    @Override
    public void keyPressed(KeyEvent e) {
        if (mantenidas.add(e.getKeyCode())) {
            nuevas.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        mantenidas.remove(e.getKeyCode());
    }

    public boolean izquierda() {
        return mantenidas.contains(KeyEvent.VK_LEFT) || mantenidas.contains(KeyEvent.VK_A);
    }

    public boolean derecha() {
        return mantenidas.contains(KeyEvent.VK_RIGHT) || mantenidas.contains(KeyEvent.VK_D);
    }

    public boolean salto() {
        return mantenidas.contains(KeyEvent.VK_UP) || mantenidas.contains(KeyEvent.VK_W)
                || mantenidas.contains(KeyEvent.VK_SPACE);
    }

    public boolean accion() {
        return mantenidas.contains(KeyEvent.VK_E);
    }

    /** true una sola vez por cada pulsación de la tecla. */
    public boolean consumir(int codigo) {
        return nuevas.remove(codigo);
    }

    /** Descarta pulsaciones pendientes (al cambiar de pantalla). */
    public void limpiar() {
        nuevas.clear();
    }

    // ---- útiles para las pruebas automáticas (simulan teclas) ----

    public void simularPresion(int codigo) {
        if (mantenidas.add(codigo)) {
            nuevas.add(codigo);
        }
    }

    public void simularSuelta(int codigo) {
        mantenidas.remove(codigo);
    }
}
