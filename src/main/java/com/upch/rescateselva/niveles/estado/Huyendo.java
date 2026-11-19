package com.upch.rescateselva.niveles.estado;

import com.upch.rescateselva.niveles.Nivel;

// PATRÓN STATE: estado concreto (huida)
public class Huyendo implements EstadoNivel {
    private boolean iniciado;

    @Override
    public boolean enHuida() {
        return true;
    }

    @Override
    public void manejar(Nivel n) {
        if (!iniciado) {
            iniciado = true;
            n.iniciarHuida();
        }
        n.avanzarCronometro();
    }
}
