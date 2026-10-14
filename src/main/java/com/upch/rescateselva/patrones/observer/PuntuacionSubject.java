package com.upch.rescateselva.patrones.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón OBSERVER (GoF) - sujeto observado.
 * Guarda la puntuación y la vida actuales del jugador y avisa a todos los
 * observadores registrados cuando cambian.
 */
public class PuntuacionSubject {

    private final List<Observador> observadores = new ArrayList<>();
    private int puntuacion;
    private int vida;

    public void agregarObservador(Observador o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
            o.actualizar(puntuacion, vida); // el nuevo observador se pone al día
        }
    }

    public void quitarObservador(Observador o) {
        observadores.remove(o);
    }

    /** Cambia el estado y notifica a todos. */
    public void setEstado(int puntuacion, int vida) {
        this.puntuacion = puntuacion;
        this.vida = vida;
        notificar();
    }

    public void notificar() {
        for (Observador o : observadores) {
            o.actualizar(puntuacion, vida);
        }
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getVida() {
        return vida;
    }
}
