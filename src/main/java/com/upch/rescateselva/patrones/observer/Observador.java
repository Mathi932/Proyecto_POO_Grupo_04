package com.upch.rescateselva.patrones.observer;

/**
 * Patrón OBSERVER (GoF) - interfaz del observador.
 * Cualquier clase que quiera enterarse de cambios en la puntuación/vida
 * del jugador implementa esta interfaz (por ejemplo, el HUD).
 */
public interface Observador {

    /** Se llama cada vez que cambia la puntuación o la vida del jugador. */
    void actualizar(int puntuacion, int vida);
}
