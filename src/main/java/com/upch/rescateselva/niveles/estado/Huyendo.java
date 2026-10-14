package com.upch.rescateselva.niveles.estado;

import com.upch.rescateselva.niveles.Nivel;

/**
 * Fase de VUELTA / HUIDA.
 *  - Niveles 1 y 2: hay que regresar a la salida antes de que se acabe el cronómetro
 *    (en el 2 además un Cazador persigue). Si el tiempo llega a 0, el jugador se
 *    cansa (jugador.activarCansancio()) y casi no puede moverse.
 *  - Nivel 3: se entra al corredor del campamento y se pelea contra la Serpiente
 *    (sin cronómetro).
 */
public class Huyendo implements EstadoNivel {

    private boolean iniciado;

    @Override
    public void manejar(Nivel n) {
        if (!iniciado) {
            iniciado = true;
            n.iniciarHuida();
        }
        n.avanzarCronometro();
    }
}
