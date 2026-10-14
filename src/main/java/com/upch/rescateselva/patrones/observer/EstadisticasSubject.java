package com.upch.rescateselva.patrones.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón OBSERVER (GoF) - sujeto observado.
 * Guarda el tiempo jugado (en segundos) y la vida actuales del jugador y avisa a todos los
 * observadores registrados cuando cambian.
 */
public class EstadisticasSubject {

    private final List<Observador> observadores = new ArrayList<>();
    private int segundos;
    private int vida;

    public void agregarObservador(Observador o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
            o.actualizar(segundos, vida); // el nuevo observador se pone al dia
        }
    }

    public void quitarObservador(Observador o) {
        observadores.remove(o);
    }

    /** Cambia el estado y notifica a todos. */
    public void setEstado(int segundos, int vida) {
        this.segundos = segundos;
        this.vida = vida;
        notificar();
    }

    public void notificar() {
        for (Observador o : observadores) {
            o.actualizar(segundos, vida);
        }
    }

    public int getSegundos() {
        return segundos;
    }

    public int getVida() {
        return vida;
    }
}
