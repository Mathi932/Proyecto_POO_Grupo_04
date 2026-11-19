package com.upch.rescateselva.patrones.observer;

import java.util.ArrayList;
import java.util.List;

// PATRÓN OBSERVER: sujeto (notifica a los observadores registrados)
public class EstadisticasSubject {
    private final List<Observador> observadores = new ArrayList<>();
    private int segundos;
    private int vida;

    public void agregarObservador(Observador o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
            o.actualizar(segundos, vida);
        }
    }

    public void quitarObservador(Observador o) {
        observadores.remove(o);
    }

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
