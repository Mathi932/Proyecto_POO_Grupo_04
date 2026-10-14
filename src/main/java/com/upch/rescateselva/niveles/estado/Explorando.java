package com.upch.rescateselva.niveles.estado;

import com.upch.rescateselva.niveles.Nivel;

/**
 * Fase de IDA: se avanza por el nivel esquivando peligros hasta llegar al animal.
 * Sin cronómetro. En cuanto el animal queda libre, el nivel pasa a "Huyendo".
 */
public class Explorando implements EstadoNivel {

    @Override
    public void manejar(Nivel n) {
        if (n.todosLiberados()) {
            n.anunciarLiberacion();
            n.cambiarEstado(new Huyendo());
        }
    }
}
