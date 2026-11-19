package com.upch.rescateselva.niveles.estado;

import com.upch.rescateselva.niveles.Nivel;

// PATRÓN STATE: interfaz de estado
public interface EstadoNivel {
    void manejar(Nivel n);

    boolean enHuida();
}
