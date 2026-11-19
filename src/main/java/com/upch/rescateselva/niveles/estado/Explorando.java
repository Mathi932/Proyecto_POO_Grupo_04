package com.upch.rescateselva.niveles.estado;

import com.upch.rescateselva.niveles.Nivel;

// PATRÓN STATE: estado concreto (ida)
public class Explorando implements EstadoNivel {
    @Override
    public boolean enHuida() {
        return false;
    }

    @Override
    public void manejar(Nivel n) {
        if (n.todosLiberados()) {
            n.anunciarLiberacion();
            n.cambiarEstado(new Huyendo());
        }
    }
}
